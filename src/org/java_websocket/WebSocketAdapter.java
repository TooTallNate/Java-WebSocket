package org.java_websocket;

import org.java_websocket.Framedata.Opcode;
import org.java_websocket.exeptions.InvalidDataException;

public abstract class WebSocketAdapter implements WebSocketListener {

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketHandshakeRecievedAsServer(net.tootallnate.websocket.WebSocket, net.tootallnate.websocket.Draft, net.tootallnate.websocket.Handshakedata)
	 */
	@Override
	public HandshakeBuilder onWebsocketHandshakeRecievedAsServer( WebSocket conn, Draft draft, Handshakedata request ) throws InvalidDataException {
		return new HandshakedataImpl1();
	}

	/**
	 * This default implementation does not do anything which will cause connections to be accepted. Go ahead and overwrite it.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketHandshakeRecievedAsClient(net.tootallnate.websocket.WebSocket, net.tootallnate.websocket.Handshakedata, net.tootallnate.websocket.Handshakedata)
	 */
	@Override
	public void onWebsocketHandshakeRecievedAsClient( WebSocket conn, Handshakedata request, Handshakedata response ) throws InvalidDataException {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketMessage(net.tootallnate.websocket.WebSocket, java.lang.String)
	 */
	@Override
	public void onWebsocketMessage( WebSocket conn, String message ) {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketOpen(net.tootallnate.websocket.WebSocket, net.tootallnate.websocket.Handshakedata)
	 */
	@Override
	public void onWebsocketOpen( WebSocket conn, Handshakedata handshake ) {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketClose(net.tootallnate.websocket.WebSocket, int, java.lang.String, boolean)
	 */
	@Override
	public void onWebsocketClose( WebSocket conn, int code, String reason, boolean remote ) {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketMessage(net.tootallnate.websocket.WebSocket, byte[])
	 */
	@Override
	public void onWebsocketMessage( WebSocket conn, byte[] blob ) {
	}

	/**
	 * This default implementation will send a pong in response to the received ping.
	 * The pong frame will have the same payload as the ping frame.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketPing(net.tootallnate.websocket.WebSocket, net.tootallnate.websocket.Framedata)
	 */
	@Override
	public void onWebsocketPing( WebSocket conn, Framedata f ) {
		FramedataImpl1 resp = new FramedataImpl1 ( f );
		resp.setOptcode( Opcode.PONG );
		try {
			conn.sendFrame ( resp );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketPong(net.tootallnate.websocket.WebSocket, net.tootallnate.websocket.Framedata)
	 */
	@Override
	public void onWebsocketPong( WebSocket conn, Framedata f ) {
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
	@Override
	public String getFlashPolicy( WebSocket conn ) {
		return "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"" + conn.getLocalSocketAddress().getPort() + "\" /></cross-domain-policy>\0";
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * @see net.tootallnate.websocket.WebSocketListener#onWebsocketError(net.tootallnate.websocket.WebSocket, java.lang.Exception)
	 */
	@Override
	public void onWebsocketError( WebSocket conn, Exception ex ) {
	}

}
