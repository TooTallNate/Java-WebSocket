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

package org.java_websocket.issues;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Issue713Test {

	CountDownLatch countDownLatchString = new CountDownLatch( 10 );
	CountDownLatch countDownLatchConnect = new CountDownLatch( 10 );
	CountDownLatch countDownLatchBytebuffer = new CountDownLatch( 10 );

	@Test
	public void testIllegalArgument() throws IOException {
		WebSocketServer server = new WebSocketServer( new InetSocketAddress( SocketUtil.getAvailablePort() ) ) {
			@Override
			public void onOpen(WebSocket conn, ClientHandshake handshake) {

			}

			@Override
			public void onClose(WebSocket conn, int code, String reason, boolean remote) {

			}

			@Override
			public void onMessage(WebSocket conn, String message) {

			}

			@Override
			public void onError(WebSocket conn, Exception ex) {

			}

			@Override
			public void onStart() {

			}
		};
		try {
			server.broadcast((byte[]) null, null);
			fail("IllegalArgumentException should be thrown");
		} catch (Exception e) {
			// OK
		}
		try {
			server.broadcast((String) null, null);
			fail("IllegalArgumentException should be thrown");
		} catch (Exception e) {
			// OK
		}
	}

	@Test(timeout=2000)
	public void testIssue() throws Exception {
		final int port = SocketUtil.getAvailablePort();
		WebSocketServer server = new WebSocketServer( new InetSocketAddress( port ) ) {
			@Override
			public void onOpen(WebSocket conn, ClientHandshake handshake ) {
			}

			@Override
			public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
			}

			@Override
			public void onMessage( WebSocket conn, String message ) {

			}

			@Override
			public void onError( WebSocket conn, Exception ex ) {

			}

			@Override
			public void onStart() {
				try {
					for (int i = 0; i < 10; i++) {
						TestWebSocket tw = new TestWebSocket(port);
						tw.connect();
					}
				} catch (Exception e) {
					fail("Exception during connect!");
				}
			}
		};
		server.start();
		countDownLatchConnect.await();
		server.broadcast("Hello world!");
		countDownLatchString.await();
		server.broadcast("Hello world".getBytes());
		countDownLatchBytebuffer.await();
		countDownLatchBytebuffer = new CountDownLatch( 10 );
		server.broadcast(ByteBuffer.wrap("Hello world".getBytes()));
		countDownLatchBytebuffer.await();


		countDownLatchString = new CountDownLatch( 5 );
		ArrayList<WebSocket> specialList = new ArrayList<WebSocket>(server.getConnections());
		specialList.remove(8);
		specialList.remove(6);
		specialList.remove(4);
		specialList.remove(2);
		specialList.remove(0);
		server.broadcast("Hello world", specialList);
		countDownLatchString.await();

		countDownLatchBytebuffer = new CountDownLatch( 5 );
		server.broadcast("Hello world".getBytes());
		countDownLatchBytebuffer.await();
	}


	class TestWebSocket extends WebSocketClient {

		TestWebSocket(int port) throws URISyntaxException {
			super(new URI( "ws://localhost:" + port));
		}

		@Override
		public void onOpen( ServerHandshake handshakedata ) {
			countDownLatchConnect.countDown();
		}

		@Override
		public void onMessage( String message ) {
			countDownLatchString.countDown();
		}
		@Override
		public void onMessage( ByteBuffer message ) {
			countDownLatchBytebuffer.countDown();
		}

		@Override
		public void onClose( int code, String reason, boolean remote ) {

		}

		@Override
		public void onError( Exception ex ) {

		}
	}
}
