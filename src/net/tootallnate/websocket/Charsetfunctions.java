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

	public static String stringAscii( byte[] bytes ) {
		return stringAscii( bytes, 0, bytes.length );
	}
	
	public static String stringAscii( byte[] bytes, int offset, int length ){
		try {
			return new String( bytes, offset, length, "ASCII" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}

	public static String stringUtf8( byte[] bytes ) throws CharacterCodingException {
		return stringAscii( bytes, 0, bytes.length );
	}

	public static String stringUtf8( byte[] bytes, int off, int length ) throws CharacterCodingException {
		CharsetDecoder encode = Charset.forName( "UTF8" ).newDecoder();
		encode.onMalformedInput( codingErrorAction );
		return encode.decode( ByteBuffer.wrap( bytes, off, length ) ).toString();
	}
}
