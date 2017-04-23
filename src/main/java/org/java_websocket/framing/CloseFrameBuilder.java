package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.util.Charsetfunctions;

import java.nio.ByteBuffer;

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

	public CloseFrameBuilder( int code, String m ) throws InvalidDataException {
		super( Opcode.CLOSING );
		setFin( true );
		setCodeAndMessage( code, m );
	}

	private void setCodeAndMessage( int code, String m ) throws InvalidDataException {
		if( m == null ) {
			m = "";
		}
		// CloseFrame.TLS_ERROR is not allowed to be transfered over the wire
		if( code == CloseFrame.TLS_ERROR ) {
			code = CloseFrame.NOCODE;
			m = "";
		}
		if( code == CloseFrame.NOCODE ) {
			if( 0 < m.length() ) {
				throw new InvalidDataException( PROTOCOL_ERROR, "A close frame must have a closecode if it has a reason" );
			}
			return;// empty payload
		}
		//Intentional check for code != CloseFrame.TLS_ERROR just to make sure even if the code earlier changes
		if( ( code > CloseFrame.UNEXPECTED_CONDITION && code < 3000 && code != CloseFrame.TLS_ERROR ) ) {
			throw new InvalidDataException( PROTOCOL_ERROR, "Trying to send an illegal close code!" );
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
		ByteBuffer payload = super.getPayloadData();
		payload.mark();
		if( payload.remaining() >= 2 ) {
			ByteBuffer bb = ByteBuffer.allocate( 4 );
			bb.position( 2 );
			bb.putShort( payload.getShort() );
			bb.position( 0 );
			code = bb.getInt();

			if( code == CloseFrame.ABNORMAL_CLOSE || code == CloseFrame.TLS_ERROR || code == CloseFrame.NOCODE || code > 4999 || code < 1000 || code == 1004 ) {
				throw new InvalidFrameException( "closecode must not be sent over the wire: " + code );
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
			reason = Charsetfunctions.stringUtf8( super.getPayloadData() );
		} else {
			ByteBuffer b = super.getPayloadData();
			int mark = b.position();// because stringUtf8 also creates a mark
			try {
				b.position( b.position() + 2 );
				reason = Charsetfunctions.stringUtf8( b );
			} catch ( IllegalArgumentException e ) {
				throw new InvalidFrameException( e );
			} finally {
				b.position( mark );
			}
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
