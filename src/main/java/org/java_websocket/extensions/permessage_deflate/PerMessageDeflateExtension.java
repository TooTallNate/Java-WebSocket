package org.java_websocket.extensions.permessage_deflate;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.extensions.CompressionExtension;
import org.java_websocket.extensions.ExtensionRequestData;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.*;

/**
 * PerMessage Deflate Extension (<a href="https://tools.ietf.org/html/rfc7692#section-7">7&#46; The
 * "permessage-deflate" Extension</a> in
 * <a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a>).
 *
 * @see <a href="https://tools.ietf.org/html/rfc7692#section-7">7&#46; The "permessage-deflate"
 * Extension in RFC 7692</a>
 */
public class PerMessageDeflateExtension extends CompressionExtension {

  // Name of the extension as registered by IETF https://tools.ietf.org/html/rfc7692#section-9.
  private static final String EXTENSION_REGISTERED_NAME = "permessage-deflate";
  // Below values are defined for convenience. They are not used in the compression/decompression phase.
  // They may be needed during the extension-negotiation offer in the future.
  private static final String SERVER_NO_CONTEXT_TAKEOVER = "server_no_context_takeover";
  private static final String CLIENT_NO_CONTEXT_TAKEOVER = "client_no_context_takeover";
  private static final String SERVER_MAX_WINDOW_BITS = "server_max_window_bits";
  private static final String CLIENT_MAX_WINDOW_BITS = "client_max_window_bits";
  private static final int serverMaxWindowBits = 1 << 15;
  private static final int clientMaxWindowBits = 1 << 15;
  private static final byte[] TAIL_BYTES = {(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};
  private static final int BUFFER_SIZE = 1 << 10;

  private int threshold = 1024;

  private boolean serverNoContextTakeover = true;
  private boolean clientNoContextTakeover = false;

  // For WebSocketServers, this variable holds the extension parameters that the peer client has requested.
  // For WebSocketClients, this variable holds the extension parameters that client himself has requested.
  private Map<String, String> requestedParameters = new LinkedHashMap<>();

  private final int compressionLevel;

  private final Inflater inflater;
  private final Deflater deflater;

  /**
   * Constructor for the PerMessage Deflate Extension (<a href="https://tools.ietf.org/html/rfc7692#section-7">7&#46; Thepermessage-deflate" Extension</a>)
   *
   * Uses {@link java.util.zip.Deflater#DEFAULT_COMPRESSION} as the compression level for the {@link java.util.zip.Deflater#Deflater(int)}
   */
  public PerMessageDeflateExtension() {
    this(Deflater.DEFAULT_COMPRESSION);
  }

  /**
   * Constructor for the PerMessage Deflate Extension (<a href="https://tools.ietf.org/html/rfc7692#section-7">7&#46; Thepermessage-deflate" Extension</a>)
   *
   * @param compressionLevel The compression level passed to the {@link java.util.zip.Deflater#Deflater(int)}
   */
  public PerMessageDeflateExtension(int compressionLevel) {
    this.compressionLevel = compressionLevel;
    this.deflater = new Deflater(this.compressionLevel, true);
    this.inflater = new Inflater(true);
  }

  /**
   * Get the compression level used for the compressor.
   * @return the compression level
   */
  public int getCompressionLevel() {
    return this.compressionLevel;
  }

  /**
   * Get the size threshold for doing the compression
   * @return Size (in bytes) below which messages will not be compressed
   * @since 1.5.3
   */
  public int getThreshold() {
    return threshold;
  }

  /**
   * Set the size when payloads smaller than this will not be compressed.
   * @param threshold the size in bytes
   * @since 1.5.3
   */
  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  /**
   * Access the "server_no_context_takeover" extension parameter
   *
   * @see <a href="https://tools.ietf.org/html/rfc7692#section-7.1.1.1">The "server_no_context_takeover" Extension Parameter</a>
   * @return serverNoContextTakeover is the server no context parameter active
   */
  public boolean isServerNoContextTakeover() {
    return serverNoContextTakeover;
  }

  /**
   * Setter for the "server_no_context_takeover" extension parameter
   * @see <a href="https://tools.ietf.org/html/rfc7692#section-7.1.1.1">The "server_no_context_takeover" Extension Parameter</a>
   * @param serverNoContextTakeover set the server no context parameter
   */
  public void setServerNoContextTakeover(boolean serverNoContextTakeover) {
    this.serverNoContextTakeover = serverNoContextTakeover;
  }

  /**
   * Access the "client_no_context_takeover" extension parameter
   *
   * @see <a href="https://tools.ietf.org/html/rfc7692#section-7.1.1.2">The "client_no_context_takeover" Extension Parameter</a>
   * @return clientNoContextTakeover is the client no context parameter active
   */
  public boolean isClientNoContextTakeover() {
    return clientNoContextTakeover;
  }

  /**
   * Setter for the "client_no_context_takeover" extension parameter
   * @see <a href="https://tools.ietf.org/html/rfc7692#section-7.1.1.2">The "client_no_context_takeover" Extension Parameter</a>
   * @param clientNoContextTakeover set the client no context parameter
   */
  public void setClientNoContextTakeover(boolean clientNoContextTakeover) {
    this.clientNoContextTakeover = clientNoContextTakeover;
  }

  /*
      An endpoint uses the following algorithm to decompress a message.
      1.  Append 4 octets of 0x00 0x00 0xff 0xff to the tail end of the
         payload of the message.
      2.  Decompress the resulting data using DEFLATE.
      See, https://tools.ietf.org/html/rfc7692#section-7.2.2
   */
  @Override
  public void decodeFrame(Framedata inputFrame) throws InvalidDataException {
    // Only DataFrames can be decompressed.
    if (!(inputFrame instanceof DataFrame)) {
      return;
    }

    if (!inputFrame.isRSV1() && inputFrame.getOpcode() != Opcode.CONTINUOUS) {
      return;
    }

    // RSV1 bit must be set only for the first frame.
    if (inputFrame.getOpcode() == Opcode.CONTINUOUS && inputFrame.isRSV1()) {
      throw new InvalidDataException(CloseCodeConstants.POLICY_VALIDATION,
          "RSV1 bit can only be set for the first frame.");
    }

    // Decompressed output buffer.
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      decompress(inputFrame.getPayloadData().array(), output);

      /*
          If a message is "first fragmented and then compressed", as this project does, then the inflater
              can not inflate fragments except the first one.
          This behavior occurs most likely because those fragments end with "final deflate blocks".
          We can check the getRemaining() method to see whether the data we supplied has been decompressed or not.
          And if not, we just reset the inflater and decompress again.
          Note that this behavior doesn't occur if the message is "first compressed and then fragmented".
       */
      if (inflater.getRemaining() > 0) {
        inflater.reset();
        decompress(inputFrame.getPayloadData().array(), output);
      }

      if (inputFrame.isFin()) {
        decompress(TAIL_BYTES, output);
        // If context takeover is disabled, inflater can be reset.
        if (clientNoContextTakeover) {
          inflater.reset();
        }
      }
    } catch (DataFormatException e) {
      throw new InvalidDataException(CloseCodeConstants.POLICY_VALIDATION, e.getMessage());
    }

    // Set frames payload to the new decompressed data.
    ((FramedataImpl1) inputFrame)
        .setPayload(ByteBuffer.wrap(output.toByteArray(), 0, output.size()));
  }

