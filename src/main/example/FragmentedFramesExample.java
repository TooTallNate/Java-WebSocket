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
 * This example show how to send fragmented frames.<br>
 * It also shows that one can mix fragmented and normal frames at will.<br>
 * Of course one has to finish with a fragmented frame sequence before continuing with the next.
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

		System.out.println( "This example shows how to send fragmented(continuous) messages.\n It also shows that fragments can be intercepted by normal messages." );

		BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( websocket.isOpen() ) {
			System.out.println( "Please type in a loooooong line(which will be send in multible parts):" );
			String longline = stdin.readLine();
			ByteBuffer longelinebuffer = ByteBuffer.wrap( longline.getBytes() );
			longelinebuffer.rewind();

			System.out.println( "The long message you just typed in will be fragmented in messages of 2bytes payload each.\nPress enter so send the next fragemnt or make some other input to send text messages inbetween." );
			for( int position = 2 ; ; position += 2 ) {

				String sendInOnePiece = stdin.readLine();
				if( !sendInOnePiece.isEmpty() ) {
					websocket.send( sendInOnePiece );
				}

				if( position < longelinebuffer.capacity() ) {
					longelinebuffer.limit( position );
					websocket.sendFragmentedFrame( Opcode.TEXT, longelinebuffer, false );// when sending binary data use Opcode.BINARY
				} else {
					longelinebuffer.limit( longelinebuffer.capacity() );
					websocket.sendFragmentedFrame( Opcode.TEXT, longelinebuffer, true );
					break;
				}

			}
			System.out.println( "You can not type in the next long message or press Ctr-C to exit." );
		}
		System.out.println( "FragmentedFramesExample terminated" );
	}
}
