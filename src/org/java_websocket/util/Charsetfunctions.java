package org.java_websocket.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.java_websocket.exeptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;

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

	public static String stringUtf8( byte[] bytes ) throws InvalidDataException {
		return stringUtf8( ByteBuffer.wrap( bytes ) );
	}

	/*public static String stringUtf8( byte[] bytes, int off, int length ) throws InvalidDataException {
		CharsetDecoder decode = Charset.forName( "UTF8" ).newDecoder();
		decode.onMalformedInput( codingErrorAction );
		decode.onUnmappableCharacter( codingErrorAction );
		//decode.replaceWith( "X" );
		String s;
		try {
			s = decode.decode( ByteBuffer.wrap( bytes, off, length ) ).toString();
		} catch ( CharacterCodingException e ) {
			throw new InvalidDataException( CloseFrame.NO_UTF8, e );
		}
		return s;
	}*/

	public static String stringUtf8( ByteBuffer bytes ) throws InvalidDataException {
		CharsetDecoder decode = Charset.forName( "UTF8" ).newDecoder();
		decode.onMalformedInput( codingErrorAction );
		decode.onUnmappableCharacter( codingErrorAction );
		// decode.replaceWith( "X" );
		String s;
		try {
			bytes.mark();
			s = decode.decode( bytes ).toString();
			bytes.reset();
		} catch ( CharacterCodingException e ) {
			throw new InvalidDataException( CloseFrame.NO_UTF8, e );
		}
		return s;
	}
}
