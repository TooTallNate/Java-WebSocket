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

package org.java_websocket.protocols;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Base64;
import org.java_websocket.util.Charsetfunctions;
import org.java_websocket.util.SocketUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import static org.junit.Assert.fail;

public class ProtoclHandshakeRejectionTest {

	private static final String additionalHandshake = "HTTP/1.1 101 Websocket Connection Upgrade\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n";
	private static int counter = 0;
	private static Thread thread;
	private static ServerSocket serverSocket;

	private static boolean debugPrintouts = false;

	private static int port;

	@BeforeClass
	public static void startServer() throws Exception {
		port = SocketUtil.getAvailablePort();
		thread = new Thread(
				new Runnable() {
					public void run() {
						try {
							serverSocket = new ServerSocket( port );
							serverSocket.setReuseAddress( true );
							int count = 1;
							while( true ) {
								Socket client = null;
								try {
									client = serverSocket.accept();
									Scanner in = new Scanner( client.getInputStream() );
									String input = in.nextLine();
									String testCase = input.split( " " )[1];
									String seckey = "";
									String secproc = "";
									while( in.hasNext() ) {
										input = in.nextLine();
										if( input.startsWith( "Sec-WebSocket-Key: " ) ) {
											seckey = input.split( " " )[1];
										}
										if( input.startsWith( "Sec-WebSocket-Protocol: " ) ) {
											secproc = input.split( " " )[1];
										}
										//Last
										if( input.startsWith( "Upgrade" ) ) {
											break;
										}
									}
									OutputStream os = client.getOutputStream();
									count++;
									if( "/0".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "\r\n" ) );
										os.flush();
									}
									if( "/1".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/2".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat, chat2" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/3".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat,chat2,chat3" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/4".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat\r\nSec-WebSocket-Protocol: chat2,chat3" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/5".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "\r\n" ) );
										os.flush();
									}
									if( "/6".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/7".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat, chat2" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/8".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat,chat2,chat3" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/9".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat\r\nSec-WebSocket-Protocol: chat2,chat3" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/10".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat2,chat3" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/11".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat2, chat3" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/12".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat2\r\nSec-WebSocket-Protocol: chat3" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/13".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat2\r\nSec-WebSocket-Protocol: chat" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/14".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat2,chat" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/15".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "Sec-WebSocket-Protocol: chat3" + "\r\n\r\n" ) );
										os.flush();
									}
									if( "/16".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "\r\n" ) );
										os.flush();
									}
									if( "/17".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + getSecKey( seckey ) + "\r\n" ) );
										os.flush();
									}
									if( "/18".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + "Sec-WebSocket-Accept: abc\r\n" + "\r\n" ) );
										os.flush();
									}
									if( "/19".equals( testCase ) ) {
										os.write( Charsetfunctions.asciiBytes( additionalHandshake + "\r\n" ) );
										os.flush();
									}
								} catch ( IOException e ) {
									//
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

	private static String getSecKey( String seckey ) {
		return "Sec-WebSocket-Accept: " + generateFinalKey( seckey ) + "\r\n";
	}

	@AfterClass
	public static void successTests() throws InterruptedException, IOException {
		serverSocket.close();
		thread.interrupt();
		if( debugPrintouts )
			System.out.println( counter + " successful tests" );
	}

	@Test(timeout = 5000)
	public void testProtocolRejectionTestCase0() throws Exception {
		testProtocolRejection( 0, new Draft_6455() );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase1() throws Exception {
		testProtocolRejection( 1, new Draft_6455() );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase2() throws Exception {
		testProtocolRejection( 2, new Draft_6455() );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase3() throws Exception {
		testProtocolRejection( 3, new Draft_6455() );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase4() throws Exception {
		testProtocolRejection( 4, new Draft_6455() );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase5() throws Exception {
		testProtocolRejection( 5, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase6() throws Exception {
		testProtocolRejection( 6, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase7() throws Exception {
		testProtocolRejection( 7, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase8() throws Exception {
		testProtocolRejection( 8, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase9() throws Exception {
		testProtocolRejection( 9, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase10() throws Exception {
		testProtocolRejection( 10, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase11() throws Exception {
		testProtocolRejection( 11, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase12() throws Exception {
		testProtocolRejection( 12, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase13() throws Exception {
		testProtocolRejection( 13, new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase14() throws Exception {
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "chat" ) );
		protocols.add( new Protocol( "chat2" ) );
		testProtocolRejection( 14, new Draft_6455( Collections.<IExtension>emptyList(), protocols ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase15() throws Exception {
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "chat" ) );
		protocols.add( new Protocol( "chat2" ) );
		testProtocolRejection( 15, new Draft_6455( Collections.<IExtension>emptyList(), protocols ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase16() throws Exception {
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "chat" ) );
		protocols.add( new Protocol( "chat2" ) );
		testProtocolRejection( 16, new Draft_6455( Collections.<IExtension>emptyList(), protocols ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase17() throws Exception {
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "chat" ) );
		protocols.add( new Protocol( "" ) );
		testProtocolRejection( 17, new Draft_6455( Collections.<IExtension>emptyList(), protocols ) );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase18() throws Exception {
		testProtocolRejection( 18, new Draft_6455() );
	}

	@Test(timeout = 5000)
	public void testHandshakeRejectionTestCase19() throws Exception {
		testProtocolRejection( 19, new Draft_6455() );
	}

	private void testProtocolRejection( int i, Draft_6455 draft ) throws Exception {
		final int finalI = i;
		final boolean[] threadReturned = { false };
		WebSocketClient webSocketClient = new WebSocketClient( new URI( "ws://localhost:" + port + "/" + finalI ), draft ) {
			@Override
			public void onOpen( ServerHandshake handshakedata ) {
				switch(finalI) {
					case 0:
					case 6:
					case 7:
					case 8:
					case 9:
					case 13:
					case 14:
					case 17:
						counter++;
						threadReturned[0] = true;
						closeConnection( CloseFrame.ABNORMAL_CLOSE, "Bye" );
						break;
					default:
						fail( "There should not be a connection!" );
				}
			}

			@Override
			public void onMessage( String message ) {
				fail( "There should not be a message!" );
			}

			@Override
			public void onClose( int code, String reason, boolean remote ) {
				if( code == CloseFrame.ABNORMAL_CLOSE ) {
					switch(finalI) {
						case 0:
						case 6:
						case 7:
						case 8:
						case 9:
						case 13:
						case 14:
						case 17:
							return;
					}
				}
				if( code != CloseFrame.PROTOCOL_ERROR ) {
					fail( "There should be a protocol error! " + finalI + " " + code );
				} else if( reason.endsWith( "refuses handshake" ) ) {
					if( debugPrintouts )
						System.out.println( "Protocol error for test case: " + finalI );
					threadReturned[0] = true;
					counter++;
				} else {
					fail( "The reason should be included!" );
				}
			}

			@Override
			public void onError( Exception ex ) {
				fail( "There should not be an exception" );
			}
		};
		Thread finalThread = new Thread( webSocketClient );
		finalThread.start();
		finalThread.join();

		if( !threadReturned[0] ) {
			fail( "Error" );
		}

	}

	/**
	 * Generate a final key from a input string
	 *
	 * @param in the input string
	 * @return a final key
	 */
	private static String generateFinalKey( String in ) {
		String seckey = in.trim();
		String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		MessageDigest sh1;
		try {
			sh1 = MessageDigest.getInstance( "SHA1" );
		} catch ( NoSuchAlgorithmException e ) {
			throw new IllegalStateException( e );
		}
		return Base64.encodeBytes( sh1.digest( acc.getBytes() ) );
	}
}
