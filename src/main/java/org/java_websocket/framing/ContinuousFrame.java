package org.java_websocket.framing;

/**
 * Created by Admin on 23.05.2017.
 */
public class ContinuousFrame extends DataFrame {
	public ContinuousFrame() {
		super( Opcode.CONTINUOUS );
	}
}
