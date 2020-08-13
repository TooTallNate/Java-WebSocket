/*
 * Copyright (c) 2010-2020 Olivier Ayache
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package org.java_websocket.issues;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import javax.net.SocketFactory;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Assert;
import org.junit.Test;

public class Issue962Test {

  private static class TestSocketFactory extends SocketFactory {

    private final SocketFactory socketFactory = SocketFactory.getDefault();
    private final String bindingAddress;

    public TestSocketFactory(String bindingAddress) {
      this.bindingAddress = bindingAddress;
    }

    @Override
    public Socket createSocket() throws IOException {
      Socket socket = socketFactory.createSocket();
      socket.bind(new InetSocketAddress(bindingAddress, 0));
      return socket;
    }

    @Override
    public Socket createSocket(String string, int i) throws IOException, UnknownHostException {
      Socket socket = socketFactory.createSocket(string, i);
      socket.bind(new InetSocketAddress(bindingAddress, 0));
      return socket;
    }

    @Override
    public Socket createSocket(String string, int i, InetAddress ia, int i1)
        throws IOException, UnknownHostException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(InetAddress ia, int i) throws IOException {
      Socket socket = socketFactory.createSocket(ia, i);
      socket.bind(new InetSocketAddress(bindingAddress, 0));
      return socket;
    }

    @Override
    public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
      throw new UnsupportedOperationException();
    }

  }

  @Test(timeout = 2000)
  public void testIssue() throws IOException, URISyntaxException, InterruptedException {
    int port = SocketUtil.getAvailablePort();
    WebSocketClient client = new WebSocketClient(new URI("ws://127.0.0.1:" + port)) {
      @Override
      public void onOpen(ServerHandshake handshakedata) {
      }

      @Override
      public void onMessage(String message) {
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
      }

      @Override
      public void onError(Exception ex) {
        Assert.fail(ex.toString() + " should not occur");
      }
    };

    String bindingAddress = "127.0.0.1";

    client.setSocketFactory(new TestSocketFactory(bindingAddress));

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
      }
    };

    server.start();
    client.connectBlocking();
    Assert.assertEquals(bindingAddress, client.getSocket().getLocalAddress().getHostAddress());
    Assert.assertNotEquals(0, client.getSocket().getLocalPort());
    Assert.assertTrue(client.getSocket().isConnected());
  }

}
