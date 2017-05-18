package org.java_websocket.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.java_websocket.AbstractWebSocket;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.handshake.HandshakeImpl1Client;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;

/**
 * A subclass must implement at least <var>onOpen</var>, <var>onClose</var>, and <var>onMessage</var> to be
 * useful. At runtime the user is expected to establish a connection via {@link #connect()}, then receive events like {@link #onMessage(String)} via the overloaded methods and to {@link #send(String)} data to the server.
 */
public abstract class WebSocketClient extends AbstractWebSocket implements Runnable, WebSocket {

	/**
	 * The URI this channel is supposed to connect to.
	 */
	protected URI uri = null;

	private WebSocketImpl engine = null;

	private Socket socket = null;

	private InputStream istream;

	private OutputStream ostream;

	private Proxy proxy = Proxy.NO_PROXY;

	private Thread writeThread;

	private Draft draft;

	private Map<String,String> headers;

	private CountDownLatch connectLatch = new CountDownLatch( 1 );

	private CountDownLatch closeLatch = new CountDownLatch( 1 );

	private int connectTimeout = 0;

	/**
	 * Constructs a WebSocketClient instance and sets it to the connect to the
	 * specified URI. The channel does not attampt to connect automatically. The connection
	 * will be established once you call <var>connect</var>.
	 *
	 * @param serverUri the server URI to connect to
	 */
	public WebSocketClient( URI serverUri ) {
		this( serverUri, new Draft_6455());
	}

	/**
	 * Constructs a WebSocketClient instance and sets it to the connect to the
	 * specified URI. The channel does not attampt to connect automatically. The connection
	 * will be established once you call <var>connect</var>.
	 * @param serverUri the server URI to connect to
	 * @param protocolDraft The draft which should be used for this connection
	 */
	public WebSocketClient( URI serverUri , Draft protocolDraft ) {
		this( serverUri, protocolDraft, null, 0 );
	}

	/**
	 * Constructs a WebSocketClient instance and sets it to the connect to the
	 * specified URI. The channel does not attampt to connect automatically. The connection
	 * will be established once you call <var>connect</var>.
	 * @param serverUri the server URI to connect to
	 * @param protocolDraft The draft which should be used for this connection
	 * @param httpHeaders Additional HTTP-Headers
	 * @param connectTimeout The Timeout for the connection
	 */
	public WebSocketClient( URI serverUri , Draft protocolDraft , Map<String,String> httpHeaders , int connectTimeout ) {
		if( serverUri == null ) {
			throw new IllegalArgumentException();
		} else if( protocolDraft == null ) {
			throw new IllegalArgumentException( "null as draft is permitted for `WebSocketServer` only!" );
		}
		this.uri = serverUri;
		this.draft = protocolDraft;
		this.headers = httpHeaders;
		this.connectTimeout = connectTimeout;
		setTcpNoDelay( false );
		this.engine = new WebSocketImpl( this, protocolDraft );
	}

	/**
	 * Returns the URI that this WebSocketClient is connected to.
	 * @return the URI connected to
	 */
	public URI getURI() {
		return uri;
	}

	/**
	 * Returns the protocol version this channel uses.<br>
	 * For more infos see https://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
	 * @return The draft used for this client
	 */
	public Draft getDraft() {
		return draft;
	}

	/**
	 * Returns the socket to allow Hostname Verification
	 * @return the socket used for this connection
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Initiates the websocket connection. This method does not block.
	 */
	public void connect() {
		if( writeThread != null )
			throw new IllegalStateException( "WebSocketClient objects are not reuseable" );
		writeThread = new Thread( this );
		writeThread.start();
	}

	/**
	 * Same as <code>connect</code> but blocks until the websocket connected or failed to do so.<br>
	 * @return Returns whether it succeeded or not.
	 * @throws InterruptedException Thrown when the threads get interrupted
	 */
	public boolean connectBlocking() throws InterruptedException {
		connect();
		connectLatch.await();
		return engine.isOpen();
	}

