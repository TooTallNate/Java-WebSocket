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
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SSLContextUtil;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class Issue890Test {


    @Test(timeout = 4000)
    public void testWithSSLSession() throws IOException, URISyntaxException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, InterruptedException {
        int port = SocketUtil.getAvailablePort();
        final CountDownLatch countServerDownLatch = new CountDownLatch(1);
        final WebSocketClient webSocket = new WebSocketClient(new URI("wss://localhost:" + port)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                countServerDownLatch.countDown();
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
        TestResult testResult = new TestResult();
        WebSocketServer server = new MyWebSocketServer(port, testResult, countServerDownLatch);
        SSLContext sslContext = SSLContextUtil.getContext();

        server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        webSocket.setSocketFactory(sslContext.getSocketFactory());
        server.start();
        countServerDownLatch.await();
        webSocket.connectBlocking();
        assertTrue(testResult.hasSSLSupport);
        assertNotNull(testResult.sslSession);
    }

    @Test(timeout = 4000)
    public void testWithOutSSLSession() throws IOException, URISyntaxException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, InterruptedException {
        int port = SocketUtil.getAvailablePort();
        final CountDownLatch countServerDownLatch = new CountDownLatch(1);
        final WebSocketClient webSocket = new WebSocketClient(new URI("ws://localhost:" + port)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                countServerDownLatch.countDown();
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
        TestResult testResult = new TestResult();
        WebSocketServer server = new MyWebSocketServer(port, testResult, countServerDownLatch);
        server.start();
        countServerDownLatch.await();
        webSocket.connectBlocking();
        assertFalse(testResult.hasSSLSupport);
        assertNull(testResult.sslSession);
    }


    private static class MyWebSocketServer extends WebSocketServer {

        private final TestResult testResult;
        private final CountDownLatch countServerDownLatch;

        public MyWebSocketServer(int port, TestResult testResult, CountDownLatch countServerDownLatch) {
            super(new InetSocketAddress(port));
            this.testResult = testResult;
            this.countServerDownLatch = countServerDownLatch;
        }
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            testResult.hasSSLSupport = conn.hasSSLSupport();
            try {
                testResult.sslSession = conn.getSSLSession();
            } catch (IllegalArgumentException e){
                // Ignore
            }
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
            countServerDownLatch.countDown();
        }
    }

    private class TestResult {
        public SSLSession sslSession = null;

        public boolean hasSSLSupport = false;
    }
}
