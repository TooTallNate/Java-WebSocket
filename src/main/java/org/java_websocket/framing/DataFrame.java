package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;

/**
 * Absstract class to represent data frames
 */
public abstract class DataFrame extends FramedataImpl1 {

	/**
	 * Class to represent a data frame
	 * @param opcode the opcode to use
	 */
	public DataFrame(Opcode opcode) {
		super(opcode);
	}

	@Override
	public void isValid() throws InvalidDataException
	{
      //Nothing specific to check
	}
}
