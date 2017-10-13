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

package org.java_websocket.misc;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Charsetfunctions;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.fail;

public class OpeningHandshakeRejectionTest {

	/**
	 * How many testcases do we have
	 */
	private static final int testCases = 10;

	public OpeningHandshakeRejectionTest() {
		Thread thread = new Thread(
				new Runnable() {
					public void run() {
						try {
							ServerSocket server = new ServerSocket( 8887 );
							while( true ) {
								Socket client = null;
								try {
									client = server.accept();
									Scanner in = new Scanner( client.getInputStream() );
									String input = in.nextLine();
									String testCase = input.split( " " )[1];
									OutputStream os = client.getOutputStream();
									if( "/0".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 100 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/1".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.0 100 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/2".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP 100 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/3".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 200 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/4".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP 101 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/5".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 404 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/6".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/2.0 404 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/7".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 500 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/8".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "GET 302 Switching Protocols\r\n" ) );
										os.flush();
									}
									if( "/9".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "GET HTTP/1.1 101 Switching Protocols\r\n" ) );
										os.flush();
									}
									client.close();
								} catch ( IOException e ) {
									fail( "There should be no exception" );
								} finally {
									if( client != null )
										client.close();
								}
							}
						} catch ( Exception e ) {
							e.printStackTrace();
							fail( "There should be no exception" );
						}
					}
				} );
		thread.start();
	}

	@Test(timeout=10000)
	public void testClient() {
		try {
			for( int i = 0; i < testCases; i++ ) {
				final CountDownLatch latch = new CountDownLatch( 1 );
				WebSocketClient webSocketClient = new WebSocketClient( new URI( "ws://localhost:8887/" + i ) ) {
					@Override
					public void onOpen( ServerHandshake handshakedata ) {
							fail( "There should not be a connection" );
							latch.countDown();
					}
					@Override
					public void onMessage( String message ) {

					}
					@Override
					public void onClose( int code, String reason, boolean remote ) {
						if( code != CloseFrame.PROTOCOL_ERROR ) {
							fail( "There should be a protocol error" );
						}
						close();
						latch.countDown();
					}

					@Override
					public void onError( Exception ex ) {
						fail( "There should not be an exception" );
					}
				};
				webSocketClient.connect();
				latch.await();
			}
		} catch ( Exception e ) {
			fail( "There should be no exception" );
		}
	}
}
