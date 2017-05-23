package org.java_websocket.util;

import org.java_websocket.util.ByteBufferUtils;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * JUnit Test for the new ByteBufferUtils class
 */
public class ByteBufferUtilsTest {

	private static byte[] smallArray = { 0, -1, -2, -3, -4 };
	private static byte[] bigArray = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	@Test
	public void testEmptyByteBufferCapacity() {
		ByteBuffer byteBuffer = ByteBufferUtils.getEmptyByteBuffer();
		assertEquals( "capacity must be 0", byteBuffer.capacity(), 0 );
	}

	@Test
	public void testEmptyByteBufferLimit() {
		ByteBuffer byteBuffer = ByteBufferUtils.getEmptyByteBuffer();
		assertEquals( "limit must be 0", byteBuffer.limit(), 0 );
	}

	@Test
	public void testEmptyByteBufferNewObject() {
		ByteBuffer byteBuffer0 = ByteBufferUtils.getEmptyByteBuffer();
		ByteBuffer byteBuffer1 = ByteBufferUtils.getEmptyByteBuffer();
		assertTrue( "Allocated new object", byteBuffer0 != byteBuffer1 );
	}

	@Test
	public void testTransferByteBufferSmallToEmpty() {
		ByteBuffer small = ByteBuffer.wrap( smallArray );
		ByteBuffer empty = ByteBufferUtils.getEmptyByteBuffer();
		ByteBufferUtils.transferByteBuffer( small, empty );
		assertArrayEquals( "Small bytebuffer should not change", small.array(), smallArray );
		assertEquals( "capacity of the empty bytebuffer should still be 0", empty.capacity(), 0 );
	}

	@Test
	public void testTransferByteBufferSmallToBig() {
		ByteBuffer small = ByteBuffer.wrap( smallArray );
		ByteBuffer big = ByteBuffer.wrap( bigArray );
		ByteBufferUtils.transferByteBuffer( small, big );
		assertArrayEquals( "Small bytebuffer should not change", small.array(), smallArray );
		assertEquals( big.get( 0 ), smallArray[0] );
		assertEquals( big.get( 1 ), smallArray[1] );
		assertEquals( big.get( 2 ), smallArray[2] );
		assertEquals( big.get( 3 ), smallArray[3] );
		assertEquals( big.get( 4 ), smallArray[4] );
		assertEquals( big.get( 5 ), bigArray[5] );
		assertEquals( big.get( 6 ), bigArray[6] );
		assertEquals( big.get( 7 ), bigArray[7] );
		assertEquals( big.get( 8 ), bigArray[8] );
	}

	@Test
	public void testTransferByteBufferBigToSmall() {
		ByteBuffer small = ByteBuffer.wrap( smallArray );
		ByteBuffer big = ByteBuffer.wrap( bigArray );
		ByteBufferUtils.transferByteBuffer( big, small );
		assert ( small.get( 0 ) == bigArray[0] );
		assert ( small.get( 1 ) == bigArray[1] );
		assert ( small.get( 2 ) == bigArray[2] );
		assert ( small.get( 3 ) == bigArray[3] );
		assert ( small.get( 4 ) == bigArray[4] );
		assert ( big.array() == bigArray );
	}

	@Test
	public void testTransferByteBufferCheckNull() {
		ByteBuffer source = ByteBufferUtils.getEmptyByteBuffer();
		ByteBuffer dest = ByteBufferUtils.getEmptyByteBuffer();
		ByteBufferUtils.transferByteBuffer( source, null );
	}
}
