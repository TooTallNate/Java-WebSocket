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

package org.java_websocket.issues;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertArrayEquals;

public class Issue941Test {

    private CountDownLatch pingLatch = new CountDownLatch(1);
    private CountDownLatch pongLatch = new CountDownLatch(1);
    private byte[] pingBuffer, receivedPingBuffer, pongBuffer;

    @Test
    public void testIssue() throws Exception {
        int port = SocketUtil.getAvailablePort();
        WebSocketClient client = new WebSocketClient(new URI( "ws://localhost:" + port)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) { }
            @Override
            public void onMessage(String message) { }
            @Override
            public void onClose(int code, String reason, boolean remote) { }
            @Override
            public void onError(Exception ex) { }
            @Override
            public PingFrame onPreparePing(WebSocket conn) {
                PingFrame frame = new PingFrame();
                pingBuffer = new byte[]{1,2,3,4,5,6,7,8,9,10};
                frame.setPayload(ByteBuffer.wrap(pingBuffer));
                return frame;
            }
            @Override
            public void onWebsocketPong(WebSocket conn, Framedata f) {
                pongBuffer = f.getPayloadData().array();
                pongLatch.countDown();
            }
        };

        WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) { }
            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) { }
            @Override
            public void onMessage(WebSocket conn, String message) { }
            @Override
            public void onError(WebSocket conn, Exception ex) { }
            @Override
            public void onStart() { }
            @Override
            public void onWebsocketPing(WebSocket conn, Framedata f) {
                receivedPingBuffer = f.getPayloadData().array();
                super.onWebsocketPing(conn, f);
                pingLatch.countDown();
            }
        };

        server.start();
        client.connectBlocking();
        client.setConnectionLostTimeout(1);
        pingLatch.await();
        assertArrayEquals(pingBuffer, receivedPingBuffer);
        pongLatch.await();
        assertArrayEquals(pingBuffer, pongBuffer);
    }
}
