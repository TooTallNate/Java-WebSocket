package net.tootallnate.websocket.exeptions;

public class LimitExedeedException extends InvalidDataException {

	public LimitExedeedException() {
	}

	public LimitExedeedException( String s ) {
		super( s );
	}

	public LimitExedeedException( Throwable t ) {
		super( t );
	}

	public LimitExedeedException( String s , Throwable t ) {
		super( s, t );
	}

}
