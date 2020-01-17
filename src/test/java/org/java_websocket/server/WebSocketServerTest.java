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

package org.java_websocket.server;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class WebSocketServerTest {

    @Test
    public void testConstructor() {
        List<Draft> draftCollection = Collections.<Draft>singletonList(new Draft_6455());
        Collection<WebSocket> webSocketCollection = new HashSet<WebSocket>();
        InetSocketAddress inetAddress = new InetSocketAddress(1337);

        try {
            WebSocketServer server = new MyWebSocketServer(null, 1,draftCollection, webSocketCollection );
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            //OK
        }
        try {
            WebSocketServer server = new MyWebSocketServer(inetAddress, 0,draftCollection, webSocketCollection );
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            //OK
        }
        try {
            WebSocketServer server = new MyWebSocketServer(inetAddress, -1,draftCollection, webSocketCollection );
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            //OK
        }
        try {
            WebSocketServer server = new MyWebSocketServer(inetAddress, Integer.MIN_VALUE, draftCollection, webSocketCollection );
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            //OK
        }
        try {
            WebSocketServer server = new MyWebSocketServer(inetAddress, Integer.MIN_VALUE, draftCollection, webSocketCollection );
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            //OK
        }
        try {
            WebSocketServer server = new MyWebSocketServer(inetAddress, 1, draftCollection, null );
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            WebSocketServer server = new MyWebSocketServer(inetAddress, 1, draftCollection, webSocketCollection );
            // OK
        } catch (IllegalArgumentException e) {
            fail("Should not fail");
        }
        try {
            WebSocketServer server = new MyWebSocketServer(inetAddress, 1, null, webSocketCollection );
            // OK
        } catch (IllegalArgumentException e) {
            fail("Should not fail");
        }
    }


    @Test
    public void testGetAddress() throws IOException {
        int port = SocketUtil.getAvailablePort();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        MyWebSocketServer server = new MyWebSocketServer(port);
        assertEquals(inetSocketAddress, server.getAddress());
    }

    @Test
    public void testGetDrafts() {
        List<Draft> draftCollection = Collections.<Draft>singletonList(new Draft_6455());
        Collection<WebSocket> webSocketCollection = new HashSet<WebSocket>();
        InetSocketAddress inetAddress = new InetSocketAddress(1337);
        MyWebSocketServer server = new MyWebSocketServer(inetAddress, 1, draftCollection, webSocketCollection);
        assertEquals(1, server.getDraft().size());
        assertEquals(draftCollection.get(0), server.getDraft().get(0));
    }

    @Test
    public void testGetPort() throws IOException, InterruptedException {
        int port = SocketUtil.getAvailablePort();
        CountDownLatch countServerDownLatch = new CountDownLatch( 1 );
        MyWebSocketServer server = new MyWebSocketServer(port);
        assertEquals(port, server.getPort());
        server = new MyWebSocketServer(0, countServerDownLatch);
        assertEquals(0, server.getPort());
        server.start();
        countServerDownLatch.await();
        assertNotEquals(0, server.getPort());
    }

    @Test
    public void testBroadcast() {
        MyWebSocketServer server = new MyWebSocketServer(1337);
        try {
            server.broadcast((byte[]) null, Collections.<WebSocket>emptyList());
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            // OK
        }
        try {
            server.broadcast((ByteBuffer) null, Collections.<WebSocket>emptyList());
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            // OK
        }
        try {
            server.broadcast((String) null, Collections.<WebSocket>emptyList());
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            // OK
        }
        try {
            server.broadcast(new byte[] {(byte) 0xD0}, null);
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            // OK
        }
        try {
            server.broadcast(ByteBuffer.wrap(new byte[] {(byte) 0xD0}), null);
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            // OK
        }
        try {
            server.broadcast("", null);
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            // OK
        }
        try {
            server.broadcast("", Collections.<WebSocket>emptyList());
            // OK
        } catch (IllegalArgumentException e) {
            fail("Should not fail");
        }
    }
    private static class MyWebSocketServer extends WebSocketServer {
        private CountDownLatch serverLatch = null;

        public MyWebSocketServer(InetSocketAddress address , int decodercount , List<Draft> drafts , Collection<WebSocket> connectionscontainer) {
            super(address, decodercount, drafts, connectionscontainer);
        }
        public MyWebSocketServer(int port, CountDownLatch serverLatch) {
            super(new InetSocketAddress(port));
            this.serverLatch = serverLatch;
        }
        public MyWebSocketServer(int port) {
            this(port, null);
        }

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
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            if (serverLatch != null) {
                serverLatch.countDown();
            }
        }
    }
}

