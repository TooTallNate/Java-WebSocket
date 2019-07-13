/*
 * Copyright (c) 2010-2019 Nathan Rajlich
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.java_websocket.exceptions;

import org.java_websocket.WebSocket;

import java.io.IOException;

/**
 * Exception to wrap an IOException and include information about the websocket which had the exception
 * @since 1.4.1
 */
public class WrappedIOException extends Exception {

    /**
     * The websocket where the IOException happened
     */
    private final WebSocket connection;

    /**
     * The IOException
     */
    private final IOException ioException;

    /**
     * Wrapp an IOException and include the websocket
     * @param connection the websocket where the IOException happened
     * @param ioException the IOException
     */
    public WrappedIOException(WebSocket connection, IOException ioException) {
        this.connection = connection;
        this.ioException = ioException;
    }

    /**
     * The websocket where the IOException happened
     * @return the websocket for the wrapped IOException
     */
    public WebSocket getConnection() {
        return connection;
    }

    /**
     * The wrapped IOException
     * @return IOException which is wrapped
     */
    public IOException getIOException() {
        return ioException;
    }
}
