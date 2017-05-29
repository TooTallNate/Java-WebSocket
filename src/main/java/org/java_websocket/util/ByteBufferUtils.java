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
	 */
	public static void transferByteBuffer( ByteBuffer source, ByteBuffer dest ) {
		if( source == null || dest == null ) {
			throw new IllegalArgumentException();
		}
		int fremain = source.remaining();
		int toremain = dest.remaining();
		if( fremain > toremain ) {
			source.limit( Math.min( fremain, toremain ) );
			dest.put( source );
		} else {
			dest.put( source );
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
