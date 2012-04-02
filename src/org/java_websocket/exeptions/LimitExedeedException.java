package org.java_websocket.exeptions;

import org.java_websocket.framing.CloseFrame;

public class LimitExedeedException extends InvalidDataException {

	/**
	 * Serializable
	 */
	private static final long serialVersionUID = 6908339749836826785L;

	public LimitExedeedException() {
		super( CloseFrame.TOOBIG );
	}

	public LimitExedeedException( String s ) {
		super( CloseFrame.TOOBIG, s );
	}

}
