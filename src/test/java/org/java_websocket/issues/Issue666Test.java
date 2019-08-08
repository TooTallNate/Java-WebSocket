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

package org.java_websocket.issues;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.java_websocket.util.ThreadCheck;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Issue666Test {
	private CountDownLatch countServerDownLatch = new CountDownLatch( 1 );

	@Test
	public void testServer() throws Exception {
		Map<Long, Thread> mapBefore = ThreadCheck.getThreadMap();
		int port = SocketUtil.getAvailablePort();
		WebSocketServer server = new WebSocketServer( new InetSocketAddress( port ) ) {
			@Override
			public void onOpen( WebSocket conn, ClientHandshake handshake ) {
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
		Map<Long, Thread> mapAfter = ThreadCheck.getThreadMap();
		for( long key : mapBefore.keySet() ) {
			mapAfter.remove( key );
		}
		for( Thread thread : mapAfter.values() ) {
			String name = thread.getName();
			if( !name.startsWith( "WebSocketSelector-" ) && !name.startsWith( "WebSocketWorker-" ) && !name.startsWith( "connectionLostChecker-" ) ) {
				Assert.fail( "Thread not correctly named! Is: " + name );
			}
		}
		server.stop();
	}

	@Test
	public void testClient() throws Exception {
		int port = SocketUtil.getAvailablePort();
		WebSocketClient client = new WebSocketClient( new URI( "ws://localhost:" + port ) ) {
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
		WebSocketServer server = new WebSocketServer( new InetSocketAddress( port ) ) {
			@Override
			public void onOpen( WebSocket conn, ClientHandshake handshake ) {
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
		Map<Long, Thread> mapBefore = ThreadCheck.getThreadMap();
		client.connectBlocking();
		Map<Long, Thread> mapAfter = ThreadCheck.getThreadMap();
		for( long key : mapBefore.keySet() ) {
			mapAfter.remove( key );
		}
		for( Thread thread : mapAfter.values() ) {
			String name = thread.getName();
			if( !name.startsWith( "connectionLostChecker-" ) && !name.startsWith( "WebSocketWriteThread-" ) && !name.startsWith( "WebSocketConnectReadThread-" )) {
				Assert.fail( "Thread not correctly named! Is: " + name );
			}
		}
		client.close();
		server.stop();
	}
}
