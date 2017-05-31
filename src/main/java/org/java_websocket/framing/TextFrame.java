package org.java_websocket.framing;

/**
 * Class to represent a text frames
 */
public class TextFrame extends DataFrame {

    /**
     * constructor which sets the opcode of this frame to text
     */
    public TextFrame() {
        super(Opcode.TEXT);
    }
}
