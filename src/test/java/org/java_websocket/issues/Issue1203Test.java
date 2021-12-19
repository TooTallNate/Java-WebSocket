package org.java_websocket.issues;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Assert;
import org.junit.Test;

public class Issue1203Test {
  private final CountDownLatch countServerDownLatch = new CountDownLatch(1);
  private final CountDownLatch countClientDownLatch = new CountDownLatch(1);
  boolean isClosedCalled = false;
  @Test(timeout = 50000)
  public void testIssue() throws Exception {
    int port = SocketUtil.getAvailablePort();
    WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {
      @Override
      public void onOpen(WebSocket conn, ClientHandshake handshake) {
      }

      @Override
      public void onClose(WebSocket conn, int code, String reason, boolean remote) {
      }

      @Override
      public void onMessage(WebSocket conn, String message) {
      }

      @Override
      public void onError(WebSocket conn, Exception ex) {
      }

      @Override
      public void onStart() {
        countServerDownLatch.countDown();
      }
    };
    final WebSocketClient client = new WebSocketClient(new URI("ws://localhost:" + port)) {
      @Override
      public void onOpen(ServerHandshake handshakedata) {
        countClientDownLatch.countDown();
      }

      @Override
      public void onMessage(String message) {

      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
        isClosedCalled = true;
      }

      @Override
      public void onError(Exception ex) {

      }
    };

    server.setConnectionLostTimeout(10);
    server.start();
    countServerDownLatch.await();

    client.setConnectionLostTimeout(10);
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        try {
          client.connectBlocking();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };
    timer.schedule(task, 15000);
    countClientDownLatch.await();
    Thread.sleep(30000);
    Assert.assertFalse(isClosedCalled);
    client.closeBlocking();
    server.stop();
  }
}