	/**
	 * Initiates the websocket close handshake. This method does not block<br>
	 * In oder to make sure the connection is closed use <code>closeBlocking</code>
	 */
	public void close() {
		if( writeThread != null ) {
			engine.close( CloseFrame.NORMAL );
		}
	}
	/**
	 * Same as <code>close</code> but blocks until the websocket closed or failed to do so.<br>
	 * @throws InterruptedException Thrown when the threads get interrupted
	 */
	public void closeBlocking() throws InterruptedException {
		close();
		closeLatch.await();
	}

	/**
	 * Sends <var>text</var> to the connected websocket server.
	 * 
	 * @param text
	 *            The string which will be transmitted.
	 */
	public void send( String text ) throws NotYetConnectedException {
		engine.send( text );
	}

	/**
	 * Sends binary <var> data</var> to the connected webSocket server.
	 * 
	 * @param data
	 *            The byte-Array of data to send to the WebSocket server.
	 */
	public void send( byte[] data ) throws NotYetConnectedException {
		engine.send( data );
	}

	protected Collection<WebSocket> connections() {
		return Collections.singletonList((WebSocket ) engine );
	}


	public void sendPing() throws NotYetConnectedException {
		engine.sendPing( );
	}

	public void run() {
		try {
			if( socket == null ) {
				socket = new Socket( proxy );
			} else if( socket.isClosed() ) {
				throw new IOException();
			}
			socket.setTcpNoDelay( isTcpNoDelay() );
			if( !socket.isBound() )
				socket.connect( new InetSocketAddress( uri.getHost(), getPort() ), connectTimeout );
			istream = socket.getInputStream();
			ostream = socket.getOutputStream();

			sendHandshake();
		} catch ( /*IOException | SecurityException | UnresolvedAddressException | InvalidHandshakeException | ClosedByInterruptException | SocketTimeoutException */Exception e ) {
			onWebsocketError( engine, e );
			engine.closeConnection( CloseFrame.NEVER_CONNECTED, e.getMessage() );
			return;
		}

		writeThread = new Thread( new WebsocketWriteThread() );
		writeThread.start();

		byte[] rawbuffer = new byte[ WebSocketImpl.RCVBUF ];
		int readBytes;

		try {
			while ( !isClosing() && !isClosed() && ( readBytes = istream.read( rawbuffer ) ) != -1 ) {
				engine.decode( ByteBuffer.wrap( rawbuffer, 0, readBytes ) );
			}
			engine.eot();
		} catch ( IOException e ) {
			engine.eot();
		} catch ( RuntimeException e ) {
			// this catch case covers internal errors only and indicates a bug in this websocket implementation
			onError( e );
			engine.closeConnection( CloseFrame.ABNORMAL_CLOSE, e.getMessage() );
		}
		assert ( socket.isClosed() );
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
				throw new RuntimeException( "unknown scheme: " + scheme );
			}
		}
		return port;
	}

	private void sendHandshake() throws InvalidHandshakeException {
		String path;
		String part1 = uri.getRawPath();
		String part2 = uri.getRawQuery();
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
		engine.startHandshake( handshake );
	}

	/**
	 * This represents the state of the connection.
	 */
	public READYSTATE getReadyState() {
		return engine.getReadyState();
	}

	/**
	 * Calls subclass' implementation of <var>onMessage</var>.
	 */
	@Override
	public final void onWebsocketMessage( WebSocket conn, String message ) {
		onMessage( message );
	}

	@Override
	public final void onWebsocketMessage( WebSocket conn, ByteBuffer blob ) {
		onMessage( blob );
	}

	@Override
	public void onWebsocketMessageFragment( WebSocket conn, Framedata frame ) {
		onFragment( frame );
	}

	/**
	 * Calls subclass' implementation of <var>onOpen</var>.
	 */
	@Override
	public final void onWebsocketOpen( WebSocket conn, Handshakedata handshake ) {
		startConnectionLostTimer();
		onOpen( (ServerHandshake) handshake );
		connectLatch.countDown();
	}

	/**
	 * Calls subclass' implementation of <var>onClose</var>.
	 */
	@Override
	public final void onWebsocketClose( WebSocket conn, int code, String reason, boolean remote ) {
		stopConnectionLostTimer();
		if( writeThread != null )
			writeThread.interrupt();
		try {
			if( socket != null )
				socket.close();
		} catch ( IOException e ) {
			onWebsocketError( this, e );
		}
		onClose( code, reason, remote );
		connectLatch.countDown();
		closeLatch.countDown();
	}

	/**
	 * Calls subclass' implementation of <var>onIOError</var>.
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

	/**
	 * Send when this peer sends a close handshake
	 *
	 * @param code The codes can be looked up here: {@link CloseFrame}
	 * @param reason Additional information string
	 */
	public void onCloseInitiated( int code, String reason ) {
	}

	/** Called as soon as no further frames are accepted
	 *
	 * @param code The codes can be looked up here: {@link CloseFrame}
	 * @param reason Additional information string
	 * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
	 */
	public void onClosing( int code, String reason, boolean remote ) {
	}

	/**
	 * Getter for the engine
	 * @return the engine
	 */
	public WebSocket getConnection() {
		return engine;
	}

	@Override
	public InetSocketAddress getLocalSocketAddress( WebSocket conn ) {
		if( socket != null )
			return (InetSocketAddress) socket.getLocalSocketAddress();
		return null;
	}

	@Override
	public InetSocketAddress getRemoteSocketAddress( WebSocket conn ) {
		if( socket != null )
			return (InetSocketAddress) socket.getRemoteSocketAddress();
		return null;
	}

	// ABTRACT METHODS /////////////////////////////////////////////////////////
	public abstract void onOpen( ServerHandshake handshakedata );
	public abstract void onMessage( String message );
	public abstract void onClose( int code, String reason, boolean remote );
	public abstract void onError( Exception ex );
	public void onMessage( ByteBuffer bytes ) {
	}
	public void onFragment( Framedata frame ) {
	}

	private class WebsocketWriteThread implements Runnable {
		@Override
		public void run() {
			Thread.currentThread().setName( "WebsocketWriteThread" );
			try {
				while ( !Thread.interrupted() ) {
					ByteBuffer buffer = engine.outQueue.take();
					ostream.write( buffer.array(), 0, buffer.limit() );
					ostream.flush();
				}
			} catch ( IOException e ) {
				engine.eot();
			} catch ( InterruptedException e ) {
				// this thread is regularly terminated via an interrupt
			}
		}
	}

	public void setProxy( Proxy proxy ) {
		if( proxy == null )
			throw new IllegalArgumentException();
		this.proxy = proxy;
	}

	/**
	 * Accepts bound and unbound sockets.<br>
	 * This method must be called before <code>connect</code>.
	 * If the given socket is not yet bound it will be bound to the uri specified in the constructor.
	 * @param socket The socket which should be used for the connection
	 */
	public void setSocket( Socket socket ) {
		if( this.socket != null ) {
			throw new IllegalStateException( "socket has already been set" );
		}
		this.socket = socket;
	}

	@Override
	public void sendFragmentedFrame( Opcode op, ByteBuffer buffer, boolean fin ) {
		engine.sendFragmentedFrame( op, buffer, fin );
	}

	@Override
	public boolean isOpen() {
		return engine.isOpen();
	}

	@Override
	public boolean isFlushAndClose() {
		return engine.isFlushAndClose();
	}

	@Override
	public boolean isClosed() {
		return engine.isClosed();
	}

	@Override
	public boolean isClosing() {
		return engine.isClosing();
	}

	@Override
	public boolean isConnecting() {
		return engine.isConnecting();
	}

	@Override
	public boolean hasBufferedData() {
		return engine.hasBufferedData();
	}

	@Override
	public void close( int code ) {
		engine.close();
	}

	@Override
	public void close( int code, String message ) {
		engine.close( code, message );
	}

	@Override
	public void closeConnection( int code, String message ) {
		engine.closeConnection( code, message );
	}

	@Override
	public void send( ByteBuffer bytes ) throws IllegalArgumentException , NotYetConnectedException {
		engine.send( bytes );
	}

	@Override
	public void sendFrame( Framedata framedata ) {
		engine.sendFrame( framedata );
	}

	@Override
	public InetSocketAddress getLocalSocketAddress() {
		return engine.getLocalSocketAddress();
	}
	@Override
	public InetSocketAddress getRemoteSocketAddress() {
		return engine.getRemoteSocketAddress();
	}
	
	@Override
	public String getResourceDescriptor() {
		return uri.getPath();
	}
}
