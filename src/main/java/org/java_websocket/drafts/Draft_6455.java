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

import org.java_websocket.WebSocketImpl;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.exceptions.LimitExedeedException;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation for the RFC 6455 websocket protocol
 * This is the recommended class for your websocket connection
 */
@SuppressWarnings("deprecation")
public class Draft_6455 extends Draft_17 {

	/**
	 * Attribute for the used extension in this draft
	 */
	private IExtension extension;

	/**
	 * Attribute for all available extension in this draft
	 */
	private List<IExtension> knownExtensions;

	/**
	 * Constructor for the websocket protocol specified by RFC 6455 with default extensions
	 */
	public Draft_6455() {
		this( Collections.<IExtension>emptyList() );
	}

	/**
	 * Constructor for the websocket protocol specified by RFC 6455 with custom extensions
	 *
	 * @param inputExtension the extension which should be used for this draft
	 */
	public Draft_6455( IExtension inputExtension ) {
		this( Collections.singletonList( inputExtension ) );
	}

	/**
	 * Constructor for the websocket protocol specified by RFC 6455 with custom extensions
	 *
	 * @param inputExtensions the extensions which should be used for this draft
	 */
	public Draft_6455( List<IExtension> inputExtensions ) {
		knownExtensions = new ArrayList<IExtension>();
		boolean hasDefault = false;
		for( IExtension inputExtension : inputExtensions ) {
			if( inputExtension.getClass().equals( DefaultExtension.class ) ) {
				hasDefault = true;
			}
		}
		knownExtensions.addAll( inputExtensions );
		//We always add the DefaultExtension to implement the normal RFC 6455 specification
		if( !hasDefault ) {
			DefaultExtension defaultExtension = new DefaultExtension();
			knownExtensions.add( this.knownExtensions.size(), defaultExtension );
		}
	}

	@Override
	public HandshakeState acceptHandshakeAsServer( ClientHandshake handshakedata ) throws InvalidHandshakeException {
		if( super.acceptHandshakeAsServer( handshakedata ) == HandshakeState.NOT_MATCHED ) {
			return HandshakeState.NOT_MATCHED;
		}
		String requestedExtension = handshakedata.getFieldValue( "Sec-WebSocket-Extensions" );
		for( IExtension knownExtension : knownExtensions ) {
			if( knownExtension.acceptProvidedExtensionAsServer( requestedExtension ) ) {
				extension = knownExtension;
				return HandshakeState.MATCHED;
			}
		}
		return HandshakeState.NOT_MATCHED;
	}


	@Override
	public HandshakeState acceptHandshakeAsClient( ClientHandshake request, ServerHandshake response ) throws InvalidHandshakeException {
		if( super.acceptHandshakeAsClient( request, response ) == HandshakeState.NOT_MATCHED ) {
			return HandshakeState.NOT_MATCHED;
		}
		String requestedExtension = response.getFieldValue( "Sec-WebSocket-Extensions" );
		for( IExtension knownExtension : knownExtensions ) {
			if( knownExtension.acceptProvidedExtensionAsClient( requestedExtension ) ) {
				extension = knownExtension;
				return HandshakeState.MATCHED;
			}
		}
		return HandshakeState.NOT_MATCHED;
	}

	/**
	 * Getter for the extension which is used by this draft
	 *
	 * @return the extension which is used or null, if handshake is not yet done
	 */
	public IExtension getExtension() {
		return extension;
	}

	@Override
	public ClientHandshakeBuilder postProcessHandshakeRequestAsClient( ClientHandshakeBuilder request ) {
		super.postProcessHandshakeRequestAsClient( request );
		StringBuilder requestedExtensions = new StringBuilder();
		for( IExtension knownExtension : knownExtensions ) {
			if( knownExtension.getProvidedExtensionAsClient() != null && !knownExtension.getProvidedExtensionAsClient().equals( "" ) ) {
				requestedExtensions.append( knownExtension.getProvidedExtensionAsClient() ).append( "; " );
			}
		}
		if( requestedExtensions.length() != 0 ) {
			request.put( "Sec-WebSocket-Extensions", requestedExtensions.toString() );
		}
		return request;
	}

