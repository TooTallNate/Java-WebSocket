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

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * This example shows how to reject a handshake as a server from a client.
 * <p>
 * For this you have to override onWebsocketHandshakeReceivedAsServer in your WebSocketServer class
 */
public class ServerRejectHandshakeExample extends ChatServer {

  public ServerRejectHandshakeExample(int port) {
    super(new InetSocketAddress(port));
  }

  @Override
  public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft,
      ClientHandshake request) throws InvalidDataException {
    ServerHandshakeBuilder builder = super
        .onWebsocketHandshakeReceivedAsServer(conn, draft, request);
    //In this example we don't allow any resource descriptor ( "ws://localhost:8887/?roomid=1 will be rejected but ws://localhost:8887 is fine)
    if (!request.getResourceDescriptor().equals("/")) {
      throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Not accepted!");
    }
    //If there are no cookies set reject it as well.
    if (!request.hasFieldValue("Cookie")) {
      throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Not accepted!");
    }
    //If the cookie does not contain a specific value
    if (!request.getFieldValue("Cookie").equals("username=nemo")) {
      throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Not accepted!");
    }
    //If there is a Origin Field, it has to be localhost:8887
    if (request.hasFieldValue("Origin")) {
      if (!request.getFieldValue("Origin").equals("localhost:8887")) {
        throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Not accepted!");
      }
    }
    return builder;
  }


  public static void main(String[] args) throws InterruptedException, IOException {
    int port = 8887; // 843 flash policy port
    try {
      port = Integer.parseInt(args[0]);
    } catch (Exception ex) {
    }
    ServerRejectHandshakeExample s = new ServerRejectHandshakeExample(port);
    s.start();
    System.out.println("Server started on port: " + s.getPort());

    BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      String in = sysin.readLine();
      s.broadcast(in);
      if (in.equals("exit")) {
        s.stop(1000);
        break;
      }
    }
  }
}
