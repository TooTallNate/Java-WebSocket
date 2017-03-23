package org.java_websocket.example;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class AutobahnServerTest extends WebSocketServer {
	private static int counter = 0;
	
	public AutobahnServerTest( int port , Draft d ) throws UnknownHostException {
		super( new InetSocketAddress( port ), Collections.singletonList( d ) );
	}
	
	public AutobahnServerTest( InetSocketAddress address, Draft d ) {
		super( address, Collections.singletonList( d ) );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		counter++;
		System.out.println( "///////////Opened connection number" + counter );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println( "closed" );
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		System.out.println( "Error:" );
		ex.printStackTrace();
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		conn.send( message );
	}

	@Override
	public void onMessage( WebSocket conn, ByteBuffer blob ) {
		conn.send( blob );
	}

	@Override
	public void onWebsocketMessageFragment( WebSocket conn, Framedata frame ) {
		FrameBuilder builder = (FrameBuilder) frame;
		builder.setTransferemasked( false );
		conn.sendFrame( frame );
	}

	public static void main( String[] args ) throws  UnknownHostException {
		WebSocketImpl.DEBUG = false;
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
