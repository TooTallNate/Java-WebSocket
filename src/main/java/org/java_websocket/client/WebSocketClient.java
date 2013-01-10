package org.java_websocket.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.java_websocket.SocketChannelIOHelper;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketFactory;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WrappedByteChannel;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.HandshakeImpl1Client;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;

/**
 * The <tt>WebSocketClient</tt> is an abstract class that expects a valid
 * "ws://" URI to connect to. When connected, an instance recieves important
 * events related to the life of the connection. A subclass must implement
 * <var>onOpen</var>, <var>onClose</var>, and <var>onMessage</var> to be
 * useful. An instance can send messages to it's connected server via the
 * <var>send</var> method.
 * 
 * @author Nathan Rajlich
 */
public abstract class WebSocketClient extends WebSocketAdapter implements Runnable {

	/**
	 * The URI this channel is supposed to connect to.
	 */
	private URI uri = null;

	private WebSocketImpl conn = null;
	/**
	 * The SocketChannel instance this channel uses.
	 */
	private SocketChannel channel = null;

	private ByteChannel wrappedchannel = null;

	private Thread writethread;

	private Thread readthread;

	private Draft draft;

	private Map<String,String> headers;

	private CountDownLatch connectLatch = new CountDownLatch( 1 );

	private CountDownLatch closeLatch = new CountDownLatch( 1 );

	private int timeout = 0;

	WebSocketClientFactory wf = new WebSocketClientFactory() {
		@Override
		public WebSocket createWebSocket( WebSocketAdapter a, Draft d, Socket s ) {
			return new WebSocketImpl( WebSocketClient.this, d, s );
		}

		@Override
		public WebSocket createWebSocket( WebSocketAdapter a, List<Draft> d, Socket s ) {
			return new WebSocketImpl( WebSocketClient.this, d, s );
		}

		@Override
		public ByteChannel wrapChannel( SocketChannel channel, SelectionKey c, String host, int port ) {
			if( c == null )
				return channel;
			return channel;
		}
	};

	public WebSocketClient( URI serverURI ) {
		this( serverURI, new Draft_10() );
	}

	/**
	 * Constructs a WebSocketClient instance and sets it to the connect to the
	 * specified URI. The channel does not attampt to connect automatically. You
	 * must call <var>connect</var> first to initiate the socket connection.
	 */
	public WebSocketClient( URI serverUri , Draft draft ) {
		this( serverUri, draft, null, 0 );
	}

	public WebSocketClient( URI serverUri , Draft draft , Map<String,String> headers , int connecttimeout ) {
		if( serverUri == null ) {
			throw new IllegalArgumentException();
		}
		if( draft == null ) {
			throw new IllegalArgumentException( "null as draft is permitted for `WebSocketServer` only!" );
		}
		this.uri = serverUri;
		this.draft = draft;
		this.headers = headers;
		this.timeout = connecttimeout;

	}

	/**
	 * Gets the URI that this WebSocketClient is connected to.
	 * 
	 * @return The <tt>URI</tt> for this WebSocketClient.
	 */
	public URI getURI() {
		return uri;
	}

	/** Returns the protocol version this channel uses. */
	public Draft getDraft() {
		return draft;
	}

	/**
	 * Starts a background thread that attempts and maintains a WebSocket
	 * connection to the URI specified in the constructor or via <var>setURI</var>.
	 * <var>setURI</var>.
	 */
	public void connect() {
		if( writethread != null )
			throw new IllegalStateException( "WebSocketClient objects are not reuseable" );
		writethread = new Thread( this );
		writethread.start();
	}

	/**
	 * Same as connect but blocks until the websocket connected or failed to do so.<br>
	 * Returns whether it succeeded or not.
	 **/
	public boolean connectBlocking() throws InterruptedException {
		connect();
		connectLatch.await();
		return conn.isOpen();
	}

	public void close() {
		if( writethread != null && conn != null ) {
			conn.close( CloseFrame.NORMAL );
		}
	}

	public void closeBlocking() throws InterruptedException {
		close();
		closeLatch.await();
	}

	/**
	 * Sends <var>text</var> to the connected WebSocket server.
	 * 
	 * @param text
	 *            The String to send to the WebSocket server.
	 */
	public void send( String text ) throws NotYetConnectedException {
		if( conn != null ) {
			conn.send( text );
		}
	}

	/**
	 * Sends <var>data</var> to the connected WebSocket server.
	 * 
	 * @param data
	 *            The Byte-Array of data to send to the WebSocket server.
	 */
	public void send( byte[] data ) throws NotYetConnectedException {
		if( conn != null ) {
			conn.send( data );
		}
	}

	private void tryToConnect( InetSocketAddress remote ) throws IOException , InvalidHandshakeException {
		channel = SelectorProvider.provider().openSocketChannel();
		channel.configureBlocking( true );
		channel.connect( remote );

	}

	// Runnable IMPLEMENTATION /////////////////////////////////////////////////
	public void run() {
		if( writethread == null )
			writethread = Thread.currentThread();
		interruptableRun();

		assert ( !channel.isOpen() );

	}

