package org.java_websocket;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.util.List;

import org.java_websocket.drafts.Draft;

public interface WebSocketFactory {
	public WebSocket createWebSocket( WebSocketAdapter a, Draft d, Socket s );
	public WebSocket createWebSocket( WebSocketAdapter a, List<Draft> drafts, Socket s );
	/**
	 * Allows to wrap the Socketchannel( key.channel() ) to insert a protocol layer( like ssl or proxy authentication) beyond the ws layer.
	 * 
	 * @param key
	 *            a SelectionKey of an open SocketChannel.
	 * @return The channel on which the read and write operations will be performed.<br>
	 */
	public ByteChannel wrapChannel( SelectionKey key ) throws IOException;
}
