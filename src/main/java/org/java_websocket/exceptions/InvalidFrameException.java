package org.java_websocket.exceptions;

import org.java_websocket.framing.CloseFrame;

public class InvalidFrameException extends InvalidDataException {

	/**
	 * Serializable
	 */
	private static final long serialVersionUID = -9016496369828887591L;

	public InvalidFrameException() {
		super( CloseFrame.PROTOCOL_ERROR );
	}

	public InvalidFrameException( String arg0 ) {
		super( CloseFrame.PROTOCOL_ERROR, arg0 );
	}

	public InvalidFrameException( Throwable arg0 ) {
		super( CloseFrame.PROTOCOL_ERROR, arg0 );
	}

	public InvalidFrameException( String arg0 , Throwable arg1 ) {
		super( CloseFrame.PROTOCOL_ERROR, arg0, arg1 );
	}
}
