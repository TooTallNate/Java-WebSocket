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

package org.java_websocket.exceptions;

import org.java_websocket.framing.CloseFrame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test for the InvalidHandshakeException class
 */
public class InvalidHandshakeExceptionTest {

  @Test
  public void testConstructor() {
    InvalidHandshakeException invalidHandshakeException = new InvalidHandshakeException();
    assertEquals( CloseFrame.PROTOCOL_ERROR,
        invalidHandshakeException.getCloseCode(), "The close code has to be PROTOCOL_ERROR");
    invalidHandshakeException = new InvalidHandshakeException("Message");
    assertEquals( CloseFrame.PROTOCOL_ERROR,
        invalidHandshakeException.getCloseCode(), "The close code has to be PROTOCOL_ERROR");
    assertEquals( "Message",
        invalidHandshakeException.getMessage(), "The message has to be the argument");
    Exception e = new Exception();
    invalidHandshakeException = new InvalidHandshakeException("Message", e);
    assertEquals(CloseFrame.PROTOCOL_ERROR,
        invalidHandshakeException.getCloseCode(), "The close code has to be PROTOCOL_ERROR");
    assertEquals( "Message",
        invalidHandshakeException.getMessage(), "The message has to be the argument");
    assertEquals(e, invalidHandshakeException.getCause(), "The throwable has to be the argument");
    invalidHandshakeException = new InvalidHandshakeException(e);
    assertEquals(CloseFrame.PROTOCOL_ERROR,
        invalidHandshakeException.getCloseCode(), "The close code has to be PROTOCOL_ERROR");
    assertEquals(e, invalidHandshakeException.getCause(), "The throwable has to be the argument");

  }

  @Test
  public void testExtends() {
    InvalidHandshakeException invalidHandshakeException = new InvalidHandshakeException();
      assertInstanceOf(InvalidDataException.class, invalidHandshakeException, "InvalidHandshakeException must extend InvalidDataException");
  }
}
