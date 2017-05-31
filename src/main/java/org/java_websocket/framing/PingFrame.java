package org.java_websocket.framing;

/**
 * Class to represent a ping frame
 */
public class PingFrame extends ControlFrame {

	/**
	 * constructor which sets the opcode of this frame to ping
	 */
	public PingFrame() {
		super(Opcode.PING);
	}
}
