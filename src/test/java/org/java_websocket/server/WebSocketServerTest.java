package org.java_websocket.server;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class WebSocketServerTest extends WebSocketClient {

	public WebSocketServerTest() throws URISyntaxException {
		super(new URI("ws://localhost:9999"));
	}

	@Test
	@Ignore
	public void testServer() throws Exception {
		MyWebSocketServer server = new MyWebSocketServer();

		try {
			server.start();

			WebSocketClient client1 = new WebSocketServerTest();
			try {
				client1.connectBlocking();
				client1.send("X");
			} finally {
				client1.closeBlocking();
			}

			 WebSocketClient client2 = new WebSocketServerTest();
			try {
				 client2.connectBlocking();
				 client2.send("X");
			} finally {
				 client2.closeBlocking();
			}
		} finally {
			server.stop();
		}

		Assert.assertFalse(server.error);
	}

	public static class MyWebSocketServer extends WebSocketServer {

		private boolean error;

		public MyWebSocketServer() throws UnknownHostException {
			super(new InetSocketAddress(9999));
		}

		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			// TODO Auto-generated method stub
			boolean a = true;
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			// TODO Auto-generated method stub
			boolean a = true;
		}

		@Override
		public void onMessage(WebSocket conn, String message) {
			// TODO Auto-generated method stub
			boolean a = true;
			conn.send(message);
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			// TODO Auto-generated method stub
			boolean a = true;
			this.error = true;
		}

	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		// TODO Auto-generated method stub
		boolean a = true;
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		boolean a = true;
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		boolean a = true;
	}

	@Override
	public void onError(Exception ex) {
		// TODO Auto-generated method stub
		boolean a = true;
	}

}
