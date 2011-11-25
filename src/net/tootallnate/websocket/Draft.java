package net.tootallnate.websocket;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.tootallnate.websocket.WebSocket.Role;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;

public abstract class Draft{
	
	public enum HandshakeState{ 
		/**Handshake matched this Draft successfully*/
		MATCHED, 
		/**Handshake is does not match this Draft*/
		NOT_MATCHED, 
		/**Handshake matches this Draft but is not complete*/
		MATCHING
		//,/**Can not yet say anything*/
		//PENDING  not yet in use
	}
	/**
	* The WebSocket protocol expects UTF-8 encoded bytes.
	*/ 
	public static final  Charset  UTF8_CHARSET = Charset.forName ( "UTF-8" );
	
	private static final byte[] FLASH_POLICY_REQUEST = "<policy-file-request/>".getBytes( UTF8_CHARSET );
	
	/**will return the index of the first \r\n or the index off the last element in arr*/
	public static int findNewLine( byte[] arr , int offset ) {
		int len = arr.length - 1;
		int i = offset;
		  for (  ; i < len ; i++ ) 
			if( arr[i] == (byte)'\r' && arr[ i + 1 ] == (byte)'\n' )
				return i;
		return i;//the end of input will be handled like newline
	}
	public static boolean isFlashEdgeCase( byte[] request , int requestsize ){
		for( int i = 0 ; i < requestsize && i < FLASH_POLICY_REQUEST.length ; i++ ){
			if( FLASH_POLICY_REQUEST[i] != request[i] ){
				return false;
			}
		}
		return requestsize >= FLASH_POLICY_REQUEST.length;
	}
	public static HandshakeBuilder translateHandshakeHttp( byte[] buffer, int readcount ) throws InvalidHandshakeException{
		HandshakedataImpl1 draft = new HandshakedataImpl1();
		
		ByteBuffer message = ByteBuffer.allocate ( readcount );
		message.put ( buffer , 0 , readcount );
		byte[] lines = message.array ();
		int previndex = 0;
		int index = findNewLine ( lines , previndex );
		if ( index == lines.length )
			throw new InvalidHandshakeException("not an http header");;
		String line = new String ( lines , previndex , index - previndex );
		String[] firstLineTokens = line.split(" ");
		//if( firstLineTokens.length != 3)
		String path = firstLineTokens[1];
		draft.setResourceDescriptor( path );
		//TODO Care about resources here like: GET /chat HTTP/1.1 
		//if ( line.startsWith ( "GET" ) == false ) 
		//if ( line.startsWith ( "HTTP" ) == false ) 
		
		previndex = index + 2;
		index = findNewLine ( lines , previndex );
		int length = index - previndex;
		while ( length != 0 ) {
			line = new String ( lines , previndex , length );
			if ( index != previndex ) {
				String[] pair = line.split ( ":" , 2 );
				if ( pair.length != 2 )
					throw new InvalidHandshakeException("not an http header");
				draft.put ( pair[ 0 ] , pair[ 1 ].replaceFirst( "^ +" , "" ) );
			}
			previndex = index + 2;
			index = findNewLine ( lines , previndex );
			length = index - previndex;
		}
		previndex = index + 2;
		length = lines.length - previndex;
		draft.setContent ( ByteBuffer.allocate ( length ).put ( lines, previndex , length ).array () );
		return draft;
	}
	public abstract HandshakeState acceptHandshakeAsClient( Handshakedata request , Handshakedata response ) throws InvalidHandshakeException;
	public abstract HandshakeState acceptHandshakeAsServer( Handshakedata handshakedata ) throws InvalidHandshakeException;
	public abstract ByteBuffer createBinaryFrame( Framedata framedata ); //TODO Allow to send data on the base of an Iterator or InputStream 
	public abstract List<Framedata> createFrames(  byte[] binary , boolean mask );
	public abstract List<Framedata> createFrames(  String text , boolean mask );
	
	public List<ByteBuffer> createHandshake( Handshakedata handshakedata , Role ownrole ){
		return createHandshake( handshakedata , ownrole , true );
	}
	
	public List<ByteBuffer> createHandshake( Handshakedata handshakedata , Role ownrole , boolean withcontent ){
		StringBuilder bui = new StringBuilder ( 100 );
		if( ownrole == Role.CLIENT){
			bui.append ( "GET " );
			bui.append ( handshakedata.getResourceDescriptor () );
			bui.append ( " HTTP/1.1" );
		}
		else if( ownrole == Role.SERVER ){
			bui.append ( "HTTP/1.1 101 "+handshakedata.getHttpStatusMessage() );
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
		bui.append ( "\r\n" );
		byte[] httpheader = bui.toString ().getBytes ( UTF8_CHARSET );
		byte[] content = withcontent ? handshakedata.getContent() : null;
		ByteBuffer bytebuffer = ByteBuffer.allocate ( ( content==null ? 0 : content.length ) + httpheader.length );
		bytebuffer.put ( httpheader );
		if( content!=null )
			bytebuffer.put ( content );
		return Collections.singletonList( bytebuffer );
	}
	
	public abstract HandshakeBuilder postProcessHandshakeRequestAsClient( HandshakeBuilder request ) throws InvalidHandshakeException;
	
	public abstract HandshakeBuilder postProcessHandshakeResponseAsServer(  Handshakedata request , HandshakeBuilder response ) throws InvalidHandshakeException;

	public abstract List<Framedata> translateFrame( ByteBuffer buffer, int read );
	
	public Handshakedata translateHandshake( byte[] buffer, int readcount ) throws InvalidHandshakeException{
		return translateHandshakeHttp( buffer , readcount );
	}

}