package org.java_websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;

/**
 * <tt>WebSocketServer</tt> is an abstract class that only takes care of the
 * HTTP handshake portion of WebSockets. It's up to a subclass to add
 * functionality/purpose to the server.
 * 
 */
public abstract class WebSocketServer extends WebSocketAdapter implements Runnable {

	public static int DECODERS = Runtime.getRuntime().availableProcessors();

	/**
	 * Holds the list of active WebSocket connections. "Active" means WebSocket
	 * handshake is complete and socket can be written to, or read from.
	 */
	private final Set<WebSocket> connections = new HashSet<WebSocket>();
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
	private List<Draft> drafts;

	private Thread selectorthread;

	private ExecutorService decoders;

	private ArrayBlockingQueue<WebSocket> queue;

	// CONSTRUCTORS ////////////////////////////////////////////////////////////
	/**
	 * Nullary constructor. Creates a WebSocketServer that will attempt to
	 * listen on port <var>WebSocket.DEFAULT_PORT</var>.
	 */
	public WebSocketServer() throws UnknownHostException {
		this( new InetSocketAddress( WebSocket.DEFAULT_PORT ), DECODERS, null );
	}

	/**
	 * Creates a WebSocketServer that will attempt to bind/listen on the given <var>address</var>.
	 * 
	 * @param address
	 *            The address (host:port) this server should listen on.
	 */
	public WebSocketServer( InetSocketAddress address ) {
		this( address, DECODERS, null );
	}

