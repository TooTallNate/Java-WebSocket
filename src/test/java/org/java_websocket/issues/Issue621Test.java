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
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

public class Issue621Test {

	CountDownLatch countDownLatch = new CountDownLatch( 1 );
	CountDownLatch countServerDownLatch = new CountDownLatch( 1 );

	boolean wasError = false;

	class TestPrintStream extends PrintStream {
		public TestPrintStream( OutputStream out ) {
			super( out );
		}

		@Override
		public void println( Object o ) {
			wasError = true;
			super.println( o );
		}
	}

	@Test
	public void testIssue() throws Exception {
		System.setErr( new TestPrintStream( System.err ) );
		int port = SocketUtil.getAvailablePort();
		WebSocketClient webSocket = new WebSocketClient( new URI( "ws://localhost:" + port ) ) {
			@Override
			public void onOpen( ServerHandshake handshakedata ) {

			}

			@Override
			public void onMessage( String message ) {

			}

			@Override
			public void onClose( int code, String reason, boolean remote ) {
				countDownLatch.countDown();
			}

			@Override
			public void onError( Exception ex ) {

			}
		};
		WebSocketServer server = new WebSocketServer( new InetSocketAddress( port ) ) {
			@Override
			public void onOpen( WebSocket conn, ClientHandshake handshake ) {
				conn.close();
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
				countServerDownLatch.countDown();
			}
		};
		server.start();
		countServerDownLatch.await();
		webSocket.connectBlocking();
		countDownLatch.await();
		assertTrue( "There was an error using System.err", !wasError );
		server.stop();
	}
}
