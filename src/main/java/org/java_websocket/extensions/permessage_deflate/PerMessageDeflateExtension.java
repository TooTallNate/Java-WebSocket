package org.java_websocket.extensions.permessage_deflate;

import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.extensions.CompressionExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class PerMessageDeflateExtension extends CompressionExtension {

    // Name of the extension as registered by IETF https://tools.ietf.org/html/rfc7692#section-9.
    private static final String EXTENSION_REGISTERED_NAME = "permessage-deflate";

    // Below values are defined for convenience. They are not used in the compression/decompression phase.
    // They may be needed during the extension-negotiation offer in the future.
    private static final String SERVER_NO_CONTEXT_TAKEOVER = "server_no_context_takeover";
    private static final String CLIENT_NO_CONTEXT_TAKEOVER = "client_no_context_takeover";
    private static final String SERVER_MAX_WINDOW_BITS = "server_max_window_bits";
    private static final String CLIENT_MAX_WINDOW_BITS = "client_max_window_bits";
    private static final boolean serverNoContextTakeover = true;
    private static final boolean clientNoContextTakeover = true;
    private static final int serverMaxWindowBits = 1 << 15;
    private static final int clientMaxWindowBits = 1 << 15;

    private static final byte[] TAIL_BYTES = {0x00, 0x00, (byte)0xFF, (byte)0xFF};
    private static final int BUFFER_SIZE = 1 << 10;

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
        if(!(inputFrame instanceof DataFrame))
            return;

        // RSV1 bit must be set only for the first frame.
        if(inputFrame.getOpcode() == Opcode.CONTINUOUS && inputFrame.isRSV1())
            throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "RSV1 bit can only be set for the first frame.");

        // Decompressed output buffer.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Inflater inflater = new Inflater(true);
        try {
            decompress(inflater, inputFrame.getPayloadData().array(), output);
            // Decompress 4 bytes of 0x00 0x00 0xff 0xff as if they were appended to the end of message.
            if(inputFrame.isFin())
                decompress(inflater, TAIL_BYTES, output);
        } catch (DataFormatException e) {
            throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Given data format couldn't be decompressed.");
        }finally {
            inflater.end();
        }

        // Set frames payload to the new decompressed data.
        ((FramedataImpl1) inputFrame).setPayload(ByteBuffer.wrap(output.toByteArray()));
    }

    private void decompress(Inflater inflater, byte[] data, ByteArrayOutputStream outputBuffer) throws DataFormatException{
        inflater.setInput(data);
        byte[] buffer = new byte[BUFFER_SIZE];

        int bytesInflated;
        while((bytesInflated = inflater.inflate(buffer)) > 0){
            outputBuffer.write(buffer, 0, bytesInflated);
        }
    }

    @Override
    public void encodeFrame(Framedata inputFrame) {
        // Only DataFrames can be decompressed.
        if(!(inputFrame instanceof DataFrame))
            return;

        // Only the first frame's RSV1 must be set.
        if(!(inputFrame instanceof ContinuousFrame))
            ((DataFrame) inputFrame).setRSV1(true);

        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(inputFrame.getPayloadData().array());
        deflater.finish();

        // Compressed output buffer.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // Temporary buffer to hold compressed output.
        byte[] buffer = new byte[1024];
        int bytesCompressed;
        while((bytesCompressed = deflater.deflate(buffer)) > 0) {
            output.write(buffer, 0, bytesCompressed);
        }
        deflater.end();

        byte outputBytes[] = output.toByteArray();
        int outputLength = outputBytes.length;
        /*
            https://tools.ietf.org/html/rfc7692#section-7.2.1 states that if the final fragment's compressed
                payload ends with 0x00 0x00 0xff 0xff, they should be removed.
            To simulate removal, we just pass 4 bytes less to the new payload
                if the frame is final and outputBytes ends with 0x00 0x00 0xff 0xff.
         */
        if(inputFrame.isFin() && endsWithTail(outputBytes))
            outputLength -= TAIL_BYTES.length;

        // Set frames payload to the new compressed data.
        ((FramedataImpl1) inputFrame).setPayload(ByteBuffer.wrap(outputBytes, 0, outputLength));
    }

    private boolean endsWithTail(byte[] data){
        if(data.length < 4)
            return false;

        int length = data.length;
        for(int i = 0; i <= TAIL_BYTES.length; i--){
            if(TAIL_BYTES[i] != data[length - TAIL_BYTES.length + i])
                return false;
        }

        return true;
    }

    @Override
    public boolean acceptProvidedExtensionAsServer(String inputExtension) {
        String[] requestedExtensions = inputExtension.split(",");
        for(String extension : requestedExtensions)
            if(EXTENSION_REGISTERED_NAME.equalsIgnoreCase(extension.trim()))
                return true;

        return false;
    }

    @Override
    public boolean acceptProvidedExtensionAsClient(String inputExtension) {
        String[] requestedExtensions = inputExtension.split(",");
        for(String extension : requestedExtensions)
            if(EXTENSION_REGISTERED_NAME.equalsIgnoreCase(extension.trim()))
                return true;

        return false;
    }

    @Override
    public String getProvidedExtensionAsClient() {
        return EXTENSION_REGISTERED_NAME;
    }

    @Override
    public String getProvidedExtensionAsServer() {
        return EXTENSION_REGISTERED_NAME;
    }

    @Override
    public IExtension copyInstance() {
        return new PerMessageDeflateExtension();
    }

    /**
     * This extension requires the RSV1 bit to be set only for the first frame.
     * If the frame is type is CONTINUOUS, RSV1 bit must be unset.
     */
    @Override
    public void isFrameValid(Framedata inputFrame) throws InvalidDataException {
        if((inputFrame instanceof TextFrame || inputFrame instanceof BinaryFrame) && !inputFrame.isRSV1())
            throw new InvalidFrameException("RSV1 bit must be set for DataFrames.");
        if((inputFrame instanceof ContinuousFrame) && (inputFrame.isRSV1() || inputFrame.isRSV2() || inputFrame.isRSV3()))
            throw new InvalidFrameException( "bad rsv RSV1: " + inputFrame.isRSV1() + " RSV2: " + inputFrame.isRSV2() + " RSV3: " + inputFrame.isRSV3() );
        super.isFrameValid(inputFrame);
    }

    @Override
    public String toString() {
        return "PerMessageDeflateExtension";
    }

}