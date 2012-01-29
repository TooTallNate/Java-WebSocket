package net.tootallnate.websocket;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

public class CloseFrameBuilder extends FramedataImpl1 implements CloseFrame {

	public CloseFrameBuilder() {
		super( Opcode.CLOSING );
		setFin( true );
	}

	public CloseFrameBuilder( int code ) {
		super( Opcode.CLOSING );
		setFin( true );
		setCodeAndMessage( code, "" );
	}

	public CloseFrameBuilder( int code , String m ) {
		super( Opcode.CLOSING );
		setFin( true );
		setCodeAndMessage( code, m );
	}

	public void setCloseCode( int code ) {
		try {
			setCodeAndMessage( code, getMessage() );
		} catch ( CharacterCodingException e ) {
			assert ( false );
			// not expected
			throw new RuntimeException( e );
		}
	}

	/** This operation changes the payload */
	public void setMessage( String message ) {
		setCodeAndMessage( getCloseCode(), message );
	}

	public void setCodeAndMessage( int code, String m ) {
		byte[] by = Charsetfunctions.utf8Bytes( m );
		ByteBuffer buf = ByteBuffer.allocate( 4 );
		buf.putInt( code );
		buf.position( 2 );
		ByteBuffer pay = ByteBuffer.allocate( 2 + by.length );
		pay.put( buf );
		pay.put( by );
	}

	@Override
	public int getCloseCode() {
		int code = CloseFrame.NOCODE;
		if( unmaskedpayload.array().length >= 2 ) {
			ByteBuffer bb = ByteBuffer.allocate( 4 );
			bb.position( 2 );
			bb.put( unmaskedpayload.array(), 0, 2 );
			bb.position( 0 );
			code = bb.getInt();
			if( code < 0 || code > Short.MAX_VALUE )
				code = CloseFrame.NOCODE;
		}
		return code;
	}

	@Override
	public String getMessage() throws CharacterCodingException {
		if( getCloseCode() == CloseFrame.NOCODE ) {
			return Charsetfunctions.stingUtf8( unmaskedpayload.array() );
		}
		else{
			return Charsetfunctions.stingUtf8( unmaskedpayload.array(),2,unmaskedpayload.array().length-2 );
		} 
	}

	@Override
	public String toString() {
		return super.toString() + "code: " + getCloseCode();
	}

}
