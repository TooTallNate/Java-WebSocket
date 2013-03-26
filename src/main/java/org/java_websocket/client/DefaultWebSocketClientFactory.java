package org.java_websocket.client;

import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;

public class DefaultWebSocketClientFactory implements WebSocketClient.WebSocketClientFactory {
	/**
	 * 
	 */
	private final WebSocketClient webSocketClient;
	/**
	 * @param webSocketClient
	 */
	public DefaultWebSocketClientFactory( WebSocketClient webSocketClient ) {
		this.webSocketClient = webSocketClient;
	}
	@Override
	public WebSocket createWebSocket( WebSocketAdapter a, Draft d, Socket s ) {
		return new WebSocketImpl( this.webSocketClient, d );
	}
	@Override
	public WebSocket createWebSocket( WebSocketAdapter a, List<Draft> d, Socket s ) {
		return new WebSocketImpl( this.webSocketClient, d );
	}
	@Override
	public ByteChannel wrapChannel( SocketChannel channel, SelectionKey c, String host, int port ) {
		if( c == null )
			return channel;
		return channel;
	}
}