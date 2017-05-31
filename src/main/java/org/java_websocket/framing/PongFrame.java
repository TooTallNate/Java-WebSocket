package org.java_websocket.framing;

/**
 * Class to represent a pong frame
 */
public class PongFrame extends ControlFrame {

    /**
     * constructor which sets the opcode of this frame to pong
     */
    public PongFrame() {
        super(Opcode.PONG);
    }

    /**
     * constructor which sets the opcode of this frame to ping copying over the payload of the ping
     *
     * @param pingFrame the PingFrame which payload is to copy
     */
    public PongFrame(PingFrame pingFrame) {
        super(Opcode.PONG);
        setPayload(pingFrame.getPayloadData());
    }
}
