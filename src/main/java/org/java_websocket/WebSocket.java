package org.java_websocket;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;

import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;

public abstract class WebSocket {
	public enum Role {
		CLIENT, SERVER
	}

	public enum READYSTATE {
		NOT_YET_CONNECTED, CONNECTING, OPEN, CLOSING, CLOSED;
	}

	public static int RCVBUF = 16384;

	public static/*final*/boolean DEBUG = false; // must be final in the future in order to take advantage of VM optimization

	/**
	 * The default port of WebSockets, as defined in the spec. If the nullary
	 * constructor is used, DEFAULT_PORT will be the port the WebSocketServer
	 * is binded to. Note that ports under 1024 usually require root permissions.
	 */
	public static final int DEFAULT_PORT = 80;

	public static final int DEFAULT_WSS_PORT = 443;

	/**
	 * sends the closing handshake.
	 * may be send in response to an other handshake.
	 */
	public abstract void close( int code, String message );

	public abstract void close( int code );

	/**
	 * This will close the connection immediately without a proper close handshake.
	 * The code and the message therefore won't be transfered over the wire also they will be forwarded to onClose/onWebsocketClose.
	 **/
	public abstract void closeConnection( int code, String message );

	protected abstract void close( InvalidDataException e );

	/**
	 * Send Text data to the other end.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotYetConnectedException
	 */
	public abstract void send( String text ) throws NotYetConnectedException;

	/**
	 * Send Binary data (plain bytes) to the other end.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotYetConnectedException
	 */
	public abstract void send( ByteBuffer bytes ) throws IllegalArgumentException , NotYetConnectedException;

	public abstract void send( byte[] bytes ) throws IllegalArgumentException , NotYetConnectedException;

	public abstract void sendFrame( Framedata framedata );

	public abstract boolean hasBufferedData();

	/**
	 * @returns null when connections is closed
	 * @see Socket#getRemoteSocketAddress()
	 */
	public abstract InetSocketAddress getRemoteSocketAddress();

	/**
	 * @returns null when connections is closed
	 * @see Socket#getLocalSocketAddress()
	 */
	public abstract InetSocketAddress getLocalSocketAddress();

	public abstract boolean isConnecting();

	public abstract boolean isOpen();

	public abstract boolean isClosing();

	/**
	 * Returns true when no further frames may be submitted<br>
	 * This happens before the socket connection is closed.
	 */
	public abstract boolean isFlushAndClose();

	/** Returns whether the close handshake has been completed and the socket is closed. */
	public abstract boolean isClosed();

	public abstract Draft getDraft();

	/**
	 * Retrieve the WebSocket 'readyState'.
	 * This represents the state of the connection.
	 * It returns a numerical value, as per W3C WebSockets specs.
	 * 
	 * @return Returns '0 = CONNECTING', '1 = OPEN', '2 = CLOSING' or '3 = CLOSED'
	 */
	public abstract READYSTATE getReadyState();
}