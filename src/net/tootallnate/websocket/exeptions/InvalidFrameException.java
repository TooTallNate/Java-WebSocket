package net.tootallnate.websocket.exeptions;

public class InvalidFrameException extends InvalidDataException {

	public InvalidFrameException() {
	}

	public InvalidFrameException( String arg0 ) {
		super ( arg0 );
	}

	public InvalidFrameException( Throwable arg0 ) {
		super ( arg0 );
	}

	public InvalidFrameException( String arg0 , Throwable arg1 ) {
		super ( arg0 , arg1 );
	}
}
