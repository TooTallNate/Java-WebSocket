/*
 * Copyright (c) 2010-2020 Nathan Rajlich
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.java_websocket.issues;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;
import javax.net.ssl.SSLContext;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SSLContextUtil;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;

public class Issue825Test {


  @Test(timeout = 15000)
  public void testIssue()
      throws IOException, URISyntaxException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, InterruptedException {
    final CountDownLatch countClientOpenLatch = new CountDownLatch(3);
    final CountDownLatch countClientMessageLatch = new CountDownLatch(3);
    final CountDownLatch countServerDownLatch = new CountDownLatch(1);
    int port = SocketUtil.getAvailablePort();
    final WebSocketClient webSocket = new WebSocketClient(new URI("wss://localhost:" + port)) {
      @Override
      public void onOpen(ServerHandshake handshakedata) {
        countClientOpenLatch.countDown();
      }

      @Override
      public void onMessage(String message) {
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
      }

      @Override
      public void onError(Exception ex) {
      }
    };
    WebSocketServer server = new MyWebSocketServer(port, countServerDownLatch,
        countClientMessageLatch);

    // load up the key store
    SSLContext sslContext = SSLContextUtil.getContext();

    server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
    webSocket.setSocketFactory(sslContext.getSocketFactory());
    server.start();
    countServerDownLatch.await();
    webSocket.connectBlocking();
    webSocket.send("hi");
    Thread.sleep(10);
    webSocket.closeBlocking();

    //Disconnect manually and reconnect blocking
    webSocket.reconnectBlocking();
    webSocket.send("it's");
    Thread.sleep(10000);
    webSocket.closeBlocking();
    //Disconnect manually and reconnect
    webSocket.reconnect();
    countClientOpenLatch.await();
    webSocket.send("me");
    Thread.sleep(100);
    webSocket.closeBlocking();
    countClientMessageLatch.await();
  }


  private static class MyWebSocketServer extends WebSocketServer {

    private final CountDownLatch countServerLatch;
    private final CountDownLatch countClientMessageLatch;


    public MyWebSocketServer(int port, CountDownLatch serverDownLatch,
        CountDownLatch countClientMessageLatch) {
      super(new InetSocketAddress(port));
      this.countServerLatch = serverDownLatch;
      this.countClientMessageLatch = countClientMessageLatch;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
      countClientMessageLatch.countDown();
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
      ex.printStackTrace();
    }

    @Override
    public void onStart() {
      countServerLatch.countDown();
    }
  }
}
