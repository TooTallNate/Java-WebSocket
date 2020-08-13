package org.java_websocket.server;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.Handshakedata;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CustomSSLWebSocketServerFactoryTest {

  final String[] emptyArray = new String[0];

  @Test
  public void testConstructor() throws NoSuchAlgorithmException {
    try {
      new CustomSSLWebSocketServerFactory(null, null, null);
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
      // Good
    }
    try {
      new CustomSSLWebSocketServerFactory(null, null, null, null);
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
      // Good
    }
    try {
      new CustomSSLWebSocketServerFactory(SSLContext.getDefault(), null, null, null);
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
    }
    try {
      new CustomSSLWebSocketServerFactory(SSLContext.getDefault(), null, null);
    } catch (IllegalArgumentException e) {
      fail("IllegalArgumentException should not be thrown");
    }
    try {
      new CustomSSLWebSocketServerFactory(SSLContext.getDefault(), Executors.newCachedThreadPool(),
          null, null);
    } catch (IllegalArgumentException e) {
      fail("IllegalArgumentException should not be thrown");
    }
    try {
      new CustomSSLWebSocketServerFactory(SSLContext.getDefault(), Executors.newCachedThreadPool(),
          emptyArray, null);
    } catch (IllegalArgumentException e) {
      fail("IllegalArgumentException should not be thrown");
    }
    try {
      new CustomSSLWebSocketServerFactory(SSLContext.getDefault(), Executors.newCachedThreadPool(),
          null, emptyArray);
    } catch (IllegalArgumentException e) {
      fail("IllegalArgumentException should not be thrown");
    }
    try {
      new CustomSSLWebSocketServerFactory(SSLContext.getDefault(), Executors.newCachedThreadPool(),
          emptyArray, emptyArray);
    } catch (IllegalArgumentException e) {
      fail("IllegalArgumentException should not be thrown");
    }
  }

  @Test
  public void testCreateWebSocket() throws NoSuchAlgorithmException {
    CustomSSLWebSocketServerFactory webSocketServerFactory = new CustomSSLWebSocketServerFactory(
        SSLContext.getDefault(), null, null);
    CustomWebSocketAdapter webSocketAdapter = new CustomWebSocketAdapter();
    WebSocketImpl webSocketImpl = webSocketServerFactory
        .createWebSocket(webSocketAdapter, new Draft_6455());
    assertNotNull("webSocketImpl != null", webSocketImpl);
    webSocketImpl = webSocketServerFactory
        .createWebSocket(webSocketAdapter, Collections.<Draft>singletonList(new Draft_6455()));
    assertNotNull("webSocketImpl != null", webSocketImpl);
  }

  @Test
  public void testWrapChannel() throws IOException, NoSuchAlgorithmException {
    CustomSSLWebSocketServerFactory webSocketServerFactory = new CustomSSLWebSocketServerFactory(
        SSLContext.getDefault(), null, null);
    SocketChannel channel = SocketChannel.open();
    try {
      ByteChannel result = webSocketServerFactory.wrapChannel(channel, null);
    } catch (NotYetConnectedException e) {
      //We do not really connect
    }
    channel.close();
    webSocketServerFactory = new CustomSSLWebSocketServerFactory(SSLContext.getDefault(),
        new String[]{"TLSv1.2"},
        new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"});
    channel = SocketChannel.open();
    try {
      ByteChannel result = webSocketServerFactory.wrapChannel(channel, null);
    } catch (NotYetConnectedException e) {
      //We do not really connect
    }
    channel.close();
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
