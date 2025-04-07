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


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit Test for the InvalidDataException class
 */
public class InvalidDataExceptionTest {

  @Test
  public void testConstructor() {
    InvalidDataException invalidDataException = new InvalidDataException(42);
    assertEquals(42, invalidDataException.getCloseCode(), "The close code has to be the argument");
    invalidDataException = new InvalidDataException(42, "Message");
    assertEquals(42, invalidDataException.getCloseCode(), "The close code has to be the argument");
    assertEquals( "Message",
        invalidDataException.getMessage(), "The message has to be the argument");
    Exception e = new Exception();
    invalidDataException = new InvalidDataException(42, "Message", e);
    assertEquals( 42, invalidDataException.getCloseCode(), "The close code has to be the argument");
    assertEquals( "Message",
        invalidDataException.getMessage(), "The message has to be the argument");
    assertEquals(e, invalidDataException.getCause(), "The throwable has to be the argument");
    invalidDataException = new InvalidDataException(42, e);
    assertEquals(42, invalidDataException.getCloseCode(), "The close code has to be the argument");
    assertEquals(e, invalidDataException.getCause(), "The throwable has to be the argument");
  }
}
