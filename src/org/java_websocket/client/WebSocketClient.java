package org.java_websocket.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketFactory;
import org.java_websocket.WebSocketImpl;
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
	 * The URI this client is supposed to connect to.
	 */
	private URI uri = null;
	/**
	 * The WebSocket instance this client object wraps.
	 */
	private WebSocketImpl conn = null;
	/**
	 * The SocketChannel instance this client uses.
	 */
	private SocketChannel client = null;
	/**
	 * The 'Selector' used to get event keys from the underlying socket.
	 */
	private Selector selector = null;

	private Thread thread;

	private Draft draft;

	private final Lock closelock = new ReentrantLock();

	private Map<String, String> headers;

	WebSocketFactory wf = new WebSocketFactory() {
		@Override
		public WebSocket createWebSocket( WebSocketAdapter a, Draft d, SocketChannel c ) {
			return new WebSocketImpl( WebSocketClient.this, d, c );
		}

		@Override
		public WebSocket createWebSocket( WebSocketAdapter a, List<Draft> d, SocketChannel c ) {
			return new WebSocketImpl( WebSocketClient.this, d, c );
		}
	};

	public WebSocketClient( URI serverURI ) {
		this( serverURI, new Draft_10() );
	}

	/**
	 * Constructs a WebSocketClient instance and sets it to the connect to the
	 * specified URI. The client does not attampt to connect automatically. You
	 * must call <var>connect</var> first to initiate the socket connection.
	 */
	public WebSocketClient( URI serverUri , Draft draft ) {
		this(serverUri, draft, null);
	}

	public WebSocketClient( URI serverUri , Draft draft, Map<String, String> headers) {
		if( serverUri == null ) {
			throw new IllegalArgumentException();
		}
		if( draft == null ) {
			throw new IllegalArgumentException( "null as draft is permitted for `WebSocketServer` only!" );
		}
		this.uri = serverUri;
		this.draft = draft;
		this.headers = headers;
	}

	/**
	 * Gets the URI that this WebSocketClient is connected to.
	 * 
	 * @return The <tt>URI</tt> for this WebSocketClient.
	 */
	public URI getURI() {
		return uri;
	}

	/** Returns the protocol version this client uses. */
	public Draft getDraft() {
		return draft;
	}

	/**
	 * Starts a background thread that attempts and maintains a WebSocket
	 * connection to the URI specified in the constructor or via <var>setURI</var>.
	 * <var>setURI</var>.
	 */
	public void connect() {
		if( thread != null )
			throw new IllegalStateException( "already/still connected" );
		thread = new Thread( this );
		thread.start();
	}

	public void close() {
		if( thread != null ) {
			thread.interrupt();
			closelock.lock();
			try {
				if( selector != null )
					selector.wakeup();
			} finally {
				closelock.unlock();
			}
		}

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
	public void send( byte[] data ) throws NotYetConnectedException , InterruptedException {
		if( conn != null ) {
			conn.send( data );
		}
	}

	private void tryToConnect( InetSocketAddress remote ) throws IOException {
		client = SocketChannel.open();
		client.configureBlocking( false );
		client.connect( remote );
		selector = Selector.open();
		client.register( selector, SelectionKey.OP_CONNECT );
	}

	// Runnable IMPLEMENTATION /////////////////////////////////////////////////
	public void run() {
		if( thread == null )
			thread = Thread.currentThread();
		interruptableRun();
		thread = null;
	}

	protected final void interruptableRun() {
		try {
			tryToConnect( new InetSocketAddress( uri.getHost(), getPort() ) );
		} catch ( ClosedByInterruptException e ) {
			onWebsocketError( null, e );
			return;
		} catch ( IOException e ) {//
			onWebsocketError( conn, e );
			return;
		} catch ( SecurityException e ) {
			onWebsocketError( conn, e );
			return;
		} catch ( UnresolvedAddressException e ) {
			onWebsocketError( conn, e );
			return;
		}
		conn = (WebSocketImpl) wf.createWebSocket( this, draft, client );
		ByteBuffer buff = ByteBuffer.allocate( WebSocket.RCVBUF );
		try/*IO*/{
			while ( !conn.isClosed() ) {
				if( Thread.interrupted() ) {
					conn.close( CloseFrame.NORMAL );
				}
				SelectionKey key = null;
				conn.flush();
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> i = keys.iterator();
				while ( i.hasNext() ) {
					key = i.next();
					i.remove();
					if( key.isReadable() && conn.read( buff ) ) {
						conn.decode( buff );
					}
					if( !key.isValid() ) {
						continue;
					}
					if( key.isWritable() ) {
						conn.flush();
					}
					if( key.isConnectable() ) {
						try {
							finishConnect();
						} catch ( InterruptedException e ) {
							conn.close( CloseFrame.NEVERCONNECTED );// report error to only
							break;
						} catch ( InvalidHandshakeException e ) {
							conn.close( e ); // http error
							conn.flush();
						}
					}
				}
			}
		} catch ( IOException e ) {
			onError( e );
			conn.close( CloseFrame.ABNORMAL_CLOSE );
			return;
		} catch ( RuntimeException e ) {
			// this catch case covers internal errors only and indicates a bug in this websocket implementation
			onError( e );
			conn.closeConnection( CloseFrame.BUGGYCLOSE, e.toString(), false );
			return;
		}

		try {
			selector.close();
		} catch ( IOException e ) {
			onError( e );
		}
		closelock.lock();
		selector = null;
		closelock.unlock();
		try {
			client.close();
		} catch ( IOException e ) {
			onError( e );
		}
		client = null;
	}

	private int getPort() {
		int port = uri.getPort();
		return port == -1 ? WebSocket.DEFAULT_PORT : port;
	}

	private void finishConnect() throws IOException , InvalidHandshakeException , InterruptedException {
		if( client.isConnectionPending() ) {
			client.finishConnect();
		}

		// Now that we're connected, re-register for only 'READ' keys.
		client.register( selector, SelectionKey.OP_READ );

		sendHandshake();
	}

	public void flush() {
		try {
			conn.flush();
		} catch ( IOException e ) {
			onError( e );
			conn.closeConnection( CloseFrame.ABNORMAL_CLOSE, true );
			return;
		}
	}

	private void sendHandshake() throws IOException , InvalidHandshakeException , InterruptedException {
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
		if (headers != null) {
			for (Map.Entry<String, String> kv : headers.entrySet()) {
				handshake.put(kv.getKey(), kv.getValue());
			}
		}
		conn.startHandshake( handshake );
	}

	/**
	 * Retrieve the WebSocket 'readyState'.
	 * This represents the state of the connection.
	 * It returns a numerical value, as per W3C WebSockets specs.
	 * 
	 * @return Returns '0 = CONNECTING', '1 = OPEN', '2 = CLOSING' or '3 = CLOSED'
	 */
	public int getReadyState() {
		return conn.getReadyState();
	}

	/**
	 * Amount of data buffered/queued but not sent yet.
	 *
	 * In details, it returns the number of bytes of application data (UTF-8 text and binary data)
	 * that have been queued using send() but that, as of the last time the event loop
	 * started executing a task, had not yet been transmitted to the network.
	 *
	 * @return Amount still buffered/queued but not sent yet.
	 */
	public long getBufferedAmount() {
		return conn.bufferedDataAmount();
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
		onOpen( (ServerHandshake) handshake );
	}

	/**
	 * Calls subclass' implementation of <var>onClose</var>.
	 * 
	 * @param conn
	 */
	@Override
	public final void onWebsocketClose( WebSocket conn, int code, String reason, boolean remote ) {
		thread.interrupt();
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
		selector.wakeup();
	}

	public WebSocket getConnection() {
		return conn;
	}

	public final void setWebSocketFactory( WebSocketFactory wsf ) {
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
}
