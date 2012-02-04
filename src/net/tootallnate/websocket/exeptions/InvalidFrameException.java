package net.tootallnate.websocket.exeptions;

import net.tootallnate.websocket.CloseFrame;

public class InvalidFrameException extends InvalidDataException {

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
