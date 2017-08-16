/*
 * Copyright (c) 2010-2017 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.java_websocket.drafts;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.java_websocket.WebSocket.Role;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.exceptions.IncompleteHandshakeException;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.exceptions.LimitExedeedException;
import org.java_websocket.framing.*;
import org.java_websocket.framing.Framedata.Opcode;
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

	/**
	 * Enum which represents the states a handshake may be in
	 */
	public enum HandshakeState {
		/** Handshake matched this Draft successfully */
		MATCHED,
		/** Handshake is does not match this Draft */
		NOT_MATCHED
	}
	/**
	 * Enum which represents type of handshake is required for a close
	 */
	public enum CloseHandshakeType {
		NONE, ONEWAY, TWOWAY
	}

	public static int MAX_FAME_SIZE = 1000;
	public static int INITIAL_FAMESIZE = 64;

	public static final byte[] FLASH_POLICY_REQUEST = Charsetfunctions.utf8Bytes( "<policy-file-request/>\0" );

	/** In some cases the handshake will be parsed different depending on whether */
	protected Role role = null;

	protected Opcode continuousFrameType = null;

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

	public static HandshakeBuilder translateHandshakeHttp( ByteBuffer buf, Role role ) throws InvalidHandshakeException , IncompleteHandshakeException {
		HandshakeBuilder handshake;

		String line = readStringLine( buf );
		if( line == null )
			throw new IncompleteHandshakeException( buf.capacity() + 128 );

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
			// If the handshake contains already a specific key, append the new value
			if ( handshake.hasFieldValue( pair[ 0 ] ) ) {
				handshake.put( pair[0], handshake.getFieldValue( pair[ 0 ] ) + "; " + pair[1].replaceFirst( "^ +", "" ) );
			} else {
				handshake.put( pair[0], pair[1].replaceFirst( "^ +", "" ) );
			}
			line = readStringLine( buf );
		}
		if( line == null )
			throw new IncompleteHandshakeException();
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


	/**
	 * Handle the frame specific to the draft
	 * @param webSocketImpl the websocketimpl used for this draft
	 * @param frame the frame which is supposed to be handled
	 */
	public abstract void processFrame( WebSocketImpl webSocketImpl, Framedata frame ) throws InvalidDataException;

	public List<Framedata> continuousFrame( Opcode op, ByteBuffer buffer, boolean fin ) {
		if(op != Opcode.BINARY && op != Opcode.TEXT) {
			throw new IllegalArgumentException( "Only Opcode.BINARY or  Opcode.TEXT are allowed" );
		}
		DataFrame bui = null;
		if( continuousFrameType != null ) {
			bui = new ContinuousFrame();
		} else {
			continuousFrameType = op;
			if (op == Opcode.BINARY) {
				bui = new BinaryFrame();
			} else if (op == Opcode.TEXT) {
				bui = new TextFrame();
			}
		}
		bui.setPayload( buffer );
		bui.setFin( fin );
		try {
			bui.isValid();
		} catch ( InvalidDataException e ) {
			throw new RuntimeException( e ); // can only happen when one builds close frames(Opcode.Close)
		}
		if( fin ) {
			continuousFrameType = null;
		} else {
			continuousFrameType = op;
		}
		return Collections.singletonList( (Framedata) bui );
	}

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
			bui.append("HTTP/1.1 101 ").append(((ServerHandshake) handshakedata).getHttpStatusMessage());
		} else {
			throw new RuntimeException( "unknown role" );
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

	/**
	 * Drafts must only be by one websocket at all. To prevent drafts to be used more than once the Websocket implementation should call this method in order to create a new usable version of a given draft instance.<br>
	 * The copy can be safely used in conjunction with a new websocket connection.
	 * @return a copy of the draft
	 */
	public abstract Draft copyInstance();

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
	
	public Role getRole() {
		return role;
	}

	public String toString() {
		return getClass().getSimpleName();
	}

}
