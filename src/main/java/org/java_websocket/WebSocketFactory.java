package org.java_websocket;

import java.util.List;

import org.java_websocket.drafts.Draft;

public interface WebSocketFactory {
	public WebSocket createWebSocket( WebSocketAdapter a, Draft d );
	public WebSocket createWebSocket( WebSocketAdapter a, List<? extends Draft> drafts );

}
