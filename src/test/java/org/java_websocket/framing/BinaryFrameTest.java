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


import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test for the BinaryFrame class
 */
public class BinaryFrameTest {

  @Test
  public void testConstructor() {
    BinaryFrame frame = new BinaryFrame();
    assertEquals(Opcode.BINARY, frame.getOpcode(), "Opcode must be equal");
      assertTrue(frame.isFin(), "Fin must be set");
      assertFalse(frame.getTransfereMasked(), "TransferedMask must not be set");
    assertEquals(0, frame.getPayloadData().capacity(), "Payload must be empty");
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
    BinaryFrame frame = new BinaryFrame();
      assertInstanceOf(DataFrame.class, frame, "Frame must extend dataframe");
  }

  @Test
  public void testIsValid() {
    BinaryFrame frame = new BinaryFrame();
    try {
      frame.isValid();
    } catch (InvalidDataException e) {
      fail("InvalidDataException should not be thrown");
    }
  }
}
