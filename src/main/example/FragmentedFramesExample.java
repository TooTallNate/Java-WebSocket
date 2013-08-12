import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
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

		WebSocketClient websocket = new ExampleClient( new URI( "ws://localhost:8887" ), new Draft_17() ); // Draft_17 is implementation of rfc6455
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
