/*
 * Copyright (c) 2010-2020 Nathan Rajlich
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * This is a "smart" chat server which will exit when no more clients are left, in order to demonstrate socket activation
 */
public class SocketActivation extends WebSocketServer {

  AtomicInteger clients = new AtomicInteger(0);

  public SocketActivation(ServerSocketChannel chan) {
    super(chan);
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    conn.send("Welcome to the server!"); //This method sends a message to the new client
    broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
    if(clients.get() == 0) {
      broadcast("You are the first client to join");
    }
    System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    clients.incrementAndGet();
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    broadcast(conn + " has left the room!");
    System.out.println(conn + " has left the room!");
    if(clients.decrementAndGet() <= 0) {
      System.out.println("No more clients left, exiting");
      System.exit(0);
    }
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    broadcast(message);
    System.out.println(conn + ": " + message);
  }

  @Override
  public void onMessage(WebSocket conn, ByteBuffer message) {
    broadcast(message.array());
    System.out.println(conn + ": " + message);
  }


  public static void main(String[] args) throws InterruptedException, IOException {
    if(System.inheritedChannel() == null) {
      System.err.println("System.inheritedChannel() is null, make sure this program is started with file descriptor zero being a listening socket");
      System.exit(1);
    }
    SocketActivation s = new SocketActivation((ServerSocketChannel)System.inheritedChannel());
    s.start();
    System.out.println(">>>> SocketActivation started on port: " + s.getPort() + " <<<<");
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace();
  }

  @Override
  public void onStart() {
    System.out.println("Server started!");
  }

}
