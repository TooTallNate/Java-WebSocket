package net.tootallnate.websocket;

import java.util.HashMap;
import java.util.Iterator;

public interface Handshakedata {
	public String getHttpVersion();
	public String getMethod();
	public short getHttpStatus();
	public String getHttpStatusMessage();
	public String getResourceDescriptor();
	public Iterator<String> iterateHttpFields();
	public String getFieldValue( String name );
	public boolean hasFieldValue( String name );
	public byte[] getContent();
	// public boolean isComplete();
}
