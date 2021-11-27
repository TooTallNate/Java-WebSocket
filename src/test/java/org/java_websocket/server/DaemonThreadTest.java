package org.java_websocket.server;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.*;
import org.java_websocket.client.*;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class DaemonThreadTest {

  @Test(timeout = 1000)
  public void test_AllCreatedThreadsAreDaemon() throws Throwable {

    Set<Thread> threadSet1 = Thread.getAllStackTraces().keySet();
    final CountDownLatch ready = new CountDownLatch(1);

    WebSocketServer server = new WebSocketServer(new InetSocketAddress(SocketUtil.getAvailablePort())) {
      @Override
      public void onOpen(WebSocket conn, ClientHandshake handshake) {}
      @Override
      public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
      @Override
      public void onMessage(WebSocket conn, String message) {}
      @Override
      public void onError(WebSocket conn, Exception ex) {}
      @Override
      public void onStart() {}
    };
    server.setDaemon(true);
    server.setDaemon(false);
    server.setDaemon(true);
    server.start();

    WebSocketClient client = new WebSocketClient(URI.create("ws://localhost:" + server.getPort())) {
      @Override
      public void onOpen(ServerHandshake handshake) {
        ready.countDown();
      }
      @Override
      public void onClose(int code, String reason, boolean remote) {}
      @Override
      public void onMessage(String message) {}
      @Override
      public void onError(Exception ex) {}
    };
    client.setDaemon(false);
    client.setDaemon(true);
    client.connect();

    ready.await();
    Set<Thread> threadSet2 = Thread.getAllStackTraces().keySet();
    threadSet2.removeAll(threadSet1);

    assertTrue("new threads created (no new threads indicates issue in test)", !threadSet2.isEmpty());

    for (Thread t : threadSet2)
      assertTrue(t.getName(), t.isDaemon());

    boolean exception = false;
    try {
      server.setDaemon(false);
    } catch(IllegalStateException e) {
      exception = true;
    }
    assertTrue("exception was thrown when calling setDaemon on a running server", exception);

    server.stop();
  }
}
