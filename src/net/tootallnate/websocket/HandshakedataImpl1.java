package net.tootallnate.websocket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HandshakedataImpl1 implements HandshakeBuilder {
	
	private String resourcedescriptor;
	private byte[] content;
	private Map<String,String> map;
	
	public HandshakedataImpl1() {
		map = new HashMap<String,String>();
	}

	@Override
	public String getResourceDescriptor( ) {
		return resourcedescriptor == null ? "" : resourcedescriptor ;
	}

	@Override
	public Iterator<String> iterateHttpFields( ) {
		return map.keySet ().iterator ();
	}

	@Override
	public String getFieldValue( String name ) {
		String s = map.get ( name );
		if( s == null ){
			return "";
		}
		return s;
	}

	@Override
	public byte[] getContent( ) {
		return content;
	}
	
	@Override
	public void setContent( byte[] content ) {
		this.content = content;
	}
	
	@Override
	public void setResourceDescriptor( String resourcedescriptor ) {
		this.resourcedescriptor = resourcedescriptor;
	}
	
	@Override
	public void put( String name, String value ){
		map.put ( name , value );
	}

}
