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
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.java_websocket.util.ThreadCheck;
import org.junit.Rule;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Issue661Test {

	@Rule
	public ThreadCheck zombies = new ThreadCheck();

	private CountDownLatch countServerDownLatch = new CountDownLatch( 1 );

	private boolean wasError = false;

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

	//@Test(timeout =  2000)
	public void testIssue() throws Exception {
		System.setErr( new TestPrintStream( System.err ) );
		int port = SocketUtil.getAvailablePort();
		WebSocketServer server0 = new WebSocketServer( new InetSocketAddress( port ) ) {
			@Override
			public void onOpen( WebSocket conn, ClientHandshake handshake ) {
				fail( "There should be no onOpen" );
			}

			@Override
			public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
				fail( "There should be no onClose" );
			}

			@Override
			public void onMessage( WebSocket conn, String message ) {
				fail( "There should be no onMessage" );
			}

			@Override
			public void onError( WebSocket conn, Exception ex ) {
				fail( "There should be no onError!" );
			}

			@Override
			public void onStart() {
				countServerDownLatch.countDown();
			}
		};
		server0.start();
		try {
			countServerDownLatch.await();
		} catch ( InterruptedException e ) {
			//
		}
		WebSocketServer server1 = new WebSocketServer( new InetSocketAddress( port ) ) {
			@Override
			public void onOpen( WebSocket conn, ClientHandshake handshake ) {
				fail( "There should be no onOpen" );
			}

			@Override
			public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
				fail( "There should be no onClose" );
			}

			@Override
			public void onMessage( WebSocket conn, String message ) {
				fail( "There should be no onMessage" );
			}

			@Override
			public void onError( WebSocket conn, Exception ex ) {
				if( !( ex instanceof BindException ) ) {
					fail( "There should be no onError" );
				}
			}

			@Override
			public void onStart() {
				fail( "There should be no onStart!" );
			}
		};
		server1.start();
		Thread.sleep( 1000 );
		server1.stop();
		server0.stop();
		Thread.sleep( 100 );
		assertTrue( "There was an error using System.err", !wasError );
	}
}
