/*
 * TooTallNate - Java-WebSocket
 *
 * MIT License
 *
 * Copyright (C) 2025 Robert Schlabbach <robert.schlabbach@ubitricity.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.java_websocket.extensions.permessage_deflate;

import static java.util.zip.Deflater.DEFAULT_COMPRESSION;
import static java.util.zip.Deflater.NO_FLUSH;
import static java.util.zip.Deflater.SYNC_FLUSH;
import static org.java_websocket.extensions.ExtensionRequestData.parseExtensionRequest;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.extensions.CompressionExtension;
import org.java_websocket.extensions.ExtensionRequestData;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.ContinuousFrame;
import org.java_websocket.framing.DataFrame;
import org.java_websocket.framing.Framedata;

/** RFC 7692 WebSocket Per-Message Deflate Extension implementation */
public class PerMessageDeflateExtension extends CompressionExtension {
  // RFC 7692 extension common name and identifier
  public static final String EXTENSION_COMMON_NAME = "WebSocket Per-Message Deflate";
  public static final String EXTENSION_IDENTIFIER = "permessage-deflate";

  // RFC 7692 extension parameters
  public static final String PARAMETER_CLIENT_NO_CONTEXT_TAKEOVER = "client_no_context_takeover";
  public static final String PARAMETER_SERVER_NO_CONTEXT_TAKEOVER = "server_no_context_takeover";
  public static final String PARAMETER_CLIENT_MAX_WINDOW_BITS = "client_max_window_bits";
  public static final int MINIMUM_CLIENT_MAX_WINDOW_BITS = 8;
  public static final int MAXIMUM_CLIENT_MAX_WINDOW_BITS = 15;
  public static final String PARAMETER_SERVER_MAX_WINDOW_BITS = "server_max_window_bits";
  public static final int MINIMUM_SERVER_MAX_WINDOW_BITS = 8;
  public static final int MAXIMUM_SERVER_MAX_WINDOW_BITS = 15;

  // RFC 7692 extension parameter defaults
  public static boolean DEFAULT_CLIENT_NO_CONTEXT_TAKEOVER = false;
  public static boolean DEFAULT_SERVER_NO_CONTEXT_TAKEOVER = false;
  public static int DEFAULT_CLIENT_MAX_WINDOW_BITS = MAXIMUM_CLIENT_MAX_WINDOW_BITS;
  public static int DEFAULT_SERVER_MAX_WINDOW_BITS = MAXIMUM_SERVER_MAX_WINDOW_BITS;
  public static int DEFAULT_COMPRESSION_THRESHOLD = 64;

  // RFC 7692 tail end to be removed from compressed data and appended when decompressing
  public static final byte[] EMPTY_DEFLATE_BLOCK =
      new byte[] {0x00, 0x00, (byte) 0xff, (byte) 0xff};

  // RFC 7692 empty uncompressed DEFLATE block to be used when out of uncompressed data
  public static final byte[] EMPTY_UNCOMPRESSED_DEFLATE_BLOCK = new byte[] {0x00};

  private static final int TRANSFER_CHUNK_SIZE = 8192;

  private final int compressionLevel;
  private final int maxFragmentSize;
  private final Deflater compressor;
  private final Inflater decompressor;

  private int compressionThreshold;
  private boolean clientNoContextTakeover;
  private boolean serverNoContextTakeover;
  private int clientMaxWindowBits;
  private int serverMaxWindowBits;

  private boolean isCompressorResetRequired;
  private boolean isDecompressorResetAllowed;
  private boolean isCompressing;
  private boolean isDecompressing;
  private long compressedBytes;
  private long decompressedBytes;

  public PerMessageDeflateExtension() {
    this(DEFAULT_COMPRESSION);
  }

  public PerMessageDeflateExtension(int compressionLevel) {
    this(compressionLevel, Integer.MAX_VALUE);
  }

