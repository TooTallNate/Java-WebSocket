package org.java_websocket.exeptions;

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
		if( t instanceof InvalidDataException ) {
			this.closecode = ( (InvalidDataException) t ).getCloseCode();
		}
	}

	public InvalidDataException( int closecode , String s , Throwable t ) {
		super( s, t );
		if( t instanceof InvalidDataException ) {
			this.closecode = ( (InvalidDataException) t ).getCloseCode();
		}
	}

	public int getCloseCode() {
		return closecode;
	}

}
