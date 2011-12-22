package net.tootallnate.websocket.drafts;


import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.FrameBuilder;
import net.tootallnate.websocket.Framedata;
import net.tootallnate.websocket.Framedata.Opcode;
import net.tootallnate.websocket.FramedataImpl1;
import net.tootallnate.websocket.HandshakeBuilder;
import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;

public class Draft_75 extends Draft {
	
	/** 
	* The byte representing CR, or Carriage Return, or \r
	*/
	public static final byte CR = (byte)0x0D;
	/**
	* The byte representing LF, or Line Feed, or \n
	*/
	public static final byte LF = (byte)0x0A;
	/**
	* The byte representing the beginning of a WebSocket text frame.
	*/
	public static final byte START_OF_FRAME = (byte)0x00;
	/**
	* The byte representing the end of a WebSocket text frame.
	*/
	public static final byte END_OF_FRAME = (byte)0xFF;
	
	private boolean readingState = false;
	
	private ByteBuffer currentFrame;
	
	@Override
	public HandshakeState acceptHandshakeAsClient( Handshakedata request , Handshakedata response ) {
		return request.getFieldValue( "WebSocket-Origin" ).equals( response.getFieldValue( "Origin" ) ) 
			&& basicAccept( response )
		?
		HandshakeState.MATCHED
		:HandshakeState.NOT_MATCHED;
	}

	@Override
	public HandshakeState acceptHandshakeAsServer( Handshakedata handshakedata ) {
		if( handshakedata.hasFieldValue( "Origin" )
			&& basicAccept( handshakedata )
		){
			return HandshakeState.MATCHED;
		}
		return HandshakeState.NOT_MATCHED;
	}

	@Override
	public ByteBuffer createBinaryFrame( Framedata framedata ) {
		byte[] pay = framedata.getPayloadData();
		ByteBuffer b = ByteBuffer.allocate( pay.length + 2);
	    b.put(START_OF_FRAME);
	    b.put(pay);
	    b.put(END_OF_FRAME);
	    b.rewind();
	    return b;
	}

	@Override
	public List<Framedata> createFrames( byte[] binary , boolean mask ) {
		throw new RuntimeException ( "not yet implemented" );
	}

	@Override
	public List<Framedata> createFrames( String text , boolean mask ) {
		FrameBuilder frame = new FramedataImpl1();
		frame.setPayload ( WebSocket.utf8Bytes(text) );
		frame.setFin ( true );
		frame.setOptcode ( Opcode.TEXT );
		frame.setTransferemasked ( mask ); 
		return Collections.singletonList ( (Framedata)frame );
	}
	
	@Override
	public HandshakeBuilder postProcessHandshakeRequestAsClient( HandshakeBuilder request ) throws InvalidHandshakeException{
		request.put ( "Upgrade" , "WebSocket" );
		request.put ( "Connection" , "Upgrade" );
		return request;
	}

	@Override
	public HandshakeBuilder postProcessHandshakeResponseAsServer( Handshakedata request , HandshakeBuilder response ) throws InvalidHandshakeException {
		response.setHttpStatusMessage( "Web Socket Protocol Handshake" );
		response.put ( "Upgrade" , "WebSocket" );
		response.put ( "Connection" , request.getFieldValue ( "Connection" ) ); //to respond to a Connection keep alive
		response.put( "WebSocket-Origin" , request.getFieldValue( "Origin" ) );
		String location = "ws://" + request.getFieldValue("Host") + request.getResourceDescriptor();
		response.put( "WebSocket-Location" , location );
		//TODO handle Sec-WebSocket-Protocol and Set-Cookie
		return response;
	}

	@Override
	public List<Framedata> translateFrame( ByteBuffer buffer , int read ) {
		List<Framedata> frames= new LinkedList<Framedata> ();
		for( int i = 0 ; i < read ; i++ ){
			byte newestByte =  buffer.get ( i );
			if ( newestByte == START_OF_FRAME && !readingState ) { // Beginning of Frame
				this.currentFrame = null;
				readingState = true;
			} 
			else if ( newestByte == END_OF_FRAME && readingState ) { // End of Frame
				readingState = false;
				String textFrame = null;
				// currentFrame will be null if END_OF_FRAME was send directly after
				// START_OF_FRAME, thus we will send 'null' as the sent message.
				if ( this.currentFrame != null ) {
					FramedataImpl1 curframe = new FramedataImpl1();
					curframe.setPayload ( currentFrame.array () );
					curframe.setFin ( true );
					curframe.setOptcode ( Opcode.TEXT );
					frames.add ( curframe );
				}
			} 
			else { // Regular frame data, add to current frame buffer	//TODO This code is expensive and slow
				ByteBuffer frame = ByteBuffer.allocate ( ( this.currentFrame != null ? this.currentFrame.capacity () : 0 ) + 1 ); 
				if ( this.currentFrame != null ) {
					this.currentFrame.rewind ();
					frame.put ( this.currentFrame );
				}
				frame.put ( newestByte );
				this.currentFrame = frame;
			}
		}
		if( readingState ){
			FramedataImpl1 curframe = new FramedataImpl1();
			curframe.setPayload ( currentFrame.array () );
			curframe.setFin ( false );
			curframe.setOptcode ( Opcode.TEXT );
			frames.add ( curframe );
		}
		return frames;
	}
	
}
