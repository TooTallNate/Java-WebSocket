package net.tootallnate.websocket;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

public class Charsetfunctions {

	public static CodingErrorAction codingErrorAction = CodingErrorAction.REPORT;

	/*
	* @return UTF-8 encoding in bytes
	*/
	public static byte[] utf8Bytes( String s ) {
		try {
			return s.getBytes( "UTF8" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}

	/*
	* @return ASCII encoding in bytes
	*/
	public static byte[] asciiBytes( String s ) {
		try {
			return s.getBytes( "ASCII" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}

	public static String stingAscii( byte[] bytes ) {
		try {
			return new String( bytes, "ASCII" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}
	
	public static String stingAscii( byte[] bytes, int offset, int length ){
		try {
			return new String( bytes, offset, length, "ASCII" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}

	public static String stingUtf8( byte[] bytes ) throws CharacterCodingException {
		CharsetDecoder encode = Charset.forName("UTF8").newDecoder();
		encode.onMalformedInput( codingErrorAction  );
		return encode.decode( ByteBuffer.wrap( bytes )  ).toString();
	}
}
