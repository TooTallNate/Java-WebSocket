package org.java_websocket.issues;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1160Test {
  private final CountDownLatch countServerStart = new CountDownLatch(1);

  static class TestClient extends WebSocketClient {
    private final CountDownLatch onCloseLatch;

    public TestClient(URI uri, CountDownLatch latch) {
      super(uri);
      onCloseLatch = latch;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
      onCloseLatch.countDown();
    }

    @Override
    public void onError(Exception ex) {
    }
  }


  @Test
  @Timeout(5000)
  public void nonFatalErrorShallBeHandledByServer() throws Exception {
    final AtomicInteger isServerOnErrorCalledCounter = new AtomicInteger(0);

    int port = SocketUtil.getAvailablePort();
    WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {
      @Override
      public void onOpen(WebSocket conn, ClientHandshake handshake) {
      }

      @Override
      public void onClose(WebSocket conn, int code, String reason, boolean remote) {
      }

      @Override
      public void onMessage(WebSocket conn, ByteBuffer message) {
        throw new Error("Some error");
      }

      @Override
      public void onMessage(WebSocket conn, String message) {
        throw new Error("Some error");
      }

      @Override
      public void onError(WebSocket conn, Exception ex) {
        isServerOnErrorCalledCounter.incrementAndGet();
      }

      @Override
      public void onStart() {
        countServerStart.countDown();
      }
    };


    server.setConnectionLostTimeout(10);
    server.start();
    countServerStart.await();

    URI uri = new URI("ws://localhost:" + port);

    int CONNECTION_COUNT = 3;
    for (int i = 0; i < CONNECTION_COUNT; i++) {
      CountDownLatch countClientDownLatch = new CountDownLatch(1);
      WebSocketClient client = new TestClient(uri, countClientDownLatch);
      client.setConnectionLostTimeout(10);

      client.connectBlocking();
      client.send(new byte[100]);
      countClientDownLatch.await();
      client.closeBlocking();
    }

    assertEquals(CONNECTION_COUNT, isServerOnErrorCalledCounter.get());

    server.stop();
  }

  @Test
  @Timeout(5000)
  public void fatalErrorShallNotBeHandledByServer() throws Exception {
    int port = SocketUtil.getAvailablePort();

    final CountDownLatch countServerDownLatch = new CountDownLatch(1);
    WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {
      @Override
      public void onOpen(WebSocket conn, ClientHandshake handshake) {
      }

      @Override
      public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        countServerDownLatch.countDown();
      }

      @Override
      public void onMessage(WebSocket conn, ByteBuffer message) {
        throw new OutOfMemoryError("Some intentional error");
      }

      @Override
      public void onMessage(WebSocket conn, String message) {
        throw new OutOfMemoryError("Some error");
      }

      @Override
      public void onError(WebSocket conn, Exception ex) {
      }

      @Override
      public void onStart() {
        countServerStart.countDown();
      }
    };


    server.setConnectionLostTimeout(10);
    server.start();
    countServerStart.await();

    URI uri = new URI("ws://localhost:" + port);

    CountDownLatch countClientDownLatch = new CountDownLatch(1);
    WebSocketClient client = new TestClient(uri, countClientDownLatch);
    client.setConnectionLostTimeout(10);

    client.connectBlocking();
    client.send(new byte[100]);
    countClientDownLatch.await();
    countServerDownLatch.await();
    assertEquals(0,countClientDownLatch.getCount());
    assertEquals(0, countServerDownLatch.getCount());
  }
}
