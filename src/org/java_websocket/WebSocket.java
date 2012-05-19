package org.java_websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;

import org.java_websocket.drafts.Draft.HandshakeState;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshakeBuilder;

public abstract class WebSocket {
	public enum Role {
		CLIENT, SERVER
	}

	public static int RCVBUF = 64;

	public static/*final*/boolean DEBUG = false; // must be final in the future in order to take advantage of VM optimization

	public static final int READY_STATE_CONNECTING = 0;
	public static final int READY_STATE_OPEN = 1;
	public static final int READY_STATE_CLOSING = 2;
	public static final int READY_STATE_CLOSED = 3;
	/**
	 * The default port of WebSockets, as defined in the spec. If the nullary
	 * constructor is used, DEFAULT_PORT will be the port the WebSocketServer
	 * is binded to. Note that ports under 1024 usually require root permissions.
	 */
	public static final int DEFAULT_PORT = 80;

	/**
	 * sends the closing handshake.
	 * may be send in response to an other handshake.
	 */
	public abstract void close( int code, String message );

	public abstract void closeDirect( int code, String message ) throws IOException;

	/**
	 * closes the socket no matter if the closing handshake completed.
	 * Does not send any not yet written data before closing.
	 * Calling this method more than once will have no effect.
	 * 
	 * @param remote
	 *            Indicates who "generated" <code>code</code>.<br>
	 *            <code>true</code> means that this endpoint received the <code>code</code> from the other endpoint.<br>
	 *            false means this endpoint decided to send the given code,<br>
	 *            <code>remote</code> may also be true if this endpoint started the closing handshake since the other endpoint may not simply echo the <code>code</code> but close the connection the same time this endpoint does do but with an other <code>code</code>. <br>
	 **/
	public abstract void closeConnection( int code, String message, boolean remote );

	public abstract void closeConnection( int code, boolean remote );

	public abstract void close( int code );

	public abstract void close( InvalidDataException e );

	/**
	 * Send Text data to the other end.
	 * 
	 * @throws IllegalArgumentException
	 * @throws InterruptedException
	 * @throws NotYetConnectedException
	 */
	public abstract void send( String text ) throws NotYetConnectedException;

	/**
	 * Send Binary data (plain bytes) to the other end.
	 * 
	 * @throws IllegalArgumentException
	 * @throws InterruptedException
	 * @throws NotYetConnectedException
	 */
	public abstract void send( ByteBuffer bytes ) throws IllegalArgumentException , NotYetConnectedException , InterruptedException;

	public abstract void send( byte[] bytes ) throws IllegalArgumentException , NotYetConnectedException , InterruptedException;

	public abstract void sendFrame( Framedata framedata );

	public abstract boolean hasBufferedData();

	/**
	 * The amount of data in Queue, ready to be sent.
	 * 
	 * @return Amount of Data still in Queue and not sent yet of the socket
	 */
	public abstract long bufferedDataAmount();

	/** Returns whether the batch process all available data */
	public abstract boolean batch() throws IOException;

	public abstract HandshakeState isFlashEdgeCase( ByteBuffer request );

	public abstract void startHandshake( ClientHandshakeBuilder handshakedata ) throws InvalidHandshakeException , InterruptedException;

	public abstract InetSocketAddress getRemoteSocketAddress();

	public abstract InetSocketAddress getLocalSocketAddress();

	public abstract boolean isConnecting();

	public abstract boolean isOpen();

	public abstract boolean isClosing();

	public abstract boolean isClosed();

	/**
	 * Retrieve the WebSocket 'readyState'.
	 * This represents the state of the connection.
	 * It returns a numerical value, as per W3C WebSockets specs.
	 * 
	 * @return Returns '0 = CONNECTING', '1 = OPEN', '2 = CLOSING' or '3 = CLOSED'
	 */
	public abstract int getReadyState();
}