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

package org.java_websocket.issues;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.Opcode;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

public class Issue598Test {

	private static List<IProtocol> protocolList = Collections.<IProtocol>singletonList( new Protocol( "" ));
	private static List<IExtension> extensionList = Collections.<IExtension>emptyList();

	private static void runTestScenario(int testCase) throws Exception {
		final CountDownLatch countServerDownLatch = new CountDownLatch( 1 );
		final CountDownLatch countReceiveDownLatch = new CountDownLatch( 1 );
		final CountDownLatch countCloseDownLatch = new CountDownLatch( 1 );
		int port = SocketUtil.getAvailablePort();
		Draft draft = null;
		switch (testCase) {
			case 0:
			case 1:
			case 2:
			case 3:
			 	draft = new Draft_6455(extensionList, protocolList, 10);
				break;
			case 4:
			case 5:
			case 6:
			case 7:
				draft = new Draft_6455(extensionList, protocolList, 9);
				break;
		}
		WebSocketClient webSocket = new WebSocketClient( new URI( "ws://localhost:" + port )) {
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
		WebSocketServer server = new WebSocketServer( new InetSocketAddress( port ) , Collections.singletonList(draft)) {
			@Override
			public void onOpen( WebSocket conn, ClientHandshake handshake ) {
			}

			@Override
			public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
				if (code == CloseFrame.TOOBIG) {
					countCloseDownLatch.countDown();
				}
			}

			@Override
			public void onMessage( WebSocket conn, String message ) {
				countReceiveDownLatch.countDown();
			}

			@Override
			public void onMessage( WebSocket conn, ByteBuffer message ) {
				countReceiveDownLatch.countDown();
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
		switch (testCase) {
			case 0:
			case 4:
				byte[] bArray = new byte[10];
				for (byte i = 0; i < 10; i++) {
					bArray[i] = i;
				}
				webSocket.send(ByteBuffer.wrap(bArray));
				if (testCase == 0)
					countReceiveDownLatch.await();
				if (testCase == 4)
					countCloseDownLatch.await();
				break;
			case 2:
			case 6:
				bArray = "0123456789".getBytes();
				webSocket.send(ByteBuffer.wrap(bArray));
				if (testCase == 2)
					countReceiveDownLatch.await();
				if (testCase == 6)
					countCloseDownLatch.await();
				break;
			case 1:
			case 5:
				bArray = new byte[2];
				for (byte i = 0; i < 10; i++) {
					bArray[i%2] = i;
					if (i % 2 == 1)
						webSocket.sendFragmentedFrame(Opcode.BINARY, ByteBuffer.wrap(bArray), i == 9);
				}
				if (testCase == 1)
					countReceiveDownLatch.await();
				if (testCase == 5)
					countCloseDownLatch.await();
				break;
			case 3:
			case 7:
				for (byte i = 0; i < 10; i++) {
					webSocket.sendFragmentedFrame(Opcode.TEXT, ByteBuffer.wrap((Integer.toString(i)).getBytes()), i == 9);
				}
				if (testCase == 3)
					countReceiveDownLatch.await();
				if (testCase == 7)
					countCloseDownLatch.await();
				break;
		}
		server.stop();
	}

	@Test(timeout = 2000)
	public void runBelowLimitBytebuffer() throws Exception {
		runTestScenario(0);
	}

	@Test(timeout = 2000)
	public void runBelowSplitLimitBytebuffer() throws Exception {
		runTestScenario(1);
	}

	@Test(timeout = 2000)
	public void runBelowLimitString() throws Exception {
		runTestScenario(2);
	}

	@Test(timeout = 2000)
	public void runBelowSplitLimitString() throws Exception {
		runTestScenario(3);
	}

	@Test
			//(timeout = 2000)
	public void runAboveLimitBytebuffer() throws Exception {
		runTestScenario(4);
	}

	@Test(timeout = 2000)
	public void runAboveSplitLimitBytebuffer() throws Exception {
		runTestScenario(5);
	}

	@Test(timeout = 2000)
	public void runAboveLimitString() throws Exception {
		runTestScenario(6);
	}

	@Test(timeout = 2000)
	public void runAboveSplitLimitString() throws Exception {
		runTestScenario(7);
	}
}
