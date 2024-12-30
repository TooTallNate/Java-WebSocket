package org.java_websocket.server;


import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.Handshakedata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DefaultWebSocketServerFactoryTest {

  @Test
  public void testCreateWebSocket() {
    DefaultWebSocketServerFactory webSocketServerFactory = new DefaultWebSocketServerFactory();
    CustomWebSocketAdapter webSocketAdapter = new CustomWebSocketAdapter();
    WebSocketImpl webSocketImpl = webSocketServerFactory
        .createWebSocket(webSocketAdapter, new Draft_6455());
    assertNotNull(webSocketImpl, "webSocketImpl != null");
    webSocketImpl = webSocketServerFactory
        .createWebSocket(webSocketAdapter, Collections.<Draft>singletonList(new Draft_6455()));
    assertNotNull(webSocketImpl, "webSocketImpl != null");
  }

  @Test
  public void testWrapChannel() {
    DefaultWebSocketServerFactory webSocketServerFactory = new DefaultWebSocketServerFactory();
    SocketChannel channel = (new Socket()).getChannel();
    SocketChannel result = webSocketServerFactory.wrapChannel(channel, null);
    assertSame(channel, result, "channel == result");
  }

  @Test
  public void testClose() {
    DefaultWebSocketServerFactory webSocketServerFactory = new DefaultWebSocketServerFactory();
    webSocketServerFactory.close();
  }

  private static class CustomWebSocketAdapter extends WebSocketAdapter {

    @Override
    public void onWebsocketMessage(WebSocket conn, String message) {

    }

    @Override
    public void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {

    }

    @Override
    public void onWebsocketOpen(WebSocket conn, Handshakedata d) {

    }

    @Override
    public void onWebsocketClose(WebSocket ws, int code, String reason, boolean remote) {

    }

    @Override
    public void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote) {

    }

    @Override
    public void onWebsocketCloseInitiated(WebSocket ws, int code, String reason) {

    }

    @Override
    public void onWebsocketError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onWriteDemand(WebSocket conn) {

    }

    @Override
    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
      return null;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
      return null;
    }
  }
}
