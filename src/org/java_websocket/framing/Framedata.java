package org.java_websocket.framing;

import org.java_websocket.exeptions.InvalidFrameException;

public interface Framedata {
	public enum Opcode {
		CONTINIOUS , TEXT , BINARY , PING , PONG , CLOSING
		// more to come
	}
	public boolean isFin();
	public boolean getTransfereMasked();
	public Opcode getOpcode();
	public byte[] getPayloadData();// TODO the separation of the application data and the extension data is yet to be done
	public abstract void append( Framedata nextframe ) throws InvalidFrameException;
}