	@Override
	public HandshakeBuilder postProcessHandshakeResponseAsServer( ClientHandshake request, ServerHandshakeBuilder
			response ) throws InvalidHandshakeException {
		super.postProcessHandshakeResponseAsServer( request, response );
		if( getExtension().getProvidedExtensionAsServer().length() != 0 ) {
			response.put( "Sec-WebSocket-Extensions", getExtension().getProvidedExtensionAsServer() );
		}
		response.setHttpStatusMessage( "Web Socket Protocol Handshake" );
		response.put( "Server", "TooTallNate Java-WebSocket" );
		response.put( "Date", getServerTime() );
		return response;
	}


	@Override
	public Draft copyInstance() {
		ArrayList<IExtension> newExtensions = new ArrayList<IExtension>();
		for( IExtension extension : knownExtensions ) {
			newExtensions.add( extension.copyInstance() );
		}
		return new Draft_6455( newExtensions );
	}

	@Override
	public ByteBuffer createBinaryFrame( Framedata framedata ) {
		getExtension().encodeFrame( framedata );
		if( WebSocketImpl.DEBUG )
			System.out.println( "afterEnconding(" + framedata.getPayloadData().remaining() + "): {" + ( framedata.getPayloadData().remaining() > 1000 ? "too big to display" : new String( framedata.getPayloadData().array() ) ) + "}" );
		return super.createBinaryFrame( framedata );
	}

	@Override
	public Framedata translateSingleFrame( ByteBuffer buffer ) throws IncompleteException, InvalidDataException {
		int maxpacketsize = buffer.remaining();
		int realpacketsize = 2;
		if( maxpacketsize < realpacketsize )
			throw new IncompleteException( realpacketsize );
		byte b1 = buffer.get( /*0*/ );
		boolean FIN = b1 >> 8 != 0;
		boolean rsv1 = false, rsv2 = false, rsv3 = false;
		if( ( b1 & 0x40 ) != 0 ) {
			rsv1 = true;
		}
		if( ( b1 & 0x20 ) != 0 ) {
			rsv2 = true;
		}
		if( ( b1 & 0x10 ) != 0 ) {
			rsv3 = true;
		}
		byte b2 = buffer.get( /*1*/ );
		boolean MASK = ( b2 & -128 ) != 0;
		int payloadlength = ( byte ) ( b2 & ~( byte ) 128 );
		Framedata.Opcode optcode = toOpcode( ( byte ) ( b1 & 15 ) );

		if( !( payloadlength >= 0 && payloadlength <= 125 ) ) {
			if( optcode == Framedata.Opcode.PING || optcode == Framedata.Opcode.PONG || optcode == Framedata.Opcode.CLOSING ) {
				throw new InvalidFrameException( "more than 125 octets" );
			}
			if( payloadlength == 126 ) {
				realpacketsize += 2; // additional length bytes
				if( maxpacketsize < realpacketsize )
					throw new IncompleteException( realpacketsize );
				byte[] sizebytes = new byte[3];
				sizebytes[1] = buffer.get( /*1 + 1*/ );
				sizebytes[2] = buffer.get( /*1 + 2*/ );
				payloadlength = new BigInteger( sizebytes ).intValue();
			} else {
				realpacketsize += 8; // additional length bytes
				if( maxpacketsize < realpacketsize )
					throw new IncompleteException( realpacketsize );
				byte[] bytes = new byte[8];
				for( int i = 0; i < 8; i++ ) {
					bytes[i] = buffer.get( /*1 + i*/ );
				}
				long length = new BigInteger( bytes ).longValue();
				if( length > Integer.MAX_VALUE ) {
					throw new LimitExedeedException( "Payloadsize is to big..." );
				} else {
					payloadlength = ( int ) length;
				}
			}
		}

		// int maskskeystart = foff + realpacketsize;
		realpacketsize += ( MASK ? 4 : 0 );
		// int payloadstart = foff + realpacketsize;
		realpacketsize += payloadlength;

		if( maxpacketsize < realpacketsize )
			throw new IncompleteException( realpacketsize );

		ByteBuffer payload = ByteBuffer.allocate( checkAlloc( payloadlength ) );
		if( MASK ) {
			byte[] maskskey = new byte[4];
			buffer.get( maskskey );
			for( int i = 0; i < payloadlength; i++ ) {
				payload.put( ( byte ) ( buffer.get( /*payloadstart + i*/ ) ^ maskskey[i % 4] ) );
			}
		} else {
			payload.put( buffer.array(), buffer.position(), payload.limit() );
			buffer.position( buffer.position() + payload.limit() );
		}

		FramedataImpl1 frame = FramedataImpl1.get( optcode );
		frame.setFin( FIN );
		frame.setRSV1( rsv1 );
		frame.setRSV2( rsv2 );
		frame.setRSV3( rsv3 );
		payload.flip();
		frame.setPayload( payload );
		getExtension().isFrameValid( frame );
		getExtension().decodeFrame( frame );
		if( WebSocketImpl.DEBUG )
			System.out.println( "afterDecoding(" + frame.getPayloadData().remaining() + "): {" + ( frame.getPayloadData().remaining() > 1000 ? "too big to display" : new String( frame.getPayloadData().array() ) ) + "}" );
		frame.isValid();
		return frame;
	}


