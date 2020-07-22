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

package org.java_websocket.client;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HeadersTest {

	@Test
	public void testHttpHeaders() throws URISyntaxException {
		Map<String, String> httpHeaders = new HashMap<String, String>();
		httpHeaders.put("Cache-Control", "only-if-cached");
		httpHeaders.put("Keep-Alive", "1000");

		WebSocketClient client = new WebSocketClient(new URI( "ws://localhost"), httpHeaders) {
			@Override
			public void onOpen( ServerHandshake handshakedata ) {

			}

			@Override
			public void onMessage( String message ) {

			}

			@Override
			public void onClose( int code, String reason, boolean remote ) {

			}

			@Override
			public void onError( Exception ex ) {

			}
		};
		
		assertEquals("only-if-cached", client.removeHeader("Cache-Control"));
		assertEquals("1000", client.removeHeader("Keep-Alive"));
	}

	@Test
	public void test_Add_RemoveHeaders() throws URISyntaxException {
		Map<String, String> httpHeaders = null;
		WebSocketClient client = new WebSocketClient(new URI( "ws://localhost"), httpHeaders) {
			@Override
			public void onOpen( ServerHandshake handshakedata ) {

			}

			@Override
			public void onMessage( String message ) {

			}

			@Override
			public void onClose( int code, String reason, boolean remote ) {

			}

			@Override
			public void onError( Exception ex ) {

			}
		};
		client.addHeader("Cache-Control", "only-if-cached");
		assertEquals("only-if-cached", client.removeHeader("Cache-Control"));
		assertNull(client.removeHeader("Cache-Control"));

		client.addHeader("Cache-Control", "only-if-cached");
		client.clearHeaders();
		assertNull(client.removeHeader("Cache-Control"));
	}

	@Test
	public void testGetURI() throws URISyntaxException {
		WebSocketClient client = new WebSocketClient(new URI( "ws://localhost")) {
			@Override
			public void onOpen( ServerHandshake handshakedata ) {

			}

			@Override
			public void onMessage( String message ) {

			}

			@Override
			public void onClose( int code, String reason, boolean remote ) {

			}

			@Override
			public void onError( Exception ex ) {

			}
		};
		String actualURI = client.getURI().getScheme() + "://" + client.getURI().getHost();
		
		assertEquals("ws://localhost", actualURI);
	}
}
