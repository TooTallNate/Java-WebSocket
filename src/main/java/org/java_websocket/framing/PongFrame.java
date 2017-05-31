package org.java_websocket.framing;

/**
 * Created by Admin on 23.05.2017.
 */
public class PongFrame extends ControlFrame {

	public PongFrame() {
		super(Opcode.PONG);
	}

	public PongFrame(PingFrame pingFrame) {
		super(Opcode.PONG);
		setPayload( pingFrame.getPayloadData() );
	}
}
