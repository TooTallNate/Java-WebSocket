package org.java_websocket.framing;

import java.nio.ByteBuffer;

import org.java_websocket.exceptions.InvalidFrameException;

public interface Framedata {
	/**
	 * Enum which contains the different valid opcodes
	 */
	enum Opcode {
		CONTINUOUS, TEXT, BINARY, PING, PONG, CLOSING
		// more to come
	}

	/**
	 * Indicates that this is the final fragment in a message.  The first fragment MAY also be the final fragment.
	 * @return true, if this frame is the final fragment
	 */
	boolean isFin();

	/**
	 * Defines whether the "Payload data" is masked.
	 * @return true, "Payload data" is masked
	 */
	boolean getTransfereMasked();

	/**
	 * Defines the interpretation of the "Payload data".
	 * @return the interpretation as a Opcode
	 */
	Opcode getOpcode();

	/**
	 * The "Payload data" which was sent in this frame
	 * @return the "Payload data" as ByteBuffer
	 */
	ByteBuffer getPayloadData();// TODO the separation of the application data and the extension data is yet to be done

	/**
	 * Appends an additional frame to the current frame
	 *
	 * This methods does not override the opcode, but does override the fin
	 * @param nextframe the additional frame
	 */
	void append( Framedata nextframe );
}
