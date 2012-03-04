package org.java_websocket.exeptions;

import org.java_websocket.framing.CloseFrame;

public class LimitExedeedException extends InvalidDataException {

	public LimitExedeedException() {
		super( CloseFrame.TOOBIG );
	}

	public LimitExedeedException( String s ) {
		super( CloseFrame.TOOBIG, s );
	}

}
