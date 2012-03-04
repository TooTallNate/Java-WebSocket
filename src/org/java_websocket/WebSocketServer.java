package org.java_websocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <tt>WebSocketServer</tt> is an abstract class that only takes care of the
 * HTTP handshake portion of WebSockets. It's up to a subclass to add
 * functionality/purpose to the server.
 * 
 * @author Nathan Rajlich
 */
public abstract class WebSocketServer extends WebSocketAdapter implements Runnable {

	// INSTANCE PROPERTIES /////////////////////////////////////////////////////
	/**
	 * Holds the list of active WebSocket connections. "Active" means WebSocket
	 * handshake is complete and socket can be written to, or read from.
	 */
	private final CopyOnWriteArraySet<WebSocket> connections;
	/**
	 * The port number that this WebSocket server should listen on. Default is
	 * WebSocket.DEFAULT_PORT.
	 */
	private InetSocketAddress address;
	/**
	 * The socket channel for this WebSocket server.
	 */
	private ServerSocketChannel server;
	/**
	 * The 'Selector' used to get event keys from the underlying socket.
	 */
	private Selector selector;
	/**
	 * The Draft of the WebSocket protocol the Server is adhering to.
	 */
	private Draft draft;

	private Thread thread;

	// CONSTRUCTORS ////////////////////////////////////////////////////////////
	/**
	 * Nullary constructor. Creates a WebSocketServer that will attempt to
	 * listen on port <var>WebSocket.DEFAULT_PORT</var>.
	 */
	public WebSocketServer() throws UnknownHostException {
		this( new InetSocketAddress( InetAddress.getLocalHost(), WebSocket.DEFAULT_PORT ), null );
	}

	/**
	 * Creates a WebSocketServer that will attempt to bind/listen on the given <var>address</var>.
	 * 
	 * @param address
	 *            The address (host:port) this server should listen on.
	 */
	public WebSocketServer( InetSocketAddress address ) {
		this( address, null );
	}

	/**
	 * Creates a WebSocketServer that will attempt to bind/listen on the given <var>address</var>,
	 * and comply with <tt>Draft</tt> version <var>draft</var>.
	 * 
	 * @param address
	 *            The address (host:port) this server should listen on.
	 * @param draft
	 *            The version of the WebSocket protocol that this server
	 *            instance should comply to.
	 */
	public WebSocketServer( InetSocketAddress address , Draft draft ) {
		this.connections = new CopyOnWriteArraySet<WebSocket>();
		this.draft = draft;
		setAddress( address );
	}

	/**
	 * Starts the server thread that binds to the currently set port number and
	 * listeners for WebSocket connection requests.
	 * 
	 * @throws IllegalStateException
	 */
	public void start() {
		if( thread != null )
			throw new IllegalStateException( "Already started" );
		new Thread( this ).start();
	}

	/**
	 * Closes all connected clients sockets, then closes the underlying
	 * ServerSocketChannel, effectively killing the server socket thread and
	 * freeing the port the server was bound to.
	 * 
	 * @throws IOException
	 *             When socket related I/O errors occur.
	 */
	public void stop() throws IOException {
		for( WebSocket ws : connections ) {
			ws.close( CloseFrame.NORMAL );
		}
		thread.interrupt();
		this.server.close();

	}

	/**
	 * Returns a WebSocket[] of currently connected clients.
	 * 
	 * @return The currently connected clients in a WebSocket[].
	 */
	public Set<WebSocket> connections() {
		return Collections.unmodifiableSet( this.connections );
	}

	/**
	 * Sets the address (host:port) that this WebSocketServer should listen on.
	 * 
	 * @param address
	 *            The address (host:port) to listen on.
	 */
	public void setAddress( InetSocketAddress address ) {
		this.address = address;
	}

	public InetSocketAddress getAddress() {
		return this.address;
	}

	/**
	 * Gets the port number that this server listens on.
	 * 
	 * @return The port number.
	 */
	public int getPort() {
		return getAddress().getPort();
	}

	public Draft getDraft() {
		return this.draft;
	}

