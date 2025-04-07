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
 * JUnit Test for the PongFrame class
 */
public class PongFrameTest {

  @Test
  public void testConstructor() {
    PongFrame frame = new PongFrame();
    assertEquals(Opcode.PONG, frame.getOpcode(), "Opcode must be equal");
      assertTrue(frame.isFin(), "Fin must be set");
      assertFalse(frame.getTransfereMasked(), "TransferedMask must not be set");
    assertEquals( 0, frame.getPayloadData().capacity(),"Payload must be empty");
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
  public void testCopyConstructor() {
    PingFrame pingFrame = new PingFrame();
    pingFrame.setPayload(ByteBuffer.allocate(100));
    PongFrame pongFrame = new PongFrame(pingFrame);
    assertEquals( pingFrame.getPayloadData(), pongFrame.getPayloadData(), "Payload must be equal");
  }

  @Test
  public void testExtends() {
    PongFrame frame = new PongFrame();
      assertInstanceOf(ControlFrame.class, frame, "Frame must extend dataframe");
  }

  @Test
  public void testIsValid() {
    PongFrame frame = new PongFrame();
    try {
      frame.isValid();
    } catch (InvalidDataException e) {
      fail("InvalidDataException should not be thrown");
    }
    frame.setFin(false);
    try {
      frame.isValid();
      fail("InvalidDataException should be thrown");
    } catch (InvalidDataException e) {
      //Fine
    }
    frame.setFin(true);
    frame.setRSV1(true);
    try {
      frame.isValid();
      fail("InvalidDataException should be thrown");
    } catch (InvalidDataException e) {
      //Fine
    }
    frame.setRSV1(false);
    frame.setRSV2(true);
    try {
      frame.isValid();
      fail("InvalidDataException should be thrown");
    } catch (InvalidDataException e) {
      //Fine
    }
    frame.setRSV2(false);
    frame.setRSV3(true);
    try {
      frame.isValid();
      fail("InvalidDataException should be thrown");
    } catch (InvalidDataException e) {
      //Fine
    }
  }
}
