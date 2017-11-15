/*
 * Copyright (c) 2010-2017 Nathan Rajlich
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

package org.java_websocket.protocols;

/**
 * Interface which specifies all required methods for a Sec-WebSocket-Protocol
 */
public interface IProtocol {

	/**
	 * Check if the received Sec-WebSocket-Protocol header field contains a offer for the specific protocol
	 *
	 * @param inputProtocolHeader the received Sec-WebSocket-Protocol header field offered by the other endpoint
	 * @return true, if the offer does fit to this specific protocol
	 */
	boolean acceptProvidedProtocol( String inputProtocolHeader );

	/**
	 * Return the specific Sec-WebSocket-protocol header offer for this protocol if the endpoint.
	 * If the extension returns an empty string (""), the offer will not be included in the handshake.
	 *
	 * @return the specific Sec-WebSocket-Protocol header for this protocol
	 */
	String getProvidedProtocol();

	IProtocol copyInstance();
}
