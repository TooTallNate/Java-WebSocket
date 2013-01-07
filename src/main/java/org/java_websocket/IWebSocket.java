package org.java_websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;

import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshakeBuilder;

public interface IWebSocket {

	enum Role {
		CLIENT,
		SERVER
	}

	enum READYSTATE {
		NOT_YET_CONNECTED,
		CONNECTING,
		OPEN,
		CLOSING,
		CLOSED;
	}

	static int	RCVBUF				= 65536;

	/**
	 * The default port of WebSockets, as defined in the spec. If the nullary
	 * constructor is used, DEFAULT_PORT will be the port the WebSocketServer
	 * is binded to. Note that ports under 1024 usually require root permissions.
	 */
	int			DEFAULT_PORT		= 80;
	int			DEFAULT_WSS_PORT	= 443;

	/**
	 * sends the closing handshake.
	 * may be send in response to an other handshake.
	 */
	abstract void close(int code, String message);

	void close(int code);

	/**
	 * This will close the connection immediately without a proper close handshake.
	 * The code and the message therefore won't be transfered over the wire also they will be forwarded to onClose/onWebsocketClose.
	 **/
	void closeConnection(int code, String message);

	void close(InvalidDataException e);

	/**
	 * Send Text data to the other end.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotYetConnectedException
	 */
	void send(String text) throws NotYetConnectedException;

	/**
	 * Send Binary data (plain bytes) to the other end.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotYetConnectedException
	 */
	void send(ByteBuffer bytes) throws IllegalArgumentException, NotYetConnectedException;

	void send(byte[] bytes) throws IllegalArgumentException, NotYetConnectedException;

	void sendFrame(Framedata framedata);

	boolean hasBufferedData();

	void startHandshake(ClientHandshakeBuilder handshakedata) throws InvalidHandshakeException;

	InetSocketAddress getRemoteSocketAddress();

	InetSocketAddress getLocalSocketAddress();

	boolean isConnecting();

	boolean isOpen();

	boolean isClosing();

	boolean isClosed();

	Draft getDraft();

	/**
	 * Retrieve the WebSocket 'readyState'.
	 * This represents the state of the connection.
	 * It returns a numerical value, as per W3C WebSockets specs.
	 * 
	 * @return Returns '0 = CONNECTING', '1 = OPEN', '2 = CLOSING' or '3 = CLOSED'
	 */
	READYSTATE getReadyState();

}