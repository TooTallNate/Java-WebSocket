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

package org.java_websocket.example;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;

public class AutobahnServerTest extends WebSocketServer {

	private static int openCounter = 0;
	private static int closeCounter = 0;
	private int limit = Integer.MAX_VALUE;

	public AutobahnServerTest(int port, int limit, Draft d) throws UnknownHostException {
		super( new InetSocketAddress( port ), Collections.singletonList( d ) );
		this.limit = limit;
	}

	public AutobahnServerTest( InetSocketAddress address, Draft d ) {
		super( address, Collections.singletonList( d ) );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		openCounter++;
		System.out.println( "///////////Opened connection number" + openCounter);
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		closeCounter++;
		System.out.println( "closed" );
		if (closeCounter >= limit) {
			System.exit(0);
		}
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		System.out.println( "Error:" );
		ex.printStackTrace();
	}

	@Override
	public void onStart() {
		System.out.println( "Server started!" );
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		conn.send( message );
	}

	@Override
	public void onMessage( WebSocket conn, ByteBuffer blob ) {
		conn.send( blob );
	}

	public static void main( String[] args ) throws UnknownHostException {
		int port, limit;
		try {
			port = new Integer( args[0] );
		} catch ( Exception e ) {
			System.out.println( "No port specified. Defaulting to 9003" );
			port = 9003;
		}
		try {
			limit = new Integer( args[1] );
		} catch ( Exception e ) {
			System.out.println( "No limit specified. Defaulting to MaxInteger" );
			limit = Integer.MAX_VALUE;
		}
		AutobahnServerTest test = new AutobahnServerTest( port, limit, new Draft_6455() );
		test.setConnectionLostTimeout( 0 );
		test.start();
	}

}
