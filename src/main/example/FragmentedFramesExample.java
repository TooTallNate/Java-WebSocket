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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.Framedata.Opcode;

/**
 * This example shows how to send fragmented frames.<br>
 * For information on when to used fragmented frames see http://tools.ietf.org/html/rfc6455#section-5.4<br>
 * Fragmented and normal messages can not be mixed.
 * One is however allowed to mix them with control messages like ping/pong.
 * 
 * @see WebSocket#sendFragmentedFrame(Opcode, ByteBuffer, boolean)
 **/
public class FragmentedFramesExample {
	public static void main( String[] args ) throws URISyntaxException , IOException , InterruptedException {
		// WebSocketImpl.DEBUG = true; // will give extra output

		WebSocketClient websocket = new ExampleClient( new URI( "ws://localhost:8887" ));
		if( !websocket.connectBlocking() ) {
			System.err.println( "Could not connect to the server." );
			return;
		}

		System.out.println( "This example shows how to send fragmented(continuous) messages." );

		BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( websocket.isOpen() ) {
			System.out.println( "Please type in a loooooong line(which then will be send in 2 byte fragments):" );
			String longline = stdin.readLine();
			ByteBuffer longelinebuffer = ByteBuffer.wrap( longline.getBytes() );
			longelinebuffer.rewind();

			for( int position = 2 ; ; position += 2 ) {
				if( position < longelinebuffer.capacity() ) {
					longelinebuffer.limit( position );
					websocket.sendFragmentedFrame( Opcode.TEXT, longelinebuffer, false );// when sending binary data one should use Opcode.BINARY
					assert ( longelinebuffer.remaining() == 0 );
					// after calling sendFragmentedFrame one may reuse the buffer given to the method immediately
				} else {
					longelinebuffer.limit( longelinebuffer.capacity() );
					websocket.sendFragmentedFrame( Opcode.TEXT, longelinebuffer, true );// sending the last frame
					break;
				}

			}
			System.out.println( "You can not type in the next long message or press Ctr-C to exit." );
		}
		System.out.println( "FragmentedFramesExample terminated" );
	}
}
