package org.java_websocket.framing;

import java.nio.ByteBuffer;

import org.java_websocket.exceptions.InvalidFrameException;

public interface Framedata {
	/**
	 * Enum which contains the different valid opcodes
	 */
	public enum Opcode {
		CONTINUOUS, TEXT, BINARY, PING, PONG, CLOSING
		// more to come
	}
	public boolean isFin();
	public boolean getTransfereMasked();
	public Opcode getOpcode();
	public ByteBuffer getPayloadData();// TODO the separation of the application data and the extension data is yet to be done
	public abstract void append( Framedata nextframe ) throws InvalidFrameException;
}
