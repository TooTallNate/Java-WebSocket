package net.tootallnate.websocket;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import net.tootallnate.websocket.WebSocket.Role;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;

public abstract class Draft{
	
	/**
	* The WebSocket protocol expects UTF-8 encoded bytes.
	*/ 
	public final static Charset  UTF8_CHARSET = Charset.forName ( "UTF-8" );
	
	public abstract boolean acceptHandshakeAsServer( Handshakedata handshakedata ) throws InvalidHandshakeException;
	public abstract boolean acceptHandshakeAsClient( Handshakedata request , Handshakedata response ) throws InvalidHandshakeException;
	public abstract List<Framedata> translateFrame( ByteBuffer buffer, int read );
	public abstract ByteBuffer createBinaryFrame( Framedata framedata ); //TODO Allow to send data on the base of an Iterator or InputStream 
	public abstract List<Framedata> createFrames(  String text , boolean mask );
	public abstract List<Framedata> createFrames(  byte[] binary , boolean mask );
	
	public HandshakeBuilder postProcessHandshakeRequestAsClient( HandshakeBuilder request ){
		request.put ( "Upgrade" , "websocket" );
		request.put ( "Connection" , "Upgrade" );
		return request;
	}
	
	public HandshakeBuilder postProcessHandshakeResponseAsServer(  Handshakedata request , HandshakeBuilder response ) throws InvalidHandshakeException{
		//sb.append ( "HTTP/1.1 101 Switching Protocols\r\n" );
		response.put ( "Upgrade" , "websocket" );
		response.put ( "Connection" , /*"Upgrade"*/request.getFieldValue ( "Connection" ) ); //to respond a Connection keep alives
		return response;
	}
	
	public /*static*/ ByteBuffer createHandshake( Handshakedata handshakedata , Role ownrole ){
		StringBuilder bui = new StringBuilder ( 100 );
		if( ownrole == Role.CLIENT){
			bui.append ( "GET " );
			bui.append ( handshakedata.getResourceDescriptor () );
			bui.append ( " HTTP/1.1" );
		}
		else if( ownrole == Role.SERVER ){
			bui.append ( "HTTP/1.1 101 Switching Protocols" );
		}
		else{
			throw new RuntimeException ( "unknow role" );
		}
		bui.append ( "\r\n" );
		Iterator<String> it = handshakedata.iterateHttpFields ();
		while( it.hasNext () ){
			String fieldname = it.next ();
			String fieldvalue = handshakedata.getFieldValue ( fieldname );
			bui.append ( fieldname );
			bui.append ( ": ");
			bui.append ( fieldvalue );
			bui.append ( "\r\n" );
		}
		byte[] httpheader = bui.toString ().getBytes ( UTF8_CHARSET );
		byte[] content = handshakedata.getContent ();
		ByteBuffer bytebuffer = ByteBuffer.allocate ( ( content==null ? 0 : content.length ) + httpheader.length + 2 );
		bytebuffer.put ( httpheader );
		if( content!=null )
			bytebuffer.put ( content );
		bytebuffer.put (  ( byte )'\r' );
		bytebuffer.put (  ( byte )'\n' );
		return bytebuffer;
		
	}
	public static Handshakedata translateHandshake( byte[] buffer, int readcount ){
		HandshakedataImpl1 draft = new HandshakedataImpl1();
		
		ByteBuffer message = ByteBuffer.allocate ( readcount );
		message.put ( buffer , 0 , readcount );
		byte[] lines = message.array ();
		int previndex = 0;
		int index = findNewLine ( lines , previndex );
		if ( index == -1 )
			return null;
		String line = new String ( lines , previndex , index - previndex );
		//TODO Care about resources here like: GET /chat HTTP/1.1 
		//if ( line.startsWith ( "GET" ) == false ) 
		//if ( line.startsWith ( "HTTP" ) == false ) 
		
		previndex = index + 2;
		index = findNewLine ( lines , previndex );
		
		while ( index != -1 ) {
			int length = index - previndex;
			line = new String ( lines , previndex , length );
			if ( index != previndex ) {
				String[] pair = line.split ( ":" , 2 );
				if ( pair.length != 2 )
					draft.setContent ( ByteBuffer.allocate ( length ).put ( lines, previndex,length).array () ); //this approach will also accept suspicious looking handshakes...
				draft.put ( pair[ 0 ] , pair[ 1 ] );
			}
			previndex = index + 2;
			index = findNewLine ( lines , previndex );
		}
		return draft;
	}
	
	public static int findNewLine( byte[] arr , int offset ) {
		int len = arr.length - 1;
		  for ( int i = offset ; i < len ; i++ ) 
			if( arr[i] == (byte)'\r' && arr[ i + 1 ] == (byte)'\n' )
				return i;
		return -1;
	}
}