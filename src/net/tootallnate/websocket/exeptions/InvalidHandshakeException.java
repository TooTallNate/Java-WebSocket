package net.tootallnate.websocket.exeptions;

import net.tootallnate.websocket.CloseFrame;

public class InvalidHandshakeException extends InvalidDataException {

	public InvalidHandshakeException() {
		super( CloseFrame.PROTOCOL_ERROR );
	}

	public InvalidHandshakeException( String arg0 , Throwable arg1 ) {
		super( CloseFrame.PROTOCOL_ERROR, arg0, arg1 );
	}

	public InvalidHandshakeException( String arg0 ) {
		super( CloseFrame.PROTOCOL_ERROR, arg0 );
	}

	public InvalidHandshakeException( Throwable arg0 ) {
		super( CloseFrame.PROTOCOL_ERROR, arg0 );
	}

}
