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

package org.java_websocket.framing;


import java.nio.ByteBuffer;
import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test for the TextFrame class
 */
public class TextFrameTest {

  @Test
  public void testConstructor() {
    TextFrame frame = new TextFrame();
    assertEquals(Opcode.TEXT, frame.getOpcode(), "Opcode must be equal");
      assertTrue(frame.isFin(), "Fin must be set");
      assertFalse(frame.getTransfereMasked(), "TransferedMask must not be set");
    assertEquals( 0, frame.getPayloadData().capacity(), "Payload must be empty");
      assertFalse(frame.isRSV1(), "RSV1 must be false");
      assertFalse(frame.isRSV2(), "RSV2 must be false");
      assertFalse(frame.isRSV3(), "RSV3 must be false");
    try {
      frame.isValid();
    } catch (InvalidDataException e) {
      fail("InvalidDataException should not be thrown");
    }
  }

  @Test
  public void testExtends() {
    TextFrame frame = new TextFrame();
      assertInstanceOf(DataFrame.class, frame, "Frame must extend dataframe");
  }

  @Test
  public void testIsValid() {
    TextFrame frame = new TextFrame();
    try {
      frame.isValid();
    } catch (InvalidDataException e) {
      fail("InvalidDataException should not be thrown");
    }

    frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(new byte[]{
        (byte) 0xD0, (byte) 0x9F, // 'П'
        (byte) 0xD1, (byte) 0x80, // 'р'
        (byte) 0xD0,              // corrupted UTF-8, was 'и'
        (byte) 0xD0, (byte) 0xB2, // 'в'
        (byte) 0xD0, (byte) 0xB5, // 'е'
        (byte) 0xD1, (byte) 0x82  // 'т'
    }));
    try {
      frame.isValid();
      fail("InvalidDataException should be thrown");
    } catch (InvalidDataException e) {
      //Utf8 Check should work
    }
  }
}