  /**
   * Constructs the RFC 7692 permessage-deflate extension with a specific compression level and a
   * maximum decompressed fragment size.
   *
   * @param compressionLevel the compression level to use, see {@link Deflater}
   * @param maxFragmentSize the maximum allowed fragment size after decompression
   */
  public PerMessageDeflateExtension(int compressionLevel, int maxFragmentSize) {
    this.compressionLevel = compressionLevel;
    this.maxFragmentSize = maxFragmentSize;
    compressor = new Deflater(compressionLevel, true);
    decompressor = new Inflater(true);
    compressionThreshold = DEFAULT_COMPRESSION_THRESHOLD;
    clientNoContextTakeover = DEFAULT_CLIENT_NO_CONTEXT_TAKEOVER;
    serverNoContextTakeover = DEFAULT_SERVER_NO_CONTEXT_TAKEOVER;
    clientMaxWindowBits = DEFAULT_CLIENT_MAX_WINDOW_BITS;
    serverMaxWindowBits = DEFAULT_SERVER_MAX_WINDOW_BITS;
    isCompressorResetRequired = false;
    isDecompressorResetAllowed = false;
    isCompressing = false;
    isDecompressing = false;
    compressedBytes = 0;
    decompressedBytes = 0;
  }

  public int getCompressionLevel() {
    return compressionLevel;
  }

  public int getMaxFragmentSize() {
    return maxFragmentSize;
  }

  public int getThreshold() {
    return compressionThreshold;
  }

  public void setThreshold(int threshold) {
    this.compressionThreshold = threshold;
  }

  public boolean isClientNoContextTakeover() {
    return clientNoContextTakeover;
  }

  public void setClientNoContextTakeover(boolean clientNoContextTakeover) {
    this.clientNoContextTakeover = clientNoContextTakeover;
  }

  public boolean isServerNoContextTakeover() {
    return serverNoContextTakeover;
  }

  public void setServerNoContextTakeover(boolean serverNoContextTakeover) {
    this.serverNoContextTakeover = serverNoContextTakeover;
  }

  /**
   * Returns the overall compression ratio of all incoming and outgoing payloads which were
   * compressed.
   *
   * <p>Values below 1 mean the compression is effective, the lower, the better. If you get values
   * above 1, look into increasing the compression level or the threshold. If that does not help,
   * consider not using this extension.
   *
   * <p>IMPORTANT: This API must be called on the class instance used by the library, NOT on the
   * instance which was handed to the library! To get this class instance, retrieve it from the
   * library e.g. via ((Draft_6455) webSocketClient.getConnection().getDraft()).getExtension().
   * Make sure to apply class instance checks, as the extension may not have been negotiated.
   *
   * @return the overall compression ratio of all incoming and outgoing payloads
   */
  public double getCompressionRatio() {
    double decompressed = decompressedBytes;
    return decompressed > 0 ? compressedBytes / decompressed : 1;
  }

  @Override
  public void isFrameValid(Framedata inputFrame) throws InvalidDataException {
    // RFC 7692: RSV1 may only be set for the first fragment of a message
    if (inputFrame instanceof ContinuousFrame
        && (inputFrame.isRSV1() || inputFrame.isRSV2() || inputFrame.isRSV3())) {
      throw new InvalidFrameException("Continuous frame cannot have RSV1, RSV2 or RSV3 set");
    }
    super.isFrameValid(inputFrame);
  }

