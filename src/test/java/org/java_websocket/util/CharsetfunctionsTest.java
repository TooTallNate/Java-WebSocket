/*
 * Copyright (c) 2010-2020 Nathan Rajlich
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

import java.nio.ByteBuffer;
import org.java_websocket.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CharsetfunctionsTest {

  @Test
  public void testAsciiBytes() {
    assertArrayEquals(new byte[]{102, 111, 111}, Charsetfunctions.asciiBytes("foo"));
  }

  @Test
  public void testStringUtf8ByteBuffer() throws InvalidDataException {
    assertEquals("foo",
        Charsetfunctions.stringUtf8(ByteBuffer.wrap(new byte[]{102, 111, 111})));
  }


  @Test
  public void testIsValidUTF8off() {
    assertFalse(Charsetfunctions.isValidUTF8(ByteBuffer.wrap(new byte[]{100}), 2));
    assertFalse(Charsetfunctions.isValidUTF8(ByteBuffer.wrap(new byte[]{(byte) 128}), 0));

    assertTrue(Charsetfunctions.isValidUTF8(ByteBuffer.wrap(new byte[]{100}), 0));
  }

  @Test
  public void testIsValidUTF8() {
    assertFalse(Charsetfunctions.isValidUTF8(ByteBuffer.wrap(new byte[]{(byte) 128})));

    assertTrue(Charsetfunctions.isValidUTF8(ByteBuffer.wrap(new byte[]{100})));
  }

  @Test
  public void testStringAscii1() {
    assertEquals("oBar",
        Charsetfunctions.stringAscii(new byte[]{102, 111, 111, 66, 97, 114}, 2, 4));

  }

  @Test
  public void testStringAscii2() {
    assertEquals("foo", Charsetfunctions.stringAscii(new byte[]{102, 111, 111}));
  }

  @Test
  public void testUtf8Bytes() {
    assertArrayEquals(new byte[]{102, 111, 111, 66, 97, 114},
        Charsetfunctions.utf8Bytes("fooBar"));
  }
}
