package net.tootallnate.websocket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class HandshakedataImpl1 implements HandshakeBuilder {
	
	private String httpstatusmessage;
	private String resourcedescriptor;
	private byte[] content;
	private LinkedHashMap<String,String> map;
	
	public HandshakedataImpl1() {
		map = new LinkedHashMap<String,String>();
	}

	public HandshakedataImpl1( Handshakedata h ) {
		httpstatusmessage = h.getHttpStatusMessage();
		resourcedescriptor = h.getResourceDescriptor();
		content = h.getContent();
		map = new LinkedHashMap<String,String>( );
		Iterator<String> it = h.iterateHttpFields();
		while ( it.hasNext() ) {
			String key = ( String ) it.next();
			map.put( key , h.getFieldValue( key ) );
		}
	}

	@Override
	public String getResourceDescriptor( ) {
		return resourcedescriptor == null ? "" : resourcedescriptor ;
	}

	@Override
	public Iterator<String> iterateHttpFields( ) {
		return Collections.unmodifiableSet( map.keySet () ).iterator ();//Safety first
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

	@Override
	public boolean hasFieldValue( String name ) {
		return map.containsKey( name );
	}

	@Override
	public String getHttpStatusMessage( ) {
		return httpstatusmessage;
	}

	@Override
	public void setHttpStatusMessage( String message ) {
		this.httpstatusmessage=message;
		
	}

}