	// Runnable IMPLEMENTATION /////////////////////////////////////////////////
	public void run() {
		if( thread != null )
			throw new IllegalStateException( "This instance of " + getClass().getSimpleName() + " can only be started once the same time." );
		thread = Thread.currentThread();
		try {
			server = ServerSocketChannel.open();
			server.configureBlocking( false );
			server.socket().bind( address );
			// InetAddress.getLocalHost()
			selector = Selector.open();
			server.register( selector, server.validOps() );
		} catch ( IOException ex ) {
			onWebsocketError( null, ex );
			return;
		}

		while ( !thread.isInterrupted() ) {
			SelectionKey key = null;
			WebSocket conn = null;
			try {
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> i = keys.iterator();

				while ( i.hasNext() ) {
					key = i.next();

					// Remove the current key
					i.remove();

					// if isAcceptable == true
					// then a client required a connection
					if( key.isAcceptable() ) {
						SocketChannel client = server.accept();
						client.configureBlocking( false );
						WebSocket c = new WebSocket( this, Collections.singletonList( draft ), client );
						client.register( selector, SelectionKey.OP_READ, c );
					}

					// if isReadable == true
					// then the server is ready to read
					if( key.isReadable() ) {
						conn = (WebSocket) key.attachment();
						conn.handleRead();
					}

					// if isWritable == true
					// then we need to send the rest of the data to the client
					if( key.isValid() && key.isWritable() ) {
						conn = (WebSocket) key.attachment();
						conn.flush();
						key.channel().register( selector, SelectionKey.OP_READ, conn );
					}
				}
				Iterator<WebSocket> it = this.connections.iterator();
				while ( it.hasNext() ) {
					// We have to do this check here, and not in the thread that
					// adds the buffered data to the WebSocket, because the
					// Selector is not thread-safe, and can only be accessed
					// by this thread.
					conn = it.next();
					if( conn.hasBufferedData() ) {
						conn.flush();
						// key.channel().register( selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, conn );
					}
				}
			} catch ( IOException ex ) {
				if( key != null )
					key.cancel();
				onWebsocketError( conn, ex );// conn may be null here
				if( conn != null ) {
					conn.close( CloseFrame.ABNROMAL_CLOSE );
				}
			}
		}
	}

	/**
	 * Gets the XML string that should be returned if a client requests a Flash
	 * security policy.
	 * 
	 * The default implementation allows access from all remote domains, but
	 * only on the port that this WebSocketServer is listening on.
	 * 
	 * This is specifically implemented for gitime's WebSocket client for Flash:
	 * http://github.com/gimite/web-socket-js
	 * 
	 * @return An XML String that comforms to Flash's security policy. You MUST
	 *         not include the null char at the end, it is appended automatically.
	 */
	protected String getFlashSecurityPolicy() {
		return "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"" + getPort() + "\" /></cross-domain-policy>";
	}

	@Override
	public final void onWebsocketMessage( WebSocket conn, String message ) {
		onMessage( conn, message );
	}

	@Override
	public final void onWebsocketMessage( WebSocket conn, byte[] blob ) {
		onMessage( conn, blob );
	}

	@Override
	public final void onWebsocketOpen( WebSocket conn, Handshakedata handshake ) {
		if( this.connections.add( conn ) ) {
			onOpen( conn, (ClientHandshake) handshake );
		}
	}

	@Override
	public final void onWebsocketClose( WebSocket conn, int code, String reason, boolean remote ) {
		if( this.connections.remove( conn ) ) {
			onClose( conn, code, reason, remote );
		}
	}

	/**
	 * @param conn
	 *            may be null if the error does not belong to a single connection
	 */
	@Override
	public final void onWebsocketError( WebSocket conn, Exception ex ) {
		onError( conn, ex );
	}

	@Override
	public final void onWriteDemand( WebSocket conn ) {
		selector.wakeup();
	}

	// ABTRACT METHODS /////////////////////////////////////////////////////////
	public abstract void onOpen( WebSocket conn, ClientHandshake handshake );
	public abstract void onClose( WebSocket conn, int code, String reason, boolean remote );
	public abstract void onMessage( WebSocket conn, String message );
	public abstract void onError( WebSocket conn, Exception ex );
	public void onMessage( WebSocket conn, byte[] message ) {
	};

}
