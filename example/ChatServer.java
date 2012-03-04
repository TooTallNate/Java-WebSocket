import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.java_websocket.ClientHandshake;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketServer;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class ChatServer extends WebSocketServer {

	public ChatServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( InetAddress.getByName( "localhost" ), port ) );
	}
	
	public ChatServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		try {
			this.sendToAll( conn + " entered the room!" );
		} catch ( InterruptedException ex ) {
			ex.printStackTrace();
		}
		System.out.println( conn + " entered the room!" );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		try {
			this.sendToAll( conn + " has left the room!" );
		} catch ( InterruptedException ex ) {
			ex.printStackTrace();
		}
		System.out.println( conn + " has left the room!" );
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		try {
			this.sendToAll( conn + ": " + message );
		} catch ( InterruptedException ex ) {
			ex.printStackTrace();
		}
		System.out.println( conn + ": " + message );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocket.DEBUG = true;
		int port = 8887;
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		ChatServer s = new ChatServer( port );
		s.start();
		System.out.println( "ChatServer started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			s.sendToAll( in );
		}
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
	}

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll( String text ) throws InterruptedException {
		for( WebSocket c : connections() ) {
			c.send( text );
		}
	}
}
