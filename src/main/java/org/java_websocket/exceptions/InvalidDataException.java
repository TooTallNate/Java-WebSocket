package org.java_websocket.exceptions;

public class InvalidDataException extends Exception {
	/**
	 * Serializable
	 */
	private static final long serialVersionUID = 3731842424390998726L;
	
	private int closecode;
	
	public InvalidDataException( int closecode ) {
		this.closecode = closecode;
	}

	public InvalidDataException( int closecode , String s ) {
		super( s );
		this.closecode = closecode;
	}

	public InvalidDataException( int closecode , Throwable t ) {
		super( t );
		this.closecode = closecode;
	}

	public InvalidDataException( int closecode , String s , Throwable t ) {
		super( s, t );
		this.closecode = closecode;
	}

	public int getCloseCode() {
		return closecode;
	}

}
