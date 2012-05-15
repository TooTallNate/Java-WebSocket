package org.java_websocket.exceptions;

public class IncompleteHandshakeException extends RuntimeException {

	/**
	 * Serializable
	 */
	private static final long serialVersionUID = 7906596804233893092L;

	public IncompleteHandshakeException() {
		super();
	}

	public IncompleteHandshakeException( String message , Throwable cause ) {
		super( message, cause );
	}

	public IncompleteHandshakeException( String message ) {
		super( message );
	}

	public IncompleteHandshakeException( Throwable cause ) {
		super( cause );
	}

}
