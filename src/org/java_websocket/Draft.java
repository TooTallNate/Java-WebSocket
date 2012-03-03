package org.java_websocket;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.java_websocket.WebSocket.Role;
import org.java_websocket.exeptions.InvalidDataException;
import org.java_websocket.exeptions.InvalidHandshakeException;
import org.java_websocket.exeptions.LimitExedeedException;

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
		HandshakedataImpl1 draft = new HandshakedataImpl1();

		String line = readStringLine( buf );
		if( line == null )
			throw new InvalidHandshakeException( "could not match http status line" );

		String[] firstLineTokens = line.split( " " );// eg. GET / HTTP/1.1

		if( role == Role.CLIENT && firstLineTokens.length == 4 ) {
			// translating/parsing the response from the SERVER
			draft.setHttpVersion( firstLineTokens[ 0 ] );
			draft.setHttpStatus( Short.parseShort( firstLineTokens[ 1 ] ) );
			draft.setHttpStatusMessage( firstLineTokens[ 2 ] + ' ' + firstLineTokens[ 3 ] );
		} else if( role == Role.SERVER && firstLineTokens.length == 3 ) {
			// translating/parsing the request from the CLIENT
			draft.setMethod( firstLineTokens[ 0 ] );
			draft.setResourceDescriptor( firstLineTokens[ 1 ] );
			draft.setHttpVersion( firstLineTokens[ 2 ] );
		} else {
			throw new InvalidHandshakeException( "could not match http status line" );
		}

		line = readStringLine( buf );
		while ( line != null && line.length() > 0 ) {
			String[] pair = line.split( ":", 2 );
			if( pair.length != 2 )
				throw new InvalidHandshakeException( "not an http header" );
			draft.put( pair[ 0 ], pair[ 1 ].replaceFirst( "^ +", "" ) );
			line = readStringLine( buf );
		}
		return draft;
	}

	public abstract HandshakeState acceptHandshakeAsClient( Handshakedata request, Handshakedata response ) throws InvalidHandshakeException;

	public abstract HandshakeState acceptHandshakeAsServer( Handshakedata handshakedata ) throws InvalidHandshakeException;

	protected boolean basicAccept( Handshakedata handshakedata ) {
		return handshakedata.getFieldValue( "Upgrade" ).equalsIgnoreCase( "websocket" ) && handshakedata.getFieldValue( "Connection" ).toLowerCase( Locale.ENGLISH ).contains( "upgrade" );
	}

	public abstract ByteBuffer createBinaryFrame( Framedata framedata ); // TODO Allow to send data on the base of an Iterator or InputStream

	public abstract List<Framedata> createFrames( byte[] binary, boolean mask );

	public abstract List<Framedata> createFrames( String text, boolean mask );

	public abstract void reset();

	public List<ByteBuffer> createHandshake( Handshakedata handshakedata, Role ownrole ) {
		return createHandshake( handshakedata, ownrole, true );
	}

	public List<ByteBuffer> createHandshake( Handshakedata handshakedata, Role ownrole, boolean withcontent ) {
		StringBuilder bui = new StringBuilder( 100 );
		if( ownrole == Role.CLIENT ) {
			bui.append( "GET " );
			bui.append( handshakedata.getResourceDescriptor() );
			bui.append( " HTTP/1.1" );
		} else if( ownrole == Role.SERVER ) {
			bui.append( "HTTP/1.1 101 " + handshakedata.getHttpStatusMessage() );
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

	public abstract HandshakeBuilder postProcessHandshakeRequestAsClient( HandshakeBuilder request ) throws InvalidHandshakeException;

	public abstract HandshakeBuilder postProcessHandshakeResponseAsServer( Handshakedata request, HandshakeBuilder response ) throws InvalidHandshakeException;

	public abstract List<Framedata> translateFrame( ByteBuffer buffer ) throws InvalidDataException;

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

	public abstract boolean hasCloseHandshake();

}
