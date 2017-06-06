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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.java_websocket.WebSocket.Role;
import org.java_websocket.exceptions.IncompleteHandshakeException;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

@Deprecated
public class Draft_76 extends Draft_75 {
	private boolean failed = false;
	private static final byte[] closehandshake = { -1, 0 };
	
	private final Random reuseableRandom = new Random();
	

	public static byte[] createChallenge( String key1, String key2, byte[] key3 ) throws InvalidHandshakeException {
		byte[] part1 = getPart( key1 );
		byte[] part2 = getPart( key2 );
		byte[] challenge = new byte[ 16 ];
		challenge[ 0 ] = part1[ 0 ];
		challenge[ 1 ] = part1[ 1 ];
		challenge[ 2 ] = part1[ 2 ];
		challenge[ 3 ] = part1[ 3 ];
		challenge[ 4 ] = part2[ 0 ];
		challenge[ 5 ] = part2[ 1 ];
		challenge[ 6 ] = part2[ 2 ];
		challenge[ 7 ] = part2[ 3 ];
		challenge[ 8 ] = key3[ 0 ];
		challenge[ 9 ] = key3[ 1 ];
		challenge[ 10 ] = key3[ 2 ];
		challenge[ 11 ] = key3[ 3 ];
		challenge[ 12 ] = key3[ 4 ];
		challenge[ 13 ] = key3[ 5 ];
		challenge[ 14 ] = key3[ 6 ];
		challenge[ 15 ] = key3[ 7 ];
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance( "MD5" );
		} catch ( NoSuchAlgorithmException e ) {
			throw new RuntimeException( e );
		}
		return md5.digest( challenge );
	}

	private static String generateKey() {
		Random r = new Random();
		long maxNumber = 4294967295L;
		long spaces = r.nextInt( 12 ) + 1;
		int max = new Long( maxNumber / spaces ).intValue();
		max = Math.abs( max );
		int number = r.nextInt( max ) + 1;
		long product = number * spaces;
		String key = Long.toString( product );
		// always insert atleast one random character
		int numChars = r.nextInt( 12 ) + 1;
		for( int i = 0 ; i < numChars ; i++ ) {
			int position = r.nextInt( key.length() );
			position = Math.abs( position );
			char randChar = (char) ( r.nextInt( 95 ) + 33 );
			// exclude numbers here
			if( randChar >= 48 && randChar <= 57 ) {
				randChar -= 15;
			}
			key = new StringBuilder( key ).insert( position, randChar ).toString();
		}
		for( int i = 0 ; i < spaces ; i++ ) {
			int position = r.nextInt( key.length() - 1 ) + 1;
			position = Math.abs( position );
			key = new StringBuilder( key ).insert( position, "\u0020" ).toString();
		}
		return key;
	}

	private static byte[] getPart( String key ) throws InvalidHandshakeException {
		try {
			long keyNumber = Long.parseLong( key.replaceAll( "[^0-9]", "" ) );
			long keySpace = key.split( "\u0020" ).length - 1;
			if( keySpace == 0 ) {
				throw new InvalidHandshakeException( "invalid Sec-WebSocket-Key (/key2/)" );
			}
			long part = keyNumber / keySpace;
			return new byte[]{ (byte) ( part >> 24 ), (byte) ( ( part << 8 ) >> 24 ), (byte) ( ( part << 16 ) >> 24 ), (byte) ( ( part << 24 ) >> 24 ) };
		} catch ( NumberFormatException e ) {
			throw new InvalidHandshakeException( "invalid Sec-WebSocket-Key (/key1/ or /key2/)" );
		}
	}

	@Override
	public HandshakeState acceptHandshakeAsClient( ClientHandshake request, ServerHandshake response ) {
		if( failed ) {
			return HandshakeState.NOT_MATCHED;
		}

		try {
			if( !response.getFieldValue( "Sec-WebSocket-Origin" ).equals( request.getFieldValue( "Origin" ) ) || !basicAccept( response ) ) {
				return HandshakeState.NOT_MATCHED;
			}
			byte[] content = response.getContent();
			if( content == null || content.length == 0 ) {
				throw new IncompleteHandshakeException();
			}
			if( Arrays.equals( content, createChallenge( request.getFieldValue( "Sec-WebSocket-Key1" ), request.getFieldValue( "Sec-WebSocket-Key2" ), request.getContent() ) ) ) {
				return HandshakeState.MATCHED;
			} else {
				return HandshakeState.NOT_MATCHED;
			}
		} catch ( InvalidHandshakeException e ) {
			throw new RuntimeException( "bad handshakerequest", e );
		}
	}

	@Override
	public HandshakeState acceptHandshakeAsServer( ClientHandshake handshakedata ) {
		if( handshakedata.getFieldValue( "Upgrade" ).equals( "WebSocket" ) && handshakedata.getFieldValue( "Connection" ).contains( "Upgrade" ) && handshakedata.getFieldValue( "Sec-WebSocket-Key1" ).length() > 0 && handshakedata.getFieldValue( "Sec-WebSocket-Key2" ).length() > 0 && handshakedata.hasFieldValue( "Origin" ) )
			return HandshakeState.MATCHED;
		return HandshakeState.NOT_MATCHED;
	}

	@Override
	public ClientHandshakeBuilder postProcessHandshakeRequestAsClient( ClientHandshakeBuilder request ) {
		request.put( "Upgrade", "WebSocket" );
		request.put( "Connection", "Upgrade" );
		request.put( "Sec-WebSocket-Key1", generateKey() );
		request.put( "Sec-WebSocket-Key2", generateKey() );

		if( !request.hasFieldValue( "Origin" ) ) {
			request.put( "Origin", "random" + reuseableRandom.nextInt() );
		}

		byte[] key3 = new byte[ 8 ];
		reuseableRandom.nextBytes( key3 );
		request.setContent( key3 );
		return request;

	}

	@Override
	public HandshakeBuilder postProcessHandshakeResponseAsServer( ClientHandshake request, ServerHandshakeBuilder response ) throws InvalidHandshakeException {
		response.setHttpStatusMessage( "WebSocket Protocol Handshake" );
		response.put( "Upgrade", "WebSocket" );
		response.put( "Connection", request.getFieldValue( "Connection" ) ); // to respond to a Connection keep alive
		response.put( "Sec-WebSocket-Origin", request.getFieldValue( "Origin" ) );
		String location = "ws://" + request.getFieldValue( "Host" ) + request.getResourceDescriptor();
		response.put( "Sec-WebSocket-Location", location );
		String key1 = request.getFieldValue( "Sec-WebSocket-Key1" );
		String key2 = request.getFieldValue( "Sec-WebSocket-Key2" );
		byte[] key3 = request.getContent();
		if( key1 == null || key2 == null || key3 == null || key3.length != 8 ) {
			throw new InvalidHandshakeException( "Bad keys" );
		}
		response.setContent( createChallenge( key1, key2, key3 ) );
		return response;
	}

	@Override
	public Handshakedata translateHandshake( ByteBuffer buf ) throws InvalidHandshakeException {

		HandshakeBuilder bui = translateHandshakeHttp( buf, role );
		// the first drafts are lacking a protocol number which makes them difficult to distinguish. Sec-WebSocket-Key1 is typical for draft76
		if( ( bui.hasFieldValue( "Sec-WebSocket-Key1" ) || role == Role.CLIENT ) && !bui.hasFieldValue( "Sec-WebSocket-Version" ) ) {
			byte[] key3 = new byte[ role == Role.SERVER ? 8 : 16 ];
			try {
				buf.get( key3 );
			} catch ( BufferUnderflowException e ) {
				throw new IncompleteHandshakeException( buf.capacity() + 16 );
			}
			bui.setContent( key3 );

		}
		return bui;
	}

	@Override
	public List<Framedata> translateFrame( ByteBuffer buffer ) throws InvalidDataException {
		buffer.mark();
		List<Framedata> frames = super.translateRegularFrame( buffer );
		if( frames == null ) {
			buffer.reset();
			frames = readyframes;
			readingState = true;
			if( currentFrame == null )
				currentFrame = ByteBuffer.allocate( 2 );
			else {
				throw new InvalidFrameException();
			}
			if( buffer.remaining() > currentFrame.remaining() ) {
				throw new InvalidFrameException();
			} else {
				currentFrame.put( buffer );
			}
			if( !currentFrame.hasRemaining() ) {
				if( Arrays.equals( currentFrame.array(), closehandshake ) ) {
					CloseFrame closeFrame = new CloseFrame();
					closeFrame.setCode(CloseFrame.NORMAL);
					closeFrame.isValid();
					frames.add(closeFrame);
					return frames;
				}
				else{
					throw new InvalidFrameException();
				}
			} else {
				readyframes = new LinkedList<Framedata>();
				return frames;
			}
		} else {
			return frames;
		}
	}
	@Override
	public ByteBuffer createBinaryFrame( Framedata framedata ) {
		if( framedata.getOpcode() == Opcode.CLOSING )
			return ByteBuffer.wrap( closehandshake );
		return super.createBinaryFrame( framedata );
	}

	@Override
	public CloseHandshakeType getCloseHandshakeType() {
		return CloseHandshakeType.ONEWAY;
	}

	@Override
	public Draft copyInstance() {
		return new Draft_76();
	}
}