  @Override
  public void decodeFrame(Framedata inputFrame) throws InvalidDataException {
    // RFC 7692: PMCEs operate only on data messages.
    if (!(inputFrame instanceof DataFrame)) {
      return;
    }

    // decompression is only applicable if it was started on the first fragment
    if (!isDecompressing && inputFrame instanceof ContinuousFrame) {
      return;
    }

    // check the RFC 7692 compression marker RSV1 whether to start decompressing
    if (inputFrame.isRSV1()) {
      isDecompressing = true;
    }

    if (!isDecompressing) {
      return;
    }

    // decompress the frame payload
    DataFrame dataFrame = (DataFrame) inputFrame;
    ByteBuffer payload = dataFrame.getPayloadData();
    compressedBytes += payload.remaining();
    byte[] decompressed = decompress(payload, dataFrame.isFin());
    decompressedBytes += decompressed.length;
    dataFrame.setPayload(ByteBuffer.wrap(decompressed));

    // payload is no longer compressed, clear the RFC 7692 compression marker RSV1
    if (!(dataFrame instanceof ContinuousFrame)) {
      dataFrame.setRSV1(false);
    }

    // stop decompressing after the final fragment
    if (dataFrame.isFin()) {
      isDecompressing = false;
      // RFC 7692: If the "agreed parameters" contain the "client|server_no_context_takeover"
      // extension parameter, the server|client MAY decompress each new message with an empty
      // LZ77 sliding window.
      if (isDecompressorResetAllowed) {
        decompressor.reset();
      }
    }
  }

