package org.java_websocket.framing;

/**
 * Created by Admin on 23.05.2017.
 */
public class PingFrame extends ControlFrame {

	public PingFrame() {
		super(Opcode.PING);
	}
}
