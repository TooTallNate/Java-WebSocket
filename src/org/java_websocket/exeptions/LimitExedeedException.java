package net.tootallnate.websocket.exeptions;

import net.tootallnate.websocket.CloseFrame;

public class LimitExedeedException extends InvalidDataException {

	public LimitExedeedException() {
		super( CloseFrame.TOOBIG );
	}

	public LimitExedeedException( String s ) {
		super( CloseFrame.TOOBIG, s );
	}

}