  /**
   * @param data         the bytes of data
   * @param outputBuffer the output stream
   * @throws DataFormatException
   */
  private void decompress(byte[] data, ByteArrayOutputStream outputBuffer)
      throws DataFormatException {
    inflater.setInput(data);
    byte[] buffer = new byte[BUFFER_SIZE];

    int bytesInflated;
    while ((bytesInflated = inflater.inflate(buffer)) > 0) {
      outputBuffer.write(buffer, 0, bytesInflated);
    }
  }

  @Override
  public void encodeFrame(Framedata inputFrame) {
    // Only DataFrames can be decompressed.
    if (!(inputFrame instanceof DataFrame)) {
      return;
    }

    byte[] payloadData = inputFrame.getPayloadData().array();
    if (payloadData.length < threshold) {
      return;
    }
    // Only the first frame's RSV1 must be set.
    if (!(inputFrame instanceof ContinuousFrame)) {
      ((DataFrame) inputFrame).setRSV1(true);
    }

    deflater.setInput(payloadData);
    // Compressed output buffer.
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    // Temporary buffer to hold compressed output.
    byte[] buffer = new byte[1024];
    int bytesCompressed;
    while ((bytesCompressed = deflater.deflate(buffer, 0, buffer.length, Deflater.SYNC_FLUSH))
        > 0) {
      output.write(buffer, 0, bytesCompressed);
    }

    byte[] outputBytes = output.toByteArray();
    int outputLength = outputBytes.length;

    /*
        https://tools.ietf.org/html/rfc7692#section-7.2.1 states that if the final fragment's compressed
            payload ends with 0x00 0x00 0xff 0xff, they should be removed.
        To simulate removal, we just pass 4 bytes less to the new payload
            if the frame is final and outputBytes ends with 0x00 0x00 0xff 0xff.
     */
    if (inputFrame.isFin()) {
      if (endsWithTail(outputBytes)) {
        outputLength -= TAIL_BYTES.length;
      }

      if (serverNoContextTakeover) {
        deflater.reset();
      }
    }

    // Set frames payload to the new compressed data.
    ((FramedataImpl1) inputFrame).setPayload(ByteBuffer.wrap(outputBytes, 0, outputLength));
  }

