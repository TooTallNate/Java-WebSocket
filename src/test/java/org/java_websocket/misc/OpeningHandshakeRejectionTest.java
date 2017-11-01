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

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Charsetfunctions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Scanner;

import static org.junit.Assert.fail;

public class OpeningHandshakeRejectionTest {

	private static final int testCases = 12;
	private static final String additionalHandshake = "Upgrade: websocket\r\nConnection: Upgrade\r\n\r\n";
	private static int counter = 0;
	private static Thread thread;

	private static boolean debugPrintouts = false;

	@BeforeClass
	public static void startServer() {
		thread = new Thread(
				new Runnable() {
					public void run() {
						try {
							ServerSocket server = new ServerSocket( 8887 );
							int count = 1;
							while( true ) {
								Socket client = null;
								try {
									client = server.accept();
									Scanner in = new Scanner( client.getInputStream() );
									String input = in.nextLine();
									String testCase = input.split( " " )[1];
									OutputStream os = client.getOutputStream();
									count++;
									if( "/0".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 100 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/1".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.0 100 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/2".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP 100 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/3".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 200 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/4".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP 101 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/5".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 404 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/6".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/2.0 404 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/7".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 500 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/8".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "GET 302 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/9".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "GET HTTP/1.1 101 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/10".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 101 Switching Protocols\r\n" + additionalHandshake ) );
										os.flush();
									}
									if( "/11".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( "HTTP/1.1 101 Websocket Connection Upgrade\r\n" + additionalHandshake ) );
										os.flush();
									}
								} catch ( IOException e ) {
									fail( "There should be no exception" );
								}
							}
						} catch ( Exception e ) {
							fail( "There should be no exception" );
						}
					}
				} );
		thread.start();
	}

	@AfterClass
	public static void successTests() throws InterruptedException {
		thread.interrupt();
		if (debugPrintouts)
		System.out.println( counter + " successful tests" );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase0() {
		testHandshakeRejection( 0 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase1() {
		testHandshakeRejection( 1 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase2() {
		testHandshakeRejection( 2 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase3() {
		testHandshakeRejection( 3 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase4() {
		testHandshakeRejection( 4 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase5() {
		testHandshakeRejection( 5 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase6() {
		testHandshakeRejection( 6 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase7() {
		testHandshakeRejection( 7 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase8() {
		testHandshakeRejection( 8 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase9() {
		testHandshakeRejection( 9 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase10() {
		testHandshakeRejection( 10 );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase11() {
		testHandshakeRejection( 11 );
	}

	private void testHandshakeRejection( int i ) {
		try {
			final int finalI = i;
			WebSocketClient webSocketClient = new WebSocketClient( new URI( "ws://localhost:8887/" + finalI ) ) {
				@Override
				public void onOpen( ServerHandshake handshakedata ) {
					fail( "There should not be a connection!" );
				}

				@Override
				public void onMessage( String message ) {
					fail( "There should not be a message!" );
				}

				@Override
				public void onClose( int code, String reason, boolean remote ) {
					if( finalI != 10 && finalI != 11 ) {
						if( code != CloseFrame.PROTOCOL_ERROR ) {
							fail( "There should be a protocol error!" );
						} else if (reason.startsWith( "Invalid status code received:") || reason.startsWith( "Invalid status line received:")) {
							if (debugPrintouts)
							System.out.println("Protocol error for test case: " + finalI);
							counter++;
						} else {
							fail( "The reason should be included!" );
						}
					} else {
						//Since we do not include a correct Sec-WebSocket-Accept, onClose will be called with reason 'Draft refuses handshake'
						if (!reason.endsWith( "refuses handshake" )) {
							fail( "onClose should not be called!" );
						} else {
							if (debugPrintouts)
							System.out.println("Refuses handshake error for test case: " + finalI);
							counter++;
						}
					}
				}

				@Override
				public void onError( Exception ex ) {
					fail( "There should not be an exception" );
				}
			};
			webSocketClient.connectBlocking();
		} catch ( Exception e ) {
			e.printStackTrace();
			fail( "There should be no exception" );
		}
	}
}
