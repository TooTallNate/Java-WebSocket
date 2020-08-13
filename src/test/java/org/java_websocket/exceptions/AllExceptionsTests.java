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


import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.java_websocket.exceptions.IncompleteExceptionTest.class,
    org.java_websocket.exceptions.IncompleteHandshakeExceptionTest.class,
    org.java_websocket.exceptions.InvalidDataExceptionTest.class,
    org.java_websocket.exceptions.InvalidEncodingExceptionTest.class,
    org.java_websocket.exceptions.InvalidFrameExceptionTest.class,
    org.java_websocket.exceptions.InvalidHandshakeExceptionTest.class,
    org.java_websocket.exceptions.LimitExceededExceptionTest.class,
    org.java_websocket.exceptions.NotSendableExceptionTest.class,
    org.java_websocket.exceptions.WebsocketNotConnectedExceptionTest.class
})
/**
 * Start all tests for the exceptions
 */
public class AllExceptionsTests {

}
