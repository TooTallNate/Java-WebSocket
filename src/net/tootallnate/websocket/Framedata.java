package net.tootallnate.websocket;

import java.util.HashMap;
import net.tootallnate.websocket.exeptions.InvalidFrameException;

public interface Framedata {
	enum Opcode{
		CONTINIOUS,
		TEXT,
		BINARY,
		PING,
		PONG,
		CLOSING
		//more to come
	}
	public boolean isFin();
	public boolean getTransfereMasked();
	public Opcode getOpcode();
	public byte[] getPayloadData();
	public abstract void append( Framedata nextframe ) throws InvalidFrameException; 
}
