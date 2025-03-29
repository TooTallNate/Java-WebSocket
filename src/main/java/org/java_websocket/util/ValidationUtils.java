package org.java_websocket.util;

import java.nio.ByteBuffer;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseCodeConstants;

public final class ValidationUtils {
    private ValidationUtils() {}

    /**
     * Validates and decodes UTF-8 from ByteBuffer
     * @param payload Buffer containing UTF-8 bytes
     * @param startPos Starting position (after close code)
     * @param mark Original position to reset to
     * @return Decoded string
     * @throws InvalidDataException if invalid UTF-8
     */
    public static String decodeUtf8(ByteBuffer payload, int startPos, int mark)
            throws InvalidDataException {
        try {
            payload.position(startPos);
            return Charsetfunctions.stringUtf8(payload);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(CloseCodeConstants.NO_UTF8);
        } finally {
            payload.position(mark);
        }
    }
}