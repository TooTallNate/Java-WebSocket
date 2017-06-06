/*
 * Copyright (c) 2010-2017 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.java_websocket.util;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * JUnit Test for the new ByteBufferUtils class
 */
public class ByteBufferUtilsTest {

    /**
     * A small byte array with some data
     */
    private static byte[] smallArray = {0, -1, -2, -3, -4};

    /**
     * A big byte array with some data
     */
    private static byte[] bigArray = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    @Test
    public void testEmptyByteBufferCapacity() {
        ByteBuffer byteBuffer = ByteBufferUtils.getEmptyByteBuffer();
        assertEquals("capacity must be 0", 0, byteBuffer.capacity());
    }

    @Test
    public void testEmptyByteBufferNewObject() {
        ByteBuffer byteBuffer0 = ByteBufferUtils.getEmptyByteBuffer();
        ByteBuffer byteBuffer1 = ByteBufferUtils.getEmptyByteBuffer();
        assertTrue("Allocated new object", byteBuffer0 != byteBuffer1);
    }

    @Test
    public void testTransferByteBufferSmallToEmpty() {
        ByteBuffer small = ByteBuffer.wrap(smallArray);
        ByteBuffer empty = ByteBufferUtils.getEmptyByteBuffer();
        ByteBufferUtils.transferByteBuffer(small, empty);
        assertArrayEquals("Small bytebuffer should not change", smallArray, small.array());
        assertEquals("Capacity of the empty bytebuffer should still be 0", 0, empty.capacity());
    }

    @Test
    public void testTransferByteBufferSmallToBig() {
        ByteBuffer small = ByteBuffer.wrap(smallArray);
        ByteBuffer big = ByteBuffer.wrap(bigArray);
        ByteBufferUtils.transferByteBuffer(small, big);
        assertArrayEquals("Small bytebuffer should not change", smallArray, small.array());
        assertEquals("Big bytebuffer not same to source 0", smallArray[0], big.get(0));
        assertEquals("Big bytebuffer not same to source 1", smallArray[1], big.get(1));
        assertEquals("Big bytebuffer not same to source 2", smallArray[2], big.get(2));
        assertEquals("Big bytebuffer not same to source 3", smallArray[3], big.get(3));
        assertEquals("Big bytebuffer not same to source 4", smallArray[4], big.get(4));
        assertEquals("Big bytebuffer not same to source 5", bigArray[5], big.get(5));
        assertEquals("Big bytebuffer not same to source 6", bigArray[6], big.get(6));
        assertEquals("Big bytebuffer not same to source 7", bigArray[7], big.get(7));
        assertEquals("Big bytebuffer not same to source 8", bigArray[8], big.get(8));
    }

    @Test
    public void testTransferByteBufferBigToSmall() {
        ByteBuffer small = ByteBuffer.wrap(smallArray);
        ByteBuffer big = ByteBuffer.wrap(bigArray);
        ByteBufferUtils.transferByteBuffer(big, small);
        assertArrayEquals("Big bytebuffer should not change", bigArray, big.array());
        assertEquals("Small bytebuffer not same to source 0", bigArray[0], small.get(0));
        assertEquals("Small bytebuffer not same to source 1", bigArray[1], small.get(1));
        assertEquals("Small bytebuffer not same to source 2", bigArray[2], small.get(2));
        assertEquals("Small bytebuffer not same to source 3", bigArray[3], small.get(3));
        assertEquals("Small bytebuffer not same to source 4", bigArray[4], small.get(4));
    }

    @Test
    public void testTransferByteBufferCheckNullDest() {
        ByteBuffer source = ByteBuffer.wrap(smallArray);
        try {
            ByteBufferUtils.transferByteBuffer(source, null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //Fine
        }
    }

    @Test
    public void testTransferByteBufferCheckNullSource() {
        ByteBuffer dest = ByteBuffer.wrap(smallArray);
        try {
            ByteBufferUtils.transferByteBuffer(null, dest);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //Fine
        }
    }

    @Test
    public void testTransferByteBufferCheckNullBoth() {
        try {
            ByteBufferUtils.transferByteBuffer(null, null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //Fine
        }
    }
}
