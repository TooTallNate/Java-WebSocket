package org.java_websocket;

public interface ClientHandshakeBuilder extends HandshakeBuilder, ClientHandshake {
	public void setResourceDescriptor( String resourcedescriptor );
}