	@Override
	public List<Framedata> translateFrame( ByteBuffer buffer ) throws InvalidDataException {
		while( true ) {
			List<Framedata> frames = new LinkedList<Framedata>();
			Framedata cur;
			if( incompleteframe != null ) {
				// complete an incomplete frame
				try {
					buffer.mark();
					int available_next_byte_count = buffer.remaining();// The number of bytes received
					int expected_next_byte_count = incompleteframe.remaining();// The number of bytes to complete the incomplete frame

					if( expected_next_byte_count > available_next_byte_count ) {
						// did not receive enough bytes to complete the frame
						incompleteframe.put( buffer.array(), buffer.position(), available_next_byte_count );
						buffer.position( buffer.position() + available_next_byte_count );
						return Collections.emptyList();
					}
					incompleteframe.put( buffer.array(), buffer.position(), expected_next_byte_count );
					buffer.position( buffer.position() + expected_next_byte_count );
					cur = translateSingleFrame( ( ByteBuffer ) incompleteframe.duplicate().position( 0 ) );
					frames.add( cur );
					incompleteframe = null;
				} catch ( IncompleteException e ) {
					// extending as much as suggested
					int oldsize = incompleteframe.limit();
					ByteBuffer extendedframe = ByteBuffer.allocate( checkAlloc( e.getPreferedSize() ) );
					assert ( extendedframe.limit() > incompleteframe.limit() );
					incompleteframe.rewind();
					extendedframe.put( incompleteframe );
					incompleteframe = extendedframe;
					continue;
				}
			}

			while( buffer.hasRemaining() ) {// Read as much as possible full frames
				buffer.mark();
				try {
					cur = translateSingleFrame( buffer );
					frames.add( cur );
				} catch ( IncompleteException e ) {
					// remember the incomplete data
					buffer.reset();
					int pref = e.getPreferedSize();
					incompleteframe = ByteBuffer.allocate( checkAlloc( pref ) );
					incompleteframe.put( buffer );
					break;
				}
			}
			return frames;
		}
	}

	@Override
	public void reset() {
		super.reset();
		if (extension != null) {
			extension.reset();
		}
		extension = null;
	}

	/**
	 * Generate a date for for the date-header
	 *
	 * @return the server time
	 */
	private String getServerTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US );
		dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
		return dateFormat.format( calendar.getTime() );
	}

	@Override
	public String toString() {
		String result = super.toString();
		if( getExtension() != null )
			result += " extension: " + getExtension().toString();
		return result;
	}
}
