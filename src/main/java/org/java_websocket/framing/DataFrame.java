package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;

/**
 * Created by Admin on 23.05.2017.
 */
public abstract class DataFrame extends FramedataImpl1 {

	public DataFrame(Opcode opcode) {
		super(opcode);
	}
	@Override
	public void isValid() throws InvalidDataException
	{
      //Nothing specific to check
	}
}
