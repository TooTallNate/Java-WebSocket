/*
 * Copyright (c) 2010-2018 Nathan Rajlich
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
import org.java_websocket.framing.ControlFrame;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JUnit Test for the InvalidEncodingException class
 */
public class LimitExceededExceptionTest {

    @Test
    public void testConstructor() {
        LimitExceededException limitExceededException = new LimitExceededException();
        assertEquals("The close code has to be TOOBIG", CloseFrame.TOOBIG, limitExceededException.getCloseCode());
        assertEquals("The message has to be empty", null, limitExceededException.getMessage());
        limitExceededException = new LimitExceededException("Message");
        assertEquals("The close code has to be TOOBIG", CloseFrame.TOOBIG, limitExceededException.getCloseCode());
        assertEquals("The message has to be the argument", "Message", limitExceededException.getMessage());
    }

    @Test
    public void testExtends() {
        LimitExceededException limitExceededException = new LimitExceededException();
        assertEquals("LimitExceededException must extend InvalidDataException", true, limitExceededException instanceof InvalidDataException);
    }
}
