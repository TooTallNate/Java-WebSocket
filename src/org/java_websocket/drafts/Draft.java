package org.java_websocket.drafts;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.java_websocket.WebSocket.Role;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.exceptions.LimitExedeedException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.HandshakeImpl1Client;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.util.Charsetfunctions;

/**
 * Base class for everything of a websocket specification which is not common such as the way the handshake is read or frames are transfered.
 **/
public abstract class Draft {

	public enum HandshakeState {
		/** Handshake matched this Draft successfully */
		MATCHED,
		/** Handshake is does not match this Draft */
		NOT_MATCHED,
		/** Handshake matches this Draft but is not complete */
		MATCHING
	}
	public enum CloseHandshakeType {
		NONE, ONEWAY, TWOWAY
	}

	public static int MAX_FAME_SIZE = 1000 * 1;
	public static int INITIAL_FAMESIZE = 64;

	public static final byte[] FLASH_POLICY_REQUEST = Charsetfunctions.utf8Bytes( "<policy-file-request/>\0" );

	/** In some cases the handshake will be parsed different depending on whether */
	protected Role role = null;

	public static ByteBuffer readLine( ByteBuffer buf ) {
		ByteBuffer sbuf = ByteBuffer.allocate( buf.remaining() );
		byte prev = '0';
		byte cur = '0';
		while ( buf.hasRemaining() ) {
			prev = cur;
			cur = buf.get();
			sbuf.put( cur );
			if( prev == (byte) '\r' && cur == (byte) '\n' ) {
				sbuf.limit( sbuf.position() - 2 );
				sbuf.position( 0 );
				return sbuf;

			}
		}
		// ensure that there wont be any bytes skipped
		buf.position( buf.position() - sbuf.position() );
		return null;
	}

	public static String readStringLine( ByteBuffer buf ) {
		ByteBuffer b = readLine( buf );
		return b == null ? null : Charsetfunctions.stringAscii( b.array(), 0, b.limit() );
	}

	public static HandshakeBuilder translateHandshakeHttp( ByteBuffer buf, Role role ) throws InvalidHandshakeException {
		HandshakeBuilder handshake;

		String line = readStringLine( buf );
		if( line == null )
			throw new InvalidHandshakeException( "could not match http status line" );

		String[] firstLineTokens = line.split( " ", 3 );// eg. HTTP/1.1 101 Switching the Protocols
		if( firstLineTokens.length != 3 ) {
			throw new InvalidHandshakeException();
		}

		if( role == Role.CLIENT ) {
		    // translating/parsing the response from the SERVER
			handshake = new HandshakeImpl1Server();
			ServerHandshakeBuilder serverhandshake = (ServerHandshakeBuilder) handshake;
			serverhandshake.setHttpStatus( Short.parseShort( firstLineTokens[ 1 ] ) );
			serverhandshake.setHttpStatusMessage( firstLineTokens[ 2 ] );
		} else {
		    // translating/parsing the request from the CLIENT
			ClientHandshakeBuilder clienthandshake = new HandshakeImpl1Client();
			clienthandshake.setResourceDescriptor( firstLineTokens[ 1 ] );
			handshake = clienthandshake;
		}

		line = readStringLine( buf );
		while ( line != null && line.length() > 0 ) {
			String[] pair = line.split( ":", 2 );
			if( pair.length != 2 )
				throw new InvalidHandshakeException( "not an http header" );
			handshake.put( pair[ 0 ], pair[ 1 ].replaceFirst( "^ +", "" ) );
			line = readStringLine( buf );
		}
		return handshake;
	}

	public abstract HandshakeState acceptHandshakeAsClient( ClientHandshake request, ServerHandshake response ) throws InvalidHandshakeException;

	public abstract HandshakeState acceptHandshakeAsServer( ClientHandshake handshakedata ) throws InvalidHandshakeException;

	protected boolean basicAccept( Handshakedata handshakedata ) {
		return handshakedata.getFieldValue( "Upgrade" ).equalsIgnoreCase( "websocket" ) && handshakedata.getFieldValue( "Connection" ).toLowerCase( Locale.ENGLISH ).contains( "upgrade" );
	}

	public abstract ByteBuffer createBinaryFrame( Framedata framedata ); // TODO Allow to send data on the base of an Iterator or InputStream

	public abstract List<Framedata> createFrames( ByteBuffer binary, boolean mask );

	public abstract List<Framedata> createFrames( String text, boolean mask );

	public abstract void reset();

	public List<ByteBuffer> createHandshake( Handshakedata handshakedata, Role ownrole ) {
		return createHandshake( handshakedata, ownrole, true );
	}

	public List<ByteBuffer> createHandshake( Handshakedata handshakedata, Role ownrole, boolean withcontent ) {
		StringBuilder bui = new StringBuilder( 100 );
		if( handshakedata instanceof ClientHandshake ) {
			bui.append( "GET " );
			bui.append( ( (ClientHandshake) handshakedata ).getResourceDescriptor() );
			bui.append( " HTTP/1.1" );
		} else if( handshakedata instanceof ServerHandshake ) {
			bui.append( "HTTP/1.1 101 " + ( (ServerHandshake) handshakedata ).getHttpStatusMessage() );
		} else {
			throw new RuntimeException( "unknow role" );
		}
		bui.append( "\r\n" );
		Iterator<String> it = handshakedata.iterateHttpFields();
		while ( it.hasNext() ) {
			String fieldname = it.next();
			String fieldvalue = handshakedata.getFieldValue( fieldname );
			bui.append( fieldname );
			bui.append( ": " );
			bui.append( fieldvalue );
			bui.append( "\r\n" );
		}
		bui.append( "\r\n" );
		byte[] httpheader = Charsetfunctions.asciiBytes( bui.toString() );

		byte[] content = withcontent ? handshakedata.getContent() : null;
		ByteBuffer bytebuffer = ByteBuffer.allocate( ( content == null ? 0 : content.length ) + httpheader.length );
		bytebuffer.put( httpheader );
		if( content != null )
			bytebuffer.put( content );
		bytebuffer.flip();
		return Collections.singletonList( bytebuffer );
	}

	public abstract ClientHandshakeBuilder postProcessHandshakeRequestAsClient( ClientHandshakeBuilder request ) throws InvalidHandshakeException;

	public abstract HandshakeBuilder postProcessHandshakeResponseAsServer( ClientHandshake request, ServerHandshakeBuilder response ) throws InvalidHandshakeException;

	public abstract List<Framedata> translateFrame( ByteBuffer buffer ) throws InvalidDataException;

	public abstract CloseHandshakeType getCloseHandshakeType();

	public Handshakedata translateHandshake( ByteBuffer buf ) throws InvalidHandshakeException {
		return translateHandshakeHttp( buf, role );
	}

	public int checkAlloc( int bytecount ) throws LimitExedeedException , InvalidDataException {
		if( bytecount < 0 )
			throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "Negative count" );
		return bytecount;
	}

	public void setParseMode( Role role ) {
		this.role = role;
	}

}
