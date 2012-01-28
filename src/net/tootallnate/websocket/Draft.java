package net.tootallnate.websocket;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.tootallnate.websocket.WebSocket.Role;
import net.tootallnate.websocket.exeptions.InvalidDataException;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;
import net.tootallnate.websocket.exeptions.LimitExedeedException;

public abstract class Draft {

	public enum HandshakeState {
		/** Handshake matched this Draft successfully */
		MATCHED ,
		/** Handshake is does not match this Draft */
		NOT_MATCHED ,
		/** Handshake matches this Draft but is not complete */
		MATCHING
		// ,/**Can not yet say anything*/
		// PENDING not yet in use
	}

	private static final byte[] FLASH_POLICY_REQUEST = Charsetfunctions.utf8Bytes( "<policy-file-request/>" );

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
		//ensure that there wont be any bytes skipped
		buf.position( buf.position() - sbuf.position() );
		return null;
	}

	public static String readStringLine( ByteBuffer buf ) {
		ByteBuffer b = readLine( buf );
		return b == null ? null : Charsetfunctions.stingAscii( b.array(), 0, b.limit() );
	}

	public static HandshakeBuilder translateHandshakeHttp( ByteBuffer buf ) throws InvalidHandshakeException {
		HandshakedataImpl1 draft = new HandshakedataImpl1();

		String line = readStringLine( buf );
		if( line == null )
			throw new InvalidHandshakeException( "could not match http status line" );

		String[] firstLineTokens = line.split( " " );// eg. GET / HTTP/1.1
		if( firstLineTokens.length < 3 ) {
			throw new InvalidHandshakeException( "could not match http status line" );
		}
		draft.setResourceDescriptor( firstLineTokens[ 1 ] );

		line = readStringLine( buf );
		while ( line != null && !line.isEmpty() ) {
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
		return translateHandshakeHttp( buf );
	}

	public int checkAlloc( int bytecount ) throws LimitExedeedException , InvalidDataException {
		if( bytecount < 0 )
			throw new InvalidDataException( "Negative count" );
		return bytecount;
	}
	
	public void setParseMode( Role role ){
		this.role = role;
	}

}