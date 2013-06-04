package org.java_websocket.handshake;

public class HandshakeImpl1Client extends HandshakedataImpl1 implements ClientHandshakeBuilder {
	private String resourceDescriptor = "*";

	public HandshakeImpl1Client() {
	}

	public void setResourceDescriptor( String resourceDescriptor ) throws IllegalArgumentException {
		if(resourceDescriptor==null)
			throw new IllegalArgumentException( "http resource descriptor must not be null" );
		this.resourceDescriptor = resourceDescriptor;
	}

	public String getResourceDescriptor() {
		return resourceDescriptor;
	}
}
