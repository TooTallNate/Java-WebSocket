/*
 * Copyright (c) 2010-2019 Nathan Rajlich
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

/**
 * exception which indicates the frame payload is not sendable
 */
public class NotSendableException extends RuntimeException {

    /**
     * Serializable
     */
    private static final long serialVersionUID = -6468967874576651628L;

    /**
     * constructor for a NotSendableException
     *
     * @param s the detail message.
     */
    public NotSendableException(String s) {
        super(s);
    }

    /**
     * constructor for a NotSendableException
     *
     * @param t the throwable causing this exception.
     */
    public NotSendableException(Throwable t) {
        super(t);
    }

    /**
     * constructor for a NotSendableException
     *
     * @param s the detail message.
     * @param t the throwable causing this exception.
     */
    public NotSendableException(String s, Throwable t) {
        super(s, t);
    }

}
