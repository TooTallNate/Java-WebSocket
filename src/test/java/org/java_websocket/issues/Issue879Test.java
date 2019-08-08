/*
 * Copyright (c) 2010-2019 Nathan Rajlich
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

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class Issue879Test {

    private static final int NUMBER_OF_TESTS = 20;

    @Parameterized.Parameter
    public int numberOfConnections;


    @Test(timeout= 10000)
    public void QuickStopTest() throws IOException, InterruptedException, URISyntaxException {
        final boolean[] wasBindException = {false};
        final boolean[] wasConcurrentException = new boolean[1];
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        class SimpleServer extends WebSocketServer {
            public SimpleServer(InetSocketAddress address) {
                super(address);
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
            }

            @Override
            public void onMessage(WebSocket conn, ByteBuffer message) {
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                if (ex instanceof BindException) {
                    wasBindException[0] = true;
                }
                if (ex instanceof ConcurrentModificationException) {
                    wasConcurrentException[0] = true;
                }
            }

            @Override
            public void onStart() {
                countDownLatch.countDown();
            }
        }
        int port = SocketUtil.getAvailablePort();
        SimpleServer serverA = new SimpleServer(new InetSocketAddress( port));
        SimpleServer serverB = new SimpleServer(new InetSocketAddress( port));
        serverA.start();
        countDownLatch.await();
        List<WebSocketClient> clients = startNewConnections(numberOfConnections, port);
        Thread.sleep(100);
        int numberOfConnected = 0;
        for (WebSocketClient client : clients) {
            if (client.isOpen())
                numberOfConnected++;
        }
        // Number will differ since we use connect instead of connectBlocking
        // System.out.println(numberOfConnected + " " + numberOfConnections);

        serverA.stop();
        serverB.start();
        clients.clear();
        assertFalse("There was a BindException", wasBindException[0]);
        assertFalse("There was a ConcurrentModificationException", wasConcurrentException[0]);
    }

    @Parameterized.Parameters
    public static Collection<Integer[]> data() {
        List<Integer[]> ret = new ArrayList<Integer[]>(NUMBER_OF_TESTS);
        for (int i = 0; i < NUMBER_OF_TESTS; i++) ret.add(new Integer[]{25+ i*25});
        return ret;
    }

    private List<WebSocketClient> startNewConnections(int numberOfConnections, int port) throws URISyntaxException, InterruptedException {
        List<WebSocketClient> clients = new ArrayList<WebSocketClient>(numberOfConnections);
        for (int i = 0; i < numberOfConnections; i++) {
            WebSocketClient client = new SimpleClient(new URI("ws://localhost:" + port));
            client.connect();
            Thread.sleep(1);
            clients.add(client);
        }
        return clients;
    }
    class SimpleClient extends WebSocketClient {

        public SimpleClient(URI serverUri) {
            super(serverUri);
        }

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

        }
    }
}
