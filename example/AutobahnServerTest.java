import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;
import net.tootallnate.websocket.drafts.Draft_17;

public class AutobahnServerTest extends WebSocketServer {
	private static int counter = 0;
	
	public AutobahnServerTest( int port, Draft d ) throws UnknownHostException {
		super( new InetSocketAddress( "localhost", port ), d );
	}
	
	public AutobahnServerTest( InetSocketAddress address, Draft d ) {
		super( address, d );
	}

	@Override
	public void onOpen( WebSocket conn, Handshakedata handshake ) {
		counter++;
		System.out.println( "///////////Opened connection number" + counter );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		System.out.println( "Error:" );
		ex.printStackTrace();
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		try {
			conn.send( message );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	@Override
	public void onMessage( WebSocket conn, byte[] blob ) {
		try {
			conn.send( blob );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) throws  UnknownHostException {
		WebSocket.DEBUG = false;
		int port;
		try {
			port = new Integer( args[ 0 ] );
		} catch ( Exception e ) {
			System.out.println( "No port specified. Defaulting to 9003" );
			port = 9003;
		}
		new AutobahnServerTest( port, new Draft_17() ).start();
	}

}