  private byte[] decompress(ByteBuffer buffer, boolean isFinal) throws InvalidDataException {
    ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
    try {
      decompress(buffer, decompressed);
      // RFC 7692: Append empty deflate block to the tail end of the payload of the message
      if (isFinal) {
        decompress(ByteBuffer.wrap(EMPTY_DEFLATE_BLOCK), decompressed);
      }
    } catch (DataFormatException e) {
      throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, e.getMessage());
    }
    return decompressed.toByteArray();
  }

  private void decompress(ByteBuffer buffer, ByteArrayOutputStream decompressed)
      throws DataFormatException {
    if (buffer.hasArray()) {
      decompressor.setInput(
          buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    } else {
      byte[] input = new byte[buffer.remaining()];
      buffer.duplicate().get(input);
      decompressor.setInput(input);
    }
    byte[] chunk = new byte[TRANSFER_CHUNK_SIZE];
    while (!decompressor.finished()) {
      int length = decompressor.inflate(chunk);
      if (length > 0) {
        decompressed.write(chunk, 0, length);
        if (maxFragmentSize > 0 && maxFragmentSize < decompressed.size()) {
          throw new DataFormatException(
              "Inflated fragment size exceeds limit of " + maxFragmentSize + " bytes");
        }
      } else {
        break;
      }
    }
  }

  @Override
  public void encodeFrame(Framedata inputFrame) {
    // RFC 7692: PMCEs operate only on data messages.
    if (!(inputFrame instanceof DataFrame)) {
      return;
    }

    // compression is only applicable if it was started on the first fragment
    if (!isCompressing && inputFrame instanceof ContinuousFrame) {
      return;
    }

    // check the threshold whether to start compressing
    if (inputFrame.getPayloadData().remaining() >= compressionThreshold) {
      isCompressing = true;
    }

    if (!isCompressing) {
      return;
    }

    // compress the frame payload
    DataFrame dataFrame = (DataFrame) inputFrame;
    ByteBuffer payload = dataFrame.getPayloadData();
    decompressedBytes += payload.remaining();
    byte[] compressed = compress(payload, dataFrame.isFin());
    compressedBytes += compressed.length;
    dataFrame.setPayload(ByteBuffer.wrap(compressed));

    // payload is compressed now, set the RFC 7692 compression marker RSV1
    if (!(dataFrame instanceof ContinuousFrame)) {
      dataFrame.setRSV1(true);
    }

    // stop compressing after the final fragment
    if (dataFrame.isFin()) {
      isCompressing = false;
      // RFC 7692: If the "agreed parameters" contain the "client|server_no_context_takeover"
      // extension parameter, the client|server MUST start compressing each new message with an
      // empty LZ77 sliding window.
      if (isCompressorResetRequired) {
        compressor.reset();
      }
    }
  }

  private byte[] compress(ByteBuffer buffer, boolean isFinal) {
    // RFC 7692: Generate an empty fragment if the buffer for uncompressed data buffer is empty.
    if (!buffer.hasRemaining() && isFinal) {
      return EMPTY_UNCOMPRESSED_DEFLATE_BLOCK;
    }
    if (buffer.hasArray()) {
      compressor.setInput(
          buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    } else {
      byte[] input = new byte[buffer.remaining()];
      buffer.duplicate().get(input);
      compressor.setInput(input);
    }
    // RFC 7692 prefers the compressor output not to have the BFINAL bit set, so instead of calling
    // finish(), deflate with NO_FLUSH until the input is exhausted, then deflate with SYNC_FLUSH
    // until the output runs dry.
    ByteArrayOutputStream compressed = new ByteArrayOutputStream();
    byte[] chunk = new byte[TRANSFER_CHUNK_SIZE];
    while (!compressor.needsInput()) {
      int length = compressor.deflate(chunk, 0, chunk.length, NO_FLUSH);
      if (length > 0) {
        compressed.write(chunk, 0, length);
      } else {
        break;
      }
    }
    while (!compressor.finished()) {
      int length = compressor.deflate(chunk, 0, chunk.length, SYNC_FLUSH);
      if (length > 0) {
        compressed.write(chunk, 0, length);
      } else {
        break;
      }
    }
    return isFinal
        ? removeTail(compressed.toByteArray(), EMPTY_DEFLATE_BLOCK)
        : compressed.toByteArray();
  }

  private byte[] removeTail(byte[] input, byte[] tail) {
    return hasTail(input, tail) ? Arrays.copyOf(input, input.length - tail.length) : input;
  }

  private boolean hasTail(byte[] input, byte[] tail) {
    int offset = input.length - tail.length;
    if (offset < 0) {
      return false;
    }
    for (int i = 0; i < tail.length; i++) {
      if (input[offset + i] != tail[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean acceptProvidedExtensionAsServer(String inputExtension) {
    for (String extensionRequest : inputExtension.split(",")) {
      ExtensionRequestData extensionRequestData = parseExtensionRequest(extensionRequest);
      if (EXTENSION_IDENTIFIER.equalsIgnoreCase(extensionRequestData.getExtensionName())
          && acceptExtensionParametersAsServer(extensionRequestData)) {
        // extension offer with acceptable extension parameters found
        return true;
      }
    }
    return false;
  }

  private boolean acceptExtensionParametersAsServer(ExtensionRequestData extensionRequestData) {
    // initialize extension negotiation offer parameters
    boolean offerClientNoContextTakeover = false;
    boolean offerServerNoContextTakeover = false;
    Optional<Integer> offerClientMaxWindowBits = Optional.empty();
    Optional<Integer> offerServerMaxWindowBits = Optional.empty();

    // scan through the parameters in the extension negotiation offer
    for (Map.Entry<String, String> parameter :
        extensionRequestData.getExtensionParameters().entrySet()) {
      if (PARAMETER_CLIENT_NO_CONTEXT_TAKEOVER.equalsIgnoreCase(parameter.getKey())) {
        offerClientNoContextTakeover = true;
      } else if (PARAMETER_SERVER_NO_CONTEXT_TAKEOVER.equalsIgnoreCase(parameter.getKey())) {
        offerServerNoContextTakeover = true;
      } else if (PARAMETER_CLIENT_MAX_WINDOW_BITS.equalsIgnoreCase(parameter.getKey())) {
        // RFC 7692: This parameter may have no value to only indicate support for it
        if (parameter.getValue().isEmpty()) {
          offerClientMaxWindowBits = Optional.of(clientMaxWindowBits);
        } else {
          try {
            offerClientMaxWindowBits = Optional.of(Integer.parseInt(parameter.getValue()));
            if (offerClientMaxWindowBits.get() < MINIMUM_CLIENT_MAX_WINDOW_BITS
                || offerClientMaxWindowBits.get() > MAXIMUM_CLIENT_MAX_WINDOW_BITS) {
              return false;
            }
          } catch (NumberFormatException e) {
            return false;
          }
        }
      } else if (PARAMETER_SERVER_MAX_WINDOW_BITS.equalsIgnoreCase(parameter.getKey())) {
        // RFC 7692: This parameter must always have a value
        try {
          offerServerMaxWindowBits = Optional.of(Integer.parseInt(parameter.getValue()));
          if (offerServerMaxWindowBits.get() < MINIMUM_SERVER_MAX_WINDOW_BITS
              || offerServerMaxWindowBits.get() > MAXIMUM_SERVER_MAX_WINDOW_BITS) {
            return false;
          }
          // The Java Deflater class only supports the default maximum window bits (15)
          if (offerServerMaxWindowBits.get() != DEFAULT_SERVER_MAX_WINDOW_BITS) {
            return false;
          }
        } catch (NumberFormatException e) {
          return false;
        }
      } else {
        // RFC 7692: A server MUST decline an extension negotiation offer for this extension
        // if the negotiation offer contains an extension parameter not defined for use in an
        // offer.
        return false;
      }
    }

    // merge accepted extension parameters with local configuration
    clientNoContextTakeover |= offerClientNoContextTakeover;
    serverNoContextTakeover |= offerServerNoContextTakeover;
    clientMaxWindowBits = offerClientMaxWindowBits.orElse(clientMaxWindowBits);
    serverMaxWindowBits = offerServerMaxWindowBits.orElse(serverMaxWindowBits);

    // RFC 7692: The extension parameters with the "server_" prefix are used by the server to
    // configure its compressor. The extension parameters with the "client_" prefix are used by
    // the server to configure its decompressor.
    isCompressorResetRequired = serverNoContextTakeover;
    isDecompressorResetAllowed = clientNoContextTakeover;
    return true;
  }

  @Override
  public boolean acceptProvidedExtensionAsClient(String inputExtension) {
    for (String extensionRequest : inputExtension.split(",")) {
      ExtensionRequestData extensionRequestData = parseExtensionRequest(extensionRequest);
      if (EXTENSION_IDENTIFIER.equalsIgnoreCase(extensionRequestData.getExtensionName())) {
        return acceptExtensionParametersAsClient(extensionRequestData);
      }
    }
    return false;
  }

  private boolean acceptExtensionParametersAsClient(ExtensionRequestData extensionRequestData) {
    // initialize extension negotiation response parameters
    boolean responseClientNoContextTakeover = false;
    boolean responseServerNoContextTakeover = false;
    Optional<Integer> responseClientMaxWindowBits = Optional.empty();
    Optional<Integer> responseServerMaxWindowBits = Optional.empty();

    // scan through the parameters in the extension negotiation response
    for (Map.Entry<String, String> parameter :
        extensionRequestData.getExtensionParameters().entrySet()) {
      if (PARAMETER_CLIENT_NO_CONTEXT_TAKEOVER.equalsIgnoreCase(parameter.getKey())) {
        responseClientNoContextTakeover = true;
      } else if (PARAMETER_SERVER_NO_CONTEXT_TAKEOVER.equalsIgnoreCase(parameter.getKey())) {
        responseServerNoContextTakeover = true;
      } else if (PARAMETER_CLIENT_MAX_WINDOW_BITS.equalsIgnoreCase(parameter.getKey())) {
        try {
          responseClientMaxWindowBits = Optional.of(Integer.parseInt(parameter.getValue()));
          if (responseClientMaxWindowBits.get() < MINIMUM_CLIENT_MAX_WINDOW_BITS
              || responseClientMaxWindowBits.get() > MAXIMUM_CLIENT_MAX_WINDOW_BITS) {
            return false;
          }
          // The Java Deflater class only supports the default maximum window bits (15)
          if (responseClientMaxWindowBits.get() != DEFAULT_CLIENT_MAX_WINDOW_BITS) {
            return false;
          }
        } catch (NumberFormatException e) {
          return false;
        }
      } else if (PARAMETER_SERVER_MAX_WINDOW_BITS.equalsIgnoreCase(parameter.getKey())) {
        try {
          responseServerMaxWindowBits = Optional.of(Integer.parseInt(parameter.getValue()));
          if (responseServerMaxWindowBits.get() < MINIMUM_SERVER_MAX_WINDOW_BITS
              || responseServerMaxWindowBits.get() > MAXIMUM_SERVER_MAX_WINDOW_BITS) {
            return false;
          }
        } catch (NumberFormatException e) {
          return false;
        }
      } else {
        // RFC 7692: A client MUST _Fail the WebSocket Connection_ if the peer server accepted an
        // extension negotiation offer for this extension with an extension negotiation response
        // that contains an extension parameter not defined for use in a response.
        return false;
      }
    }

    // merge accepted extension parameters with local configuration
    clientNoContextTakeover |= responseClientNoContextTakeover;
    // the server_no_context_takeover parameter MUST NOT be merged with the local setting!
    // if the server does not return this parameter, it must not be used.
    serverNoContextTakeover = responseServerNoContextTakeover;
    clientMaxWindowBits = responseClientMaxWindowBits.orElse(clientMaxWindowBits);
    serverMaxWindowBits = responseServerMaxWindowBits.orElse(serverMaxWindowBits);

    // RFC 7692: The extension parameters with the "client_" prefix are used by the client to
    // configure its compressor. The extension parameters with the "server_" prefix are used by
    // the client to configure its decompressor.
    isCompressorResetRequired = clientNoContextTakeover;
    isDecompressorResetAllowed = serverNoContextTakeover;
    return true;
  }

  @Override
  public String getProvidedExtensionAsClient() {
    return EXTENSION_IDENTIFIER
        + (clientNoContextTakeover ? "; " + PARAMETER_CLIENT_NO_CONTEXT_TAKEOVER : "")
        + (serverNoContextTakeover ? "; " + PARAMETER_SERVER_NO_CONTEXT_TAKEOVER : "")
        + (clientMaxWindowBits != DEFAULT_CLIENT_MAX_WINDOW_BITS
            ? "; " + PARAMETER_CLIENT_MAX_WINDOW_BITS + "=" + clientMaxWindowBits
            : "")
        + (serverMaxWindowBits != DEFAULT_SERVER_MAX_WINDOW_BITS
            ? "; " + PARAMETER_SERVER_MAX_WINDOW_BITS + "=" + serverMaxWindowBits
            : "");
  }

  @Override
  public String getProvidedExtensionAsServer() {
    return EXTENSION_IDENTIFIER
        + (clientNoContextTakeover ? "; " + PARAMETER_CLIENT_NO_CONTEXT_TAKEOVER : "")
        + (serverNoContextTakeover ? "; " + PARAMETER_SERVER_NO_CONTEXT_TAKEOVER : "")
        + (clientMaxWindowBits != DEFAULT_CLIENT_MAX_WINDOW_BITS
            ? "; " + PARAMETER_CLIENT_MAX_WINDOW_BITS + "=" + clientMaxWindowBits
            : "")
        + (serverMaxWindowBits != DEFAULT_SERVER_MAX_WINDOW_BITS
            ? "; " + PARAMETER_SERVER_MAX_WINDOW_BITS + "=" + serverMaxWindowBits
            : "");
  }

  @Override
  public IExtension copyInstance() {
    PerMessageDeflateExtension clone =
        new PerMessageDeflateExtension(getCompressionLevel(), getMaxFragmentSize());
    clone.setClientNoContextTakeover(isClientNoContextTakeover());
    clone.setServerNoContextTakeover(isServerNoContextTakeover());
    clone.clientMaxWindowBits = clientMaxWindowBits;
    clone.serverMaxWindowBits = serverMaxWindowBits;
    clone.setThreshold(getThreshold());
    return clone;
  }

  @Override
  public void reset() {
    super.reset();
    isCompressing = false;
    isDecompressing = false;
    compressedBytes = 0;
    decompressedBytes = 0;
  }

  @Override
  public String toString() {
    return EXTENSION_COMMON_NAME;
  }
}
