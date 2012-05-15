package org.java_websocket;

import java.nio.channels.SocketChannel;
import java.util.List;

import org.java_websocket.drafts.Draft;

public interface WebSocketFactory {
	public WebSocket createWebSocket( WebSocketAdapter a, Draft d, SocketChannel c );
	public WebSocket createWebSocket( WebSocketServer a, List<Draft> drafts, SocketChannel c );
}
