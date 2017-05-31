package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.util.Charsetfunctions;

/**
 * Created by Admin on 23.05.2017.
 */
public class TextFrame extends DataFrame {
	public TextFrame() {
		super( Opcode.TEXT );
	}

	@Override
	public void isValid() throws InvalidDataException {
		super.isValid();

	}
}
