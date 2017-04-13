package org.java_websocket.util;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;

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

	public static String stringAscii( byte[] bytes, int offset, int length ) {
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

	public static void main( String[] args ) throws InvalidDataException {
		stringUtf8( utf8Bytes( "\0" ) );
		stringAscii( asciiBytes( "\0" ) );
	}

	/**
	 * Implementation of the "Flexible and Economical UTF-8 Decoder" algorithm
	 * by Björn Höhrmann (http://bjoern.hoehrmann.de/utf-8/decoder/dfa/)
	 */
	private static final int[] utf8d = {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 00..1f
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 20..3f
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 40..5f
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 60..7f
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, // 80..9f
			7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, // a0..bf
			8, 8, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, // c0..df
			0xa, 0x3, 0x3, 0x3, 0x3, 0x3, 0x3, 0x3, 0x3, 0x3, 0x3, 0x3, 0x3, 0x4, 0x3, 0x3, // e0..ef
			0xb, 0x6, 0x6, 0x6, 0x5, 0x8, 0x8, 0x8, 0x8, 0x8, 0x8, 0x8, 0x8, 0x8, 0x8, 0x8, // f0..ff
			0x0, 0x1, 0x2, 0x3, 0x5, 0x8, 0x7, 0x1, 0x1, 0x1, 0x4, 0x6, 0x1, 0x1, 0x1, 0x1, // s0..s0
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, // s1..s2
			1, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, // s3..s4
			1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, // s5..s6
			1, 3, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1  // s7..s8
	};

	/**
	 * Check if the provided BytebBuffer contains a valid utf8 encoded string.
	 * <p>
	 * Using the algorithm "Flexible and Economical UTF-8 Decoder" by Björn Höhrmann (http://bjoern.hoehrmann.de/utf-8/decoder/dfa/)
	 *
	 * @param data the ByteBuffer
	 * @param off  offset (for performance reasons)
	 * @return does the ByteBuffer contain a valid utf8 encoded string
	 */
	public static boolean isValidUTF8( ByteBuffer data, int off ) {
		int len = data.remaining();
		if( len < off ) {
			return false;
		}
		int state = 0;
		for( int i = off; i < len; ++i ) {
			state = utf8d[256 + ( state << 4 ) + utf8d[( 0xff & data.get( i ) )]];
			if( state == 1 ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Calling isValidUTF8 with offset 0
	 *
	 * @param data the ByteBuffer
	 * @return does the ByteBuffer contain a valid utf8 encoded string
	 */
	public static boolean isValidUTF8( ByteBuffer data ) {
		return isValidUTF8( data, 0 );
	}

}