  /**
   * @param data the bytes of data
   * @return true if the data is OK
   */
  private static boolean endsWithTail(byte[] data) {
    if (data.length < 4) {
      return false;
    }

    int length = data.length;
    for (int i = 0; i < TAIL_BYTES.length; i++) {
      if (TAIL_BYTES[i] != data[length - TAIL_BYTES.length + i]) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean acceptProvidedExtensionAsServer(String inputExtension) {
    String[] requestedExtensions = inputExtension.split(",");
    for (String extension : requestedExtensions) {
      ExtensionRequestData extensionData = ExtensionRequestData.parseExtensionRequest(extension);
      if (!EXTENSION_REGISTERED_NAME.equalsIgnoreCase(extensionData.getExtensionName())) {
        continue;
      }

      // Holds parameters that peer client has sent.
      Map<String, String> headers = extensionData.getExtensionParameters();
      requestedParameters.putAll(headers);
      if (requestedParameters.containsKey(CLIENT_NO_CONTEXT_TAKEOVER)) {
        clientNoContextTakeover = true;
      }

      return true;
    }

    return false;
  }

  @Override
  public boolean acceptProvidedExtensionAsClient(String inputExtension) {
    String[] requestedExtensions = inputExtension.split(",");
    for (String extension : requestedExtensions) {
      ExtensionRequestData extensionData = ExtensionRequestData.parseExtensionRequest(extension);
      if (!EXTENSION_REGISTERED_NAME.equalsIgnoreCase(extensionData.getExtensionName())) {
        continue;
      }

      // Holds parameters that are sent by the server, as a response to our initial extension request.
      Map<String, String> headers = extensionData.getExtensionParameters();
      // After this point, parameters that the server sent back can be configured, but we don't use them for now.
      return true;
    }

    return false;
  }

  @Override
  public String getProvidedExtensionAsClient() {
    requestedParameters.put(CLIENT_NO_CONTEXT_TAKEOVER, ExtensionRequestData.EMPTY_VALUE);
    requestedParameters.put(SERVER_NO_CONTEXT_TAKEOVER, ExtensionRequestData.EMPTY_VALUE);

    return EXTENSION_REGISTERED_NAME + "; " + SERVER_NO_CONTEXT_TAKEOVER + "; "
        + CLIENT_NO_CONTEXT_TAKEOVER;
  }

  @Override
  public String getProvidedExtensionAsServer() {
    return EXTENSION_REGISTERED_NAME
        + "; " + SERVER_NO_CONTEXT_TAKEOVER
        + (clientNoContextTakeover ? "; " + CLIENT_NO_CONTEXT_TAKEOVER : "");
  }

  @Override
  public IExtension copyInstance() {
    PerMessageDeflateExtension clone = new PerMessageDeflateExtension(this.getCompressionLevel());
    clone.setThreshold(this.getThreshold());
    clone.setClientNoContextTakeover(this.isClientNoContextTakeover());
    clone.setServerNoContextTakeover(this.isServerNoContextTakeover());
    return clone;
  }

  /**
   * This extension requires the RSV1 bit to be set only for the first frame. If the frame is type
   * is CONTINUOUS, RSV1 bit must be unset.
   */
  @Override
  public void isFrameValid(Framedata inputFrame) throws InvalidDataException {
    if ((inputFrame instanceof ContinuousFrame) && (inputFrame.isRSV1() || inputFrame.isRSV2()
        || inputFrame.isRSV3())) {
      throw new InvalidFrameException(
          "bad rsv RSV1: " + inputFrame.isRSV1() + " RSV2: " + inputFrame.isRSV2() + " RSV3: "
              + inputFrame.isRSV3());
    }
    super.isFrameValid(inputFrame);
  }

  @Override
  public String toString() {
    return "PerMessageDeflateExtension";
  }


}
