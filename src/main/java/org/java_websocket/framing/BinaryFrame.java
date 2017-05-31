package org.java_websocket.framing;

/**
 * Created by Admin on 23.05.2017.
 */
public class BinaryFrame extends DataFrame {
	public BinaryFrame() {
		super( Opcode.BINARY );
	}
}
