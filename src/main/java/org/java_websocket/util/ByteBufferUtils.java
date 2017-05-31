package org.java_websocket.util;

import java.nio.ByteBuffer;

/**
 * Utility class for ByteBuffers
 */
public class ByteBufferUtils {

	/**
	 * Private constructor for static class
	 */
	private ByteBufferUtils() {
	}

	/**
	 * Transfer from one ByteBuffer to another ByteBuffer
	 *
	 * @param source the ByteBuffer to copy from
	 * @param dest   the ByteBuffer to copy to
	 * @return the number of transferred bytes
	 */
	public static int transferByteBuffer( ByteBuffer source, ByteBuffer dest ) {
		if( source == null || dest == null ) {
			throw new IllegalArgumentException();
		}
		int fremain = source.remaining();
		int toremain = dest.remaining();
		if( fremain > toremain ) {
			int limit = Math.min( fremain, toremain );
			source.limit( limit );
			dest.put( source );
			return limit;
		} else {
			dest.put( source );
			return fremain;
		}
	}

	/**
	 * Get a ByteBuffer with zero capacity
	 *
	 * @return empty ByteBuffer
	 */
	public static ByteBuffer getEmptyByteBuffer() {
		return ByteBuffer.allocate( 0 );
	}
}