	private final void interruptableRun() {
		try {
			String host = uri.getHost();
			int port = getPort();
			tryToConnect( new InetSocketAddress( host, port ) );
			conn = (WebSocketImpl) wf.createWebSocket( this, draft, channel.socket() );
			conn.channel = wrappedchannel = wf.wrapChannel( channel, null, host, port );
			timeout = 0; // since connect is over
			sendHandshake();
			readthread = new Thread( new WebsocketWriteThread() );
			readthread.start();
		} catch ( ClosedByInterruptException e ) {
			onWebsocketError( null, e );
			return;
		} catch ( /*IOException | SecurityException | UnresolvedAddressException*/Exception e ) {//
			onWebsocketError( conn, e );
			conn.closeConnection( CloseFrame.NEVER_CONNECTED, e.getMessage() );
			return;
		}

		ByteBuffer buff = ByteBuffer.allocate( WebSocketImpl.RCVBUF );
		try/*IO*/{
			while ( channel.isOpen() ) {
				if( SocketChannelIOHelper.read( buff, this.conn, wrappedchannel ) ) {
					conn.decode( buff );
				} else {
					conn.eot();
				}

				if( wrappedchannel instanceof WrappedByteChannel ) {
					WrappedByteChannel w = (WrappedByteChannel) wrappedchannel;
					if( w.isNeedRead() ) {
						while ( SocketChannelIOHelper.read( buff, conn, w ) ) {
							conn.decode( buff );
						}
					}
				}
			}

		} catch ( CancelledKeyException e ) {
			conn.eot();
		} catch ( IOException e ) {
			conn.eot();
		} catch ( RuntimeException e ) {
			// this catch case covers internal errors only and indicates a bug in this websocket implementation
			onError( e );
			conn.closeConnection( CloseFrame.ABNORMAL_CLOSE, e.getMessage() );
		}
	}

	private int getPort() {
		int port = uri.getPort();
		if( port == -1 ) {
			String scheme = uri.getScheme();
			if( scheme.equals( "wss" ) ) {
				return WebSocket.DEFAULT_WSS_PORT;
			} else if( scheme.equals( "ws" ) ) {
				return WebSocket.DEFAULT_PORT;
			} else {
				throw new RuntimeException( "unkonow scheme" + scheme );
			}
		}
		return port;
	}

	private void sendHandshake() throws InvalidHandshakeException {
		String path;
		String part1 = uri.getPath();
		String part2 = uri.getQuery();
		if( part1 == null || part1.length() == 0 )
			path = "/";
		else
			path = part1;
		if( part2 != null )
			path += "?" + part2;
		int port = getPort();
		String host = uri.getHost() + ( port != WebSocket.DEFAULT_PORT ? ":" + port : "" );

		HandshakeImpl1Client handshake = new HandshakeImpl1Client();
		handshake.setResourceDescriptor( path );
		handshake.put( "Host", host );
		if( headers != null ) {
			for( Map.Entry<String,String> kv : headers.entrySet() ) {
				handshake.put( kv.getKey(), kv.getValue() );
			}
		}
		conn.startHandshake( handshake );
	}

	/**
	 * This represents the state of the connection.
	 * You can use this method instead of
	 */
	public READYSTATE getReadyState() {
		if( conn == null ) {
			return READYSTATE.NOT_YET_CONNECTED;
		}
		return conn.getReadyState();
	}

	/**
	 * Calls subclass' implementation of <var>onMessage</var>.
	 * 
	 * @param conn
	 * @param message
	 */
	@Override
	public final void onWebsocketMessage( WebSocket conn, String message ) {
		onMessage( message );
	}

	@Override
	public final void onWebsocketMessage( WebSocket conn, ByteBuffer blob ) {
		onMessage( blob );
	}

	/**
	 * Calls subclass' implementation of <var>onOpen</var>.
	 * 
	 * @param conn
	 */
	@Override
	public final void onWebsocketOpen( WebSocket conn, Handshakedata handshake ) {
		connectLatch.countDown();
		onOpen( (ServerHandshake) handshake );
	}

	/**
	 * Calls subclass' implementation of <var>onClose</var>.
	 * 
	 * @param conn
	 */
	@Override
	public final void onWebsocketClose( WebSocket conn, int code, String reason, boolean remote ) {
		connectLatch.countDown();
		closeLatch.countDown();
		readthread.interrupt();
		onClose( code, reason, remote );
	}

	/**
	 * Calls subclass' implementation of <var>onIOError</var>.
	 * 
	 * @param conn
	 */
	@Override
	public final void onWebsocketError( WebSocket conn, Exception ex ) {
		onError( ex );
	}

	@Override
	public final void onWriteDemand( WebSocket conn ) {
		// nothing to do
	}

	@Override
	public void onWebsocketCloseInitiated( WebSocket conn, int code, String reason ) {
		onCloseInitiated( code, reason );
	}

	@Override
	public void onWebsocketClosing( WebSocket conn, int code, String reason, boolean remote ) {
		onClosing( code, reason, remote );
	}

	public void onCloseInitiated( int code, String reason ) {
	}

	public void onClosing( int code, String reason, boolean remote ) {
	}

	public WebSocket getConnection() {
		return conn;
	}

	public final void setWebSocketFactory( WebSocketClientFactory wsf ) {
		this.wf = wsf;
	}

	public final WebSocketFactory getWebSocketFactory() {
		return wf;
	}

	// ABTRACT METHODS /////////////////////////////////////////////////////////
	public abstract void onOpen( ServerHandshake handshakedata );
	public abstract void onMessage( String message );
	public abstract void onClose( int code, String reason, boolean remote );
	public abstract void onError( Exception ex );
	public void onMessage( ByteBuffer bytes ) {
	};

	public interface WebSocketClientFactory extends WebSocketFactory {
		public ByteChannel wrapChannel( SocketChannel channel, SelectionKey key, String host, int port ) throws IOException;
	}

	private class WebsocketWriteThread implements Runnable {
		@Override
		public void run() {
			Thread.currentThread().setName( "WebsocketWriteThread" );
			try {
				while ( !Thread.interrupted() ) {
					SocketChannelIOHelper.writeBlocking( conn, wrappedchannel );
				}
			} catch ( IOException e ) {
				conn.eot();
			} catch ( InterruptedException e ) {
				// this thread is regulary terminated via an interrupt
			}
		}
	}
}