	public WebSocketServer( InetSocketAddress address , int decoders ) {
		this( address, decoders, null );
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
	public WebSocketServer( InetSocketAddress address , List<Draft> drafts ) {
		this( address, DECODERS, drafts );
	}

	public WebSocketServer( InetSocketAddress address , int decodercount , List<Draft> drafts ) {
		if( drafts == null )
			this.drafts = Collections.emptyList();
		else
			this.drafts = drafts;
		setAddress( address );
		queue = new ArrayBlockingQueue<WebSocket>( decodercount );
		this.decoders = Executors.newFixedThreadPool( decodercount, new WebsocketThreadFactory() );
		for( int i = 0 ; i < decodercount ; i++ ) {
			decoders.submit( new WebsocketWorker() );
		}
	}

	/**
	 * Starts the server selectorthread that binds to the currently set port number and
	 * listeners for WebSocket connection requests. Creates a fixed thread pool with the size {@link WebSocketServer#DECODERS}<br>
	 * May only be called once.
	 * 
	 * Alternatively you can call {@link WebSocketServer#run()} directly.
	 * 
	 * @throws IllegalStateException
	 */
	public void start() {
		if( selectorthread != null )
			throw new IllegalStateException( "Already started" );
		new Thread( this ).start();
	}

	/**
	 * Closes all connected clients sockets, then closes the underlying
	 * ServerSocketChannel, effectively killing the server socket selectorthread and
	 * freeing the port the server was bound to.
	 * 
	 * @throws IOException
	 *             When socket related I/O errors occur.
	 */
	public void stop() throws IOException {
		synchronized ( connections ) {
			for( WebSocket ws : connections ) {
				ws.close( CloseFrame.NORMAL );
			}
		}
		selectorthread.interrupt();
		this.server.close();

	}

	/**
	 * Returns a WebSocket[] of currently connected clients.
	 * Its iterators will be failfast and its not judicious
	 * to modify it.
	 * 
	 * @return The currently connected clients.
	 */
	public Set<WebSocket> connections() {
		return this.connections;
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

	public List<Draft> getDraft() {
		return Collections.unmodifiableList( drafts );
	}

	// Runnable IMPLEMENTATION /////////////////////////////////////////////////
	public void run() {
		if( selectorthread != null )
			throw new IllegalStateException( "This instance of " + getClass().getSimpleName() + " can only be started once the same time." );
		selectorthread = Thread.currentThread();
		try {
			server = ServerSocketChannel.open();
			server.configureBlocking( false );
			server.socket().bind( address );
			selector = Selector.open();
			server.register( selector, server.validOps() );
		} catch ( IOException ex ) {
			onWebsocketError( null, ex );
			return;
		}
		try {
			while ( !selectorthread.isInterrupted() ) {
				SelectionKey key = null;
				WebSocket conn = null;
				try {
					selector.select();
					Set<SelectionKey> keys = selector.selectedKeys();
					Iterator<SelectionKey> i = keys.iterator();

					while ( i.hasNext() ) {
						key = i.next();

						if( !key.isValid() ) {
							// Object o = key.attachment();
							continue;
						}

						if( key.isAcceptable() ) {
							SocketChannel client = server.accept();
							client.configureBlocking( false );
							WebSocket c = new WebSocket( this, drafts, client );
							client.register( selector, SelectionKey.OP_READ, c );
							i.remove();
						} else if( key.isReadable() ) {
							conn = (WebSocket) key.attachment();
							queue.put( conn );
							i.remove();
						}
					}
				} catch ( CancelledKeyException e ) {
					// an other thread may cancel the key
				} catch ( IOException ex ) {
					if( key != null )
						key.cancel();
					handleIOException( conn, ex );
				} catch ( InterruptedException e ) {
					return;
				}
			}
		} catch ( RuntimeException e ) {
			// should hopefully never occur
			handleFatal( null, e );
		}
	}

	private void handleIOException( WebSocket conn, IOException ex ) {
		onWebsocketError( conn, ex );// conn may be null here
		if( conn != null ) {
			conn.close( CloseFrame.ABNORMAL_CLOSE );
		}
	}

	private void handleFatal( WebSocket conn, RuntimeException e ) {
		onError( conn, e );
		try {
			selector.close();
		} catch ( IOException e1 ) {
			onError( null, e1 );
		}
		decoders.shutdown();
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
	public final void onWebsocketMessage( WebSocket conn, ByteBuffer blob ) {
		onMessage( conn, blob );
	}

	@Override
	public final void onWebsocketOpen( WebSocket conn, Handshakedata handshake ) {
		synchronized ( connections ) {
			if( this.connections.add( conn ) ) {
				onOpen( conn, (ClientHandshake) handshake );
			}
		}
	}

	@Override
	public final void onWebsocketClose( WebSocket conn, int code, String reason, boolean remote ) {
		synchronized ( connections ) {
			if( this.connections.remove( conn ) ) {
				onClose( conn, code, reason, remote );
			}
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
		if( Thread.currentThread() instanceof WebsocketExecutorThread == false ) {
			try {
				conn.flush();
			} catch ( IOException e ) {
				handleIOException( conn, e );
			}
		}
	}

	// ABTRACT METHODS /////////////////////////////////////////////////////////
	public abstract void onOpen( WebSocket conn, ClientHandshake handshake );
	public abstract void onClose( WebSocket conn, int code, String reason, boolean remote );
	public abstract void onMessage( WebSocket conn, String message );
	public abstract void onError( WebSocket conn, Exception ex );
	public void onMessage( WebSocket conn, ByteBuffer message ) {
	};

	private class WebsocketWorker implements Runnable {

		@Override
		public void run() {
			WebSocket ws = null;
			try {
				while ( true ) {
					try {
						ws = queue.take();
						ws.handleRead();
						ws.flush();
					} catch ( IOException e ) {
						handleIOException( ws, e );
					}
				}
			} catch ( RuntimeException e ) {
				handleFatal( ws, e );
			} catch ( InterruptedException e ) {
			}
		}
	}

	private class WebsocketThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread( Runnable r ) {
			return new WebsocketExecutorThread( r );
		}
	}

	private class WebsocketExecutorThread extends Thread {
		private Runnable r;
		WebsocketExecutorThread( Runnable r ) {
			this.r = r;

		}
		@Override
		public void run() {
			r.run();
		}
	}
}
