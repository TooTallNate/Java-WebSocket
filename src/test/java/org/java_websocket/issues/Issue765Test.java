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
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class Issue765Test {

    boolean isClosedCalled = false;
    @Test
    public void testIssue() {
        WebSocketServer webSocketServer = new MyWebSocketServer();
        webSocketServer.setWebSocketFactory(new LocalWebSocketFactory());
        Assert.assertFalse("Close should not have been called yet",isClosedCalled);
        webSocketServer.setWebSocketFactory(new LocalWebSocketFactory());
        Assert.assertTrue("Close has been called", isClosedCalled);
    }

    private static class MyWebSocketServer extends WebSocketServer {
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
    }

    private class LocalWebSocketFactory implements WebSocketServerFactory {
        @Override
        public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d) {
            return null;
        }

        @Override
        public WebSocketImpl createWebSocket(WebSocketAdapter a, List<Draft> drafts) {
            return null;
        }

        @Override
        public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key) throws IOException {
            return null;
        }

        @Override
        public void close() {
            isClosedCalled = true;
        }
    }
}
