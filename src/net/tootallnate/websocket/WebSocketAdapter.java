package net.tootallnate.websocket;

import java.io.IOException;


public class WebSocketAdapter implements WebSocketListener {

	@Override
	public HandshakeBuilder onHandshakeRecievedAsServer( WebSocket conn , Draft draft , Handshakedata request ) throws IOException {
		return new HandshakedataImpl1();
	}

	@Override
	public boolean onHandshakeRecievedAsClient( WebSocket conn , Handshakedata request , Handshakedata response ) throws IOException {
		return true;
	}

	@Override
	public void onMessage( WebSocket conn , String message ) {
	}

	@Override
	public void onOpen( WebSocket conn ) {
	}

	@Override
	public void onClose( WebSocket conn ) {
	}

	@Override
	public void onError( Throwable ex ) {
	}

	@Override
	public void onMessage( WebSocket conn , byte[] blob ) {
	}

	@Override
	public void onPong( ) {
	}
}
