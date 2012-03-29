package org.java_websocket.drafts;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.java_websocket.exeptions.InvalidDataException;
import org.java_websocket.exeptions.InvalidHandshakeException;
import org.java_websocket.exeptions.NotSendableException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.util.Charsetfunctions;

public class Draft_75 extends Draft {

	/**
	 * The byte representing CR, or Carriage Return, or \r
	 */
	public static final byte CR = (byte) 0x0D;
	/**
	 * The byte representing LF, or Line Feed, or \n
	 */
	public static final byte LF = (byte) 0x0A;
	/**
	 * The byte representing the beginning of a WebSocket text frame.
	 */
	public static final byte START_OF_FRAME = (byte) 0x00;
	/**
	 * The byte representing the end of a WebSocket text frame.
	 */
	public static final byte END_OF_FRAME = (byte) 0xFF;

	protected boolean readingState = false;
	private boolean inframe = false;
	protected List<Framedata> readyframes = new LinkedList<Framedata>();
	protected ByteBuffer currentFrame;
	
	
	private final Random reuseableRandom = new Random();
	

	@Override
	public HandshakeState acceptHandshakeAsClient( ClientHandshake request, ServerHandshake response ) {
		return request.getFieldValue( "WebSocket-Origin" ).equals( response.getFieldValue( "Origin" ) ) && basicAccept( response ) ? HandshakeState.MATCHED : HandshakeState.NOT_MATCHED;
	}

	@Override
	public HandshakeState acceptHandshakeAsServer( ClientHandshake handshakedata ) {
		if( handshakedata.hasFieldValue( "Origin" ) && basicAccept( handshakedata ) ) {
			return HandshakeState.MATCHED;
		}
		return HandshakeState.NOT_MATCHED;
	}

	@Override
	public ByteBuffer createBinaryFrame( Framedata framedata ) {
		if( framedata.getOpcode() != Opcode.TEXT ) {
			throw new RuntimeException( "only text frames supported" );
		}

		byte[] pay = framedata.getPayloadData();
		ByteBuffer b = ByteBuffer.allocate( pay.length + 2 );
		b.put( START_OF_FRAME );
		b.put( pay );
		b.put( END_OF_FRAME );
		b.flip();
		return b;
	}

	@Override
	public List<Framedata> createFrames( byte[] binary, boolean mask ) {
		throw new RuntimeException( "not yet implemented" );
	}

	@Override
	public List<Framedata> createFrames( String text, boolean mask ) {
		FrameBuilder frame = new FramedataImpl1();
		try {
			frame.setPayload( Charsetfunctions.utf8Bytes( text ) );
		} catch ( InvalidDataException e ) {
			throw new NotSendableException( e );
		}
		frame.setFin( true );
		frame.setOptcode( Opcode.TEXT );
		frame.setTransferemasked( mask );
		return Collections.singletonList( (Framedata) frame );
	}

	@Override
	public ClientHandshakeBuilder postProcessHandshakeRequestAsClient( ClientHandshakeBuilder request ) throws InvalidHandshakeException {
		request.put( "Upgrade", "WebSocket" );
		request.put( "Connection", "Upgrade" );
		if( !request.hasFieldValue( "Origin" ) ) {
			request.put( "Origin", "random" + reuseableRandom.nextInt() );
		}

		return request;
	}

	@Override
	public HandshakeBuilder postProcessHandshakeResponseAsServer( ClientHandshake request, ServerHandshakeBuilder response ) throws InvalidHandshakeException {
		response.setHttpStatusMessage( "Web Socket Protocol Handshake" );
		response.put( "Upgrade", "WebSocket" );
		response.put( "Connection", request.getFieldValue( "Connection" ) ); // to respond to a Connection keep alive
		response.put( "WebSocket-Origin", request.getFieldValue( "Origin" ) );
		String location = "ws://" + request.getFieldValue( "Host" ) + request.getResourceDescriptor();
		response.put( "WebSocket-Location", location );
		// TODO handle Sec-WebSocket-Protocol and Set-Cookie
		return response;
	}

	protected List<Framedata> translateRegularFrame( ByteBuffer buffer ) throws InvalidDataException {
		while ( buffer.hasRemaining() ) {
			byte newestByte = buffer.get();
			if( newestByte == START_OF_FRAME ) { // Beginning of Frame
				if( readingState )
					return null;
				readingState = true;
			} else if( newestByte == END_OF_FRAME ) { // End of Frame
				if( !readingState )
					return null;
				// currentFrame will be null if END_OF_FRAME was send directly after
				// START_OF_FRAME, thus we will send 'null' as the sent message.
				if( this.currentFrame != null ) {
					FramedataImpl1 curframe = new FramedataImpl1();
					curframe.setPayload( currentFrame.array() );
					curframe.setFin( true );
					curframe.setOptcode( inframe ? Opcode.CONTINIOUS : Opcode.TEXT );
					readyframes.add( curframe );
					this.currentFrame = null;
					buffer.mark();
				}
				readingState = false;
				inframe = false;
			} else if( readingState ) { // Regular frame data, add to current frame buffer //TODO This code is very expensive and slow
				ByteBuffer frame = ByteBuffer.allocate( checkAlloc( ( this.currentFrame != null ? this.currentFrame.capacity() : 0 ) + 1 ) );
				if( this.currentFrame != null ) {
					this.currentFrame.rewind();
					frame.put( this.currentFrame );
				}
				frame.put( newestByte );
				this.currentFrame = frame;
			} else {
				return null;
			}
		}
		if( readingState ) {
			FramedataImpl1 curframe = new FramedataImpl1();
			curframe.setPayload( currentFrame.array() );
			curframe.setFin( false );
			curframe.setOptcode( inframe ? Opcode.CONTINIOUS : Opcode.TEXT );
			inframe = true;
			readyframes.add( curframe );
		}

		List<Framedata> frames = readyframes;
		readyframes = new LinkedList<Framedata>();
		this.currentFrame = null;
		return frames;
	}

	@Override
	public List<Framedata> translateFrame( ByteBuffer buffer ) throws InvalidDataException {
		List<Framedata> frames = translateRegularFrame( buffer );
		if( frames == null ) {
			throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR );
		}
		return frames;
	}

	@Override
	public void reset() {
		readingState = false;
		this.currentFrame = null;
	}

	@Override
	public CloseHandshakeType getCloseHandshakeType() {
		return CloseHandshakeType.NONE;
	}

}
