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
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.java_websocket.util.ThreadCheck;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(Parameterized.class)
public class Issue580Test {

	private static final int NUMBER_OF_TESTS = 10;

	@Parameterized.Parameter
	public int count;


	@Rule
	public ThreadCheck zombies = new ThreadCheck();

	private void runTestScenario(boolean closeBlocking) throws Exception {
		final CountDownLatch countServerDownLatch = new CountDownLatch( 1 );
		int port = SocketUtil.getAvailablePort();
		WebSocketServer ws = new WebSocketServer( new InetSocketAddress( port ) ) {
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
		ws.start();
		countServerDownLatch.await();
		WebSocketClient clt = new WebSocketClient( new URI( "ws://localhost:" + port ) ) {
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
		clt.connectBlocking();
		clt.send("test");
		if (closeBlocking) {
			clt.closeBlocking();
		}
		ws.stop();
		Thread.sleep( 100 );
	}

	@Parameterized.Parameters
	public static Collection<Integer[]> data() {
		List<Integer[]> ret = new ArrayList<Integer[]>(NUMBER_OF_TESTS);
		for (int i = 0; i < NUMBER_OF_TESTS; i++) ret.add(new Integer[]{i});
		return ret;
	}

	@Test
	public void runNoCloseBlockingTestScenario() throws Exception {
		runTestScenario(false);
	}
	@Test
	public void runCloseBlockingTestScenario() throws Exception {
		runTestScenario(true);
	}
}

