package net.tootallnate.websocket;

import java.io.IOException;

import net.tootallnate.websocket.Framedata.Opcode;

public abstract class WebSocketAdapter implements WebSocketListener {

	@Override
	public HandshakeBuilder onHandshakeRecievedAsServer( WebSocket conn, Draft draft, Handshakedata request ) throws IOException {
		return new HandshakedataImpl1();
	}

	@Override
	public boolean onHandshakeRecievedAsClient( WebSocket conn, Handshakedata request, Handshakedata response ) throws IOException {
		return true;
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
	}

	@Override
	public void onOpen( WebSocket conn, Handshakedata handshake ) {
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
	}

	@Override
	public void onMessage( WebSocket conn, byte[] blob ) {
	}

	@Override
	public void onPing( WebSocket conn, Framedata f ) {
		FramedataImpl1 resp = new FramedataImpl1 ( f );
		resp.setOptcode( Opcode.PONG );
		try {
			conn.sendFrame ( resp );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPong( WebSocket conn, Framedata f ) {
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

	@Override
	public void onError( WebSocket conn, Exception ex ) {
	}

}
