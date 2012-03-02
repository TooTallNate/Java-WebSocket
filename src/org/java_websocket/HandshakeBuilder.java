package org.java_websocket;

public interface HandshakeBuilder extends Handshakedata {

	public abstract void setContent( byte[] content );

	public abstract void setResourceDescriptor( String resourcedescriptor );

	public abstract void setHttpStatusMessage( String message );

	public abstract void put( String name, String value );

}