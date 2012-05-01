package org.java_websocket.framing;

import java.nio.ByteBuffer;

import org.java_websocket.exeptions.InvalidDataException;
import org.java_websocket.exeptions.InvalidFrameException;
import org.java_websocket.util.Charsetfunctions;

public class CloseFrameBuilder extends FramedataImpl1 implements CloseFrame {

	static final ByteBuffer emptybytebuffer = ByteBuffer.allocate( 0 );

	private int code;
	private String reason;

	public CloseFrameBuilder() {
		super( Opcode.CLOSING );
		setFin( true );
	}

	public CloseFrameBuilder( int code ) throws InvalidDataException {
		super( Opcode.CLOSING );
		setFin( true );
		setCodeAndMessage( code, "" );
	}

	public CloseFrameBuilder( int code , String m ) throws InvalidDataException {
		super( Opcode.CLOSING );
		setFin( true );
		setCodeAndMessage( code, m );
	}

	private void setCodeAndMessage( int code, String m ) throws InvalidDataException {
		if( m == null ) {
			m = "";
		}
		byte[] by = Charsetfunctions.utf8Bytes( m );
		ByteBuffer buf = ByteBuffer.allocate( 4 );
		buf.putInt( code );
		buf.position( 2 );
		ByteBuffer pay = ByteBuffer.allocate( 2 + by.length );
		pay.put( buf );
		pay.put( by );
		pay.rewind();
		setPayload( pay );
	}

	private void initCloseCode() throws InvalidFrameException {
		code = CloseFrame.NOCODE;
		ByteBuffer payload = getPayloadData();
		payload.mark();
		if( payload.remaining() >= 2 ) {
			ByteBuffer bb = ByteBuffer.allocate( 4 );
			bb.position( 2 );
			bb.putShort( payload.getShort() );
			bb.position( 0 );
			code = bb.getInt();
			if( code < 0 || code > Short.MAX_VALUE )
				code = CloseFrame.NOCODE;
			if( code < CloseFrame.NORMAL || code > CloseFrame.EXTENSION || code == NOCODE || code == 1004 ) {
				throw new InvalidFrameException( "bad code " + code );
			}
		}
		payload.reset();
	}

	@Override
	public int getCloseCode() {
		return code;
	}

	private void initMessage() throws InvalidDataException {
		if( code == CloseFrame.NOCODE ) {
			reason = Charsetfunctions.stringUtf8( getPayloadData() );
		} else {
			ByteBuffer b = getPayloadData();
			b.mark();
			try {
				b.position( b.position() + 2 );
			} catch ( IllegalArgumentException e ) {
				throw new InvalidFrameException( e );
			} finally {
				b.reset();
			}
			reason = Charsetfunctions.stringUtf8( getPayloadData() );
		}
	}

	@Override
	public String getMessage() {
		return reason;
	}

	@Override
	public String toString() {
		return super.toString() + "code: " + code;
	}

	@Override
	public void setPayload( ByteBuffer payload ) throws InvalidDataException {
		super.setPayload( payload );
		initCloseCode();
		initMessage();
	}
	@Override
	public ByteBuffer getPayloadData() {
		if( code == NOCODE )
			return emptybytebuffer;
		return super.getPayloadData();
	}

}
