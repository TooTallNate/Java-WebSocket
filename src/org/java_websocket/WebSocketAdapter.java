package org.java_websocket;

import org.java_websocket.drafts.Draft;
import org.java_websocket.exeptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

public abstract class WebSocketAdapter implements WebSocketListener {

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketHandshakeRecievedAsServer(org.java_websocket.WebSocket, org.java_websocket.Draft, org.java_websocket.Handshakedata)
	 */
	@Override
	public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer( WebSocket conn, Draft draft, ClientHandshake request ) throws InvalidDataException {
		return new HandshakeImpl1Server();
	}

	/**
	 * This default implementation does not do anything which will cause connections to be accepted. Go ahead and overwrite it.
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketHandshakeRecievedAsClient(org.java_websocket.WebSocket, org.java_websocket.Handshakedata, org.java_websocket.Handshakedata)
	 */
	@Override
	public void onWebsocketHandshakeReceivedAsClient( WebSocket conn, ClientHandshake request, ServerHandshake response ) throws InvalidDataException {
	}

	/**
	 * This default implementation does not do anything which will cause the connections to always progress.
	 * 
	 * @see net.tootallnate.websocket.WebSocketListener#onHandshakeSentAsClient(net.tootallnate.websocket.WebSocket, net.tootallnate.websocket.Handshakedata)
	 */
	@Override
	public void onWebsocketHandshakeSentAsClient( WebSocket conn, ClientHandshake request ) throws InvalidDataException {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketMessage(org.java_websocket.WebSocket, java.lang.String)
	 */
	@Override
	public void onWebsocketMessage( WebSocket conn, String message ) {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketOpen(org.java_websocket.WebSocket, org.java_websocket.Handshakedata)
	 */
	@Override
	public void onWebsocketOpen( WebSocket conn, Handshakedata handshake ) {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketClose(org.java_websocket.WebSocket, int, java.lang.String, boolean)
	 */
	@Override
	public void onWebsocketClose( WebSocket conn, int code, String reason, boolean remote ) {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketMessage(org.java_websocket.WebSocket, byte[])
	 */
	@Override
	public void onWebsocketMessage( WebSocket conn, byte[] blob ) {
	}

	/**
	 * This default implementation will send a pong in response to the received ping.
	 * The pong frame will have the same payload as the ping frame.
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketPing(org.java_websocket.WebSocket, org.java_websocket.Framedata)
	 */
	@Override
	public void onWebsocketPing( WebSocket conn, Framedata f ) {
		FramedataImpl1 resp = new FramedataImpl1( f );
		resp.setOptcode( Opcode.PONG );
		try {
			conn.sendFrame( resp );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketPong(org.java_websocket.WebSocket, org.java_websocket.Framedata)
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
	 * 
	 * @see org.java_websocket.WebSocketListener#onWebsocketError(org.java_websocket.WebSocket, java.lang.Exception)
	 */
	@Override
	public void onWebsocketError( WebSocket conn, Exception ex ) {
	}

}
