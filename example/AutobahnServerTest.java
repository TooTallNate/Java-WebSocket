import java.io.IOException;
import java.nio.channels.NotYetConnectedException;

import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;

import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.drafts.Draft_10;
import net.tootallnate.websocket.drafts.Draft_17;

public class AutobahnServerTest extends WebSocketServer {

	public AutobahnServerTest(int port, Draft d ) {
		super( port );
	}
	@Override
	public void onClientOpen( WebSocket conn ) {
	}

	@Override
	public void onClientClose( WebSocket conn ) {
	}

	@Override
	public void onClientMessage( WebSocket conn , String message ) {
	}

	@Override
	public void onError( WebSocket conn , Exception ex ) {
		System.out.println("Error:");
		ex.printStackTrace();
	}
	
	@Override
	public void onMessage( WebSocket conn , String message ) {
		try {
			conn.send( message );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	@Override
	public void onMessage( WebSocket conn , byte[] blob ) {
		try {
			conn.send( blob );
		}  catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	public static void main( String[] args ) {
		int port;
		try {
			port = new Integer( args[0] );
		} catch ( Exception e ) {
			System.out.println("No port specified. Defaulting to 9001");
			port = 9002;
		}
		new AutobahnServerTest( port, new Draft_17() ).start();
	}

}
