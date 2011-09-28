package net.tootallnate.websocket.exeptions;

public class InvalidDataException extends Exception {

	public InvalidDataException() {
	}

	public InvalidDataException( String arg0 ) {
		super ( arg0 );
	}

	public InvalidDataException( Throwable arg0 ) {
		super ( arg0 );
	}

	public InvalidDataException( String arg0 , Throwable arg1 ) {
		super ( arg0 , arg1 );
	}

}
