package net.tootallnate.websocket;

import java.io.UnsupportedEncodingException;

public class Charsetfunctions {

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
	
	public static String stingAscii( byte[] bytes, int offset, int length ) {
		try {
			return new String( bytes, offset, length, "ASCII" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}

	public static String stingUtf8( byte[] bytes ) {
		try {
			return new String( bytes, "UTF8" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}
}
