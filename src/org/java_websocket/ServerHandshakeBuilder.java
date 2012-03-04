package org.java_websocket;

public interface ServerHandshakeBuilder extends HandshakeBuilder, ServerHandshake {
	public void setHttpStatus( short status );
	public void setHttpStatusMessage( String message );
}
