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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test for the FramedataImpl1 class
 */
public class FramedataImpl1Test {

  @Test
  public void testDefaultValues() {
    FramedataImpl1 binary = FramedataImpl1.get(Opcode.BINARY);
    assertEquals(Opcode.BINARY, binary.getOpcode(), "Opcode must be equal");
      assertTrue(binary.isFin(), "Fin must be set");
      assertFalse(binary.getTransfereMasked(), "TransferedMask must not be set");
    assertEquals( 0, binary.getPayloadData().capacity(), "Payload must be empty");
      assertFalse(binary.isRSV1(), "RSV1 must be false");
      assertFalse(binary.isRSV2(), "RSV2 must be false");
      assertFalse(binary.isRSV3(), "RSV3 must be false");
  }

  @Test
  public void testGet() {
    FramedataImpl1 binary = FramedataImpl1.get(Opcode.BINARY);
      assertInstanceOf(BinaryFrame.class, binary, "Frame must be binary");
    FramedataImpl1 text = FramedataImpl1.get(Opcode.TEXT);
      assertInstanceOf(TextFrame.class, text, "Frame must be text");
    FramedataImpl1 closing = FramedataImpl1.get(Opcode.CLOSING);
      assertInstanceOf(CloseFrame.class, closing, "Frame must be closing");
    FramedataImpl1 continuous = FramedataImpl1.get(Opcode.CONTINUOUS);
      assertInstanceOf(ContinuousFrame.class, continuous, "Frame must be continuous");
    FramedataImpl1 ping = FramedataImpl1.get(Opcode.PING);
      assertInstanceOf(PingFrame.class, ping, "Frame must be ping");
    FramedataImpl1 pong = FramedataImpl1.get(Opcode.PONG);
      assertInstanceOf(PongFrame.class, pong, "Frame must be pong");
    try {
      FramedataImpl1.get(null);
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
      //Fine
    }
  }

  @Test
  public void testSetters() {
    FramedataImpl1 frame = FramedataImpl1.get(Opcode.BINARY);
    frame.setFin(false);
      assertFalse(frame.isFin(), "Fin must not be set");
    frame.setTransferemasked(true);
      assertTrue(frame.getTransfereMasked(), "TransferedMask must be set");
    ByteBuffer buffer = ByteBuffer.allocate(100);
    frame.setPayload(buffer);
    assertEquals( 100, frame.getPayloadData().capacity(), "Payload must be of size 100");
    frame.setRSV1(true);
      assertTrue(frame.isRSV1(), "RSV1 must be true");
    frame.setRSV2(true);
      assertTrue(frame.isRSV2(), "RSV2 must be true");
    frame.setRSV3(true);
      assertTrue(frame.isRSV3(), "RSV3 must be true");
  }

  @Test
  public void testAppend() {
    FramedataImpl1 frame0 = FramedataImpl1.get(Opcode.BINARY);
    frame0.setFin(false);
    frame0.setPayload(ByteBuffer.wrap("first".getBytes()));
    FramedataImpl1 frame1 = FramedataImpl1.get(Opcode.BINARY);
    frame1.setPayload(ByteBuffer.wrap("second".getBytes()));
    frame0.append(frame1);
      assertTrue(frame0.isFin(), "Fin must be set");
    assertArrayEquals( "firstsecond".getBytes(),
        frame0.getPayloadData().array(), "Payload must be equal");
  }
}
