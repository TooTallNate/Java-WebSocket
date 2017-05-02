package org.java_websocket.framing;

import java.nio.ByteBuffer;

import org.java_websocket.exceptions.InvalidDataException;

public interface FrameBuilder extends Framedata {

	/**
	 * Setter for fin to indicate if this frame is the final fragment
	 * @param fin true, if this frame is the final fragment
	 */
	void setFin( boolean fin );

	/**
	 * Setter for the opcode to use, how the provided "Payload data" should be interpreted
	 * @param optcode the interpretation as a Opcode
	 */
	void setOptcode( Opcode optcode );

	/**
	 * Setter for the "Payload data" to use in this frame
	 * @param payload the "Payload data"
	 * @throws InvalidDataException indicates that the provided "Payload data" is not a valid data
	 */
	void setPayload( ByteBuffer payload ) throws InvalidDataException;

	/**
	 * Setter for the transfermask to use in this frame
	 * @param transferemasked true, "Payload data" is masked
	 */
	void setTransferemasked( boolean transferemasked );

}