package org.java_websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;

import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;

public interface WebSocket {
	/**
	 * Enum which represents the states a websocket may be in
	 */
	public enum Role {
		CLIENT, SERVER
	}

	/**
	 * Enum which represents the state a websocket may be in
	 */
	public enum READYSTATE {
		NOT_YET_CONNECTED, CONNECTING, OPEN, CLOSING, CLOSED
	}

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
	 * @param code the closing code
	 * @param message the closing message
	 */
	public void close( int code, String message );

	/**
	 * sends the closing handshake.
	 * may be send in response to an other handshake.
	 * @param code the closing code
	 */
	public void close( int code );

	/** Convenience function which behaves like close(CloseFrame.NORMAL) */
	public void close();

	/**
	 * This will close the connection immediately without a proper close handshake.
	 * The code and the message therefore won't be transfered over the wire also they will be forwarded to onClose/onWebsocketClose.
	 * @param code the closing code
	 * @param message the closing message
	 **/
	public abstract void closeConnection( int code, String message );

	/**
	 * Send Text data to the other end.
	 *
	 * @param text the text data to send
	 * @throws NotYetConnectedException websocket is not yet connected
	 */
	public abstract void send( String text ) throws NotYetConnectedException;

	/**
	 * Send Binary data (plain bytes) to the other end.
	 *
	 * @param bytes the binary data to send
	 * @throws IllegalArgumentException the data is null
	 * @throws NotYetConnectedException websocket is not yet connected
	 */
	public abstract void send( ByteBuffer bytes ) throws IllegalArgumentException , NotYetConnectedException;

	/**
	 * Send Binary data (plain bytes) to the other end.
	 *
	 * @param bytes the byte array to send
	 * @throws IllegalArgumentException the data is null
	 * @throws NotYetConnectedException websocket is not yet connected
	 */
	public abstract void send( byte[] bytes ) throws IllegalArgumentException , NotYetConnectedException;

	/**
	 * Send a frame to the other end
	 * @param framedata the frame to send to the other end
	 */
	public abstract void sendFrame( Framedata framedata );

	/**
	 * Allows to send continuous/fragmented frames conveniently. <br>
	 * For more into on this frame type see http://tools.ietf.org/html/rfc6455#section-5.4<br>
	 * 
	 * If the first frame you send is also the last then it is not a fragmented frame and will received via onMessage instead of onFragmented even though it was send by this method.
	 * 
	 * @param op
	 *            This is only important for the first frame in the sequence. Opcode.TEXT, Opcode.BINARY are allowed.
	 * @param buffer
	 *            The buffer which contains the payload. It may have no bytes remaining.
	 * @param fin
	 *            true means the current frame is the last in the sequence.
	 **/
	public abstract void sendFragmentedFrame( Opcode op, ByteBuffer buffer, boolean fin );

	/**
	 * Checks if the websocket has buffered data
	 * @return has the websocket buffered data
	 */
	public abstract boolean hasBufferedData();

	/**
	 * Returns the address of the endpoint this socket is connected to, or{@code null} if it is unconnected.
	 *
	 * @return never returns null
	 */
	public abstract InetSocketAddress getRemoteSocketAddress();

	/**
	 * Returns the address of the endpoint this socket is bound to.
	 *
	 * @return never returns null
	 */
	public abstract InetSocketAddress getLocalSocketAddress();

	/**
	 * Is the websocket in the state CONNECTING
	 * @return state equals READYSTATE.CONNECTING
	 */
	public abstract boolean isConnecting();

	/**
	 * Is the websocket in the state OPEN
	 * @return state equals READYSTATE.OPEN
	 */
	public abstract boolean isOpen();

	/**
	 * Is the websocket in the state CLOSING
	 * @return state equals READYSTATE.CLOSING
	 */
	public abstract boolean isClosing();

	/**
	 * Returns true when no further frames may be submitted<br>
	 * This happens before the socket connection is closed.
	 * @return true when no further frames may be submitted
	 */
	public abstract boolean isFlushAndClose();

	/**
	 * Is the websocket in the state CLOSED
	 * @return state equals READYSTATE.CLOSED
	 */
	public abstract boolean isClosed();

	/**
	 * Getter for the draft
	 * @return the used draft
	 */
	public abstract Draft getDraft();

	/**
	 * Retrieve the WebSocket 'readyState'.
	 * This represents the state of the connection.
	 * It returns a numerical value, as per W3C WebSockets specs.
	 * 
	 * @return Returns '0 = CONNECTING', '1 = OPEN', '2 = CLOSING' or '3 = CLOSED'
	 */
	public abstract READYSTATE getReadyState();
	
	/**
	 * Returns the HTTP Request-URI as defined by http://tools.ietf.org/html/rfc2616#section-5.1.2<br>
	 * If the opening handshake has not yet happened it will return null.
	 * @return Returns the decoded path component of this URI.
	 **/
	public abstract String getResourceDescriptor();
}