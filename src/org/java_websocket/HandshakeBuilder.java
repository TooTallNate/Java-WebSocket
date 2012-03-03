package org.java_websocket;

public interface HandshakeBuilder extends Handshakedata {
	public abstract void setHttpVersion( String version );
	public abstract void setMethod( String method );
	public abstract void setContent( byte[] content );
	public abstract void setResourceDescriptor( String resourcedescriptor );
	public abstract void setHttpStatus( short status );
	public abstract void setHttpStatusMessage( String message );
	public abstract void put( String name, String value );
}
