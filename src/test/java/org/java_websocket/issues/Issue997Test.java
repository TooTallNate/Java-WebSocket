package org.java_websocket.issues;

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


import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SSLContextUtil;
import org.java_websocket.util.SocketUtil;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class Issue997Test {

    @Test(timeout=2000)
    public void test_localServer_ServerLocalhost_Client127_CheckActive() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, URISyntaxException, InterruptedException {
        SSLWebSocketClient client = testIssueWithLocalServer("127.0.0.1", SocketUtil.getAvailablePort(), SSLContextUtil.getLocalhostOnlyContext(), SSLContextUtil.getLocalhostOnlyContext(), "HTTPS");
        assertFalse(client.onOpen);
        assertTrue(client.onSSLError);
    }
    @Test(timeout=2000)
    public void test_localServer_ServerLocalhost_Client127_CheckInactive() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, URISyntaxException, InterruptedException {
        SSLWebSocketClient client = testIssueWithLocalServer("127.0.0.1", SocketUtil.getAvailablePort(), SSLContextUtil.getLocalhostOnlyContext(), SSLContextUtil.getLocalhostOnlyContext(), "");
        assertTrue(client.onOpen);
        assertFalse(client.onSSLError);
    }

    @Test(timeout=2000)
    public void test_localServer_ServerLocalhost_Client127_CheckDefault() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, URISyntaxException, InterruptedException {
        SSLWebSocketClient client = testIssueWithLocalServer("127.0.0.1", SocketUtil.getAvailablePort(), SSLContextUtil.getLocalhostOnlyContext(), SSLContextUtil.getLocalhostOnlyContext(), null);
        assertFalse(client.onOpen);
        assertTrue(client.onSSLError);
    }

    @Test(timeout=2000)
    public void test_localServer_ServerLocalhost_ClientLocalhost_CheckActive() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, URISyntaxException, InterruptedException {
        SSLWebSocketClient client = testIssueWithLocalServer("localhost", SocketUtil.getAvailablePort(), SSLContextUtil.getLocalhostOnlyContext(), SSLContextUtil.getLocalhostOnlyContext(), "HTTPS");
        assertTrue(client.onOpen);
        assertFalse(client.onSSLError);
    }
    @Test(timeout=2000)
    public void test_localServer_ServerLocalhost_ClientLocalhost_CheckInactive() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, URISyntaxException, InterruptedException {
        SSLWebSocketClient client = testIssueWithLocalServer("localhost", SocketUtil.getAvailablePort(), SSLContextUtil.getLocalhostOnlyContext(), SSLContextUtil.getLocalhostOnlyContext(), "");
        assertTrue(client.onOpen);
        assertFalse(client.onSSLError);
    }

    @Test(timeout=2000)
    public void test_localServer_ServerLocalhost_ClientLocalhost_CheckDefault() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, URISyntaxException, InterruptedException {
        SSLWebSocketClient client = testIssueWithLocalServer("localhost", SocketUtil.getAvailablePort(), SSLContextUtil.getLocalhostOnlyContext(), SSLContextUtil.getLocalhostOnlyContext(), null);
        assertTrue(client.onOpen);
        assertFalse(client.onSSLError);
    }


    public SSLWebSocketClient testIssueWithLocalServer(String address, int port, SSLContext serverContext, SSLContext clientContext, String endpointIdentificationAlgorithm) throws IOException, URISyntaxException, InterruptedException {
        CountDownLatch countServerDownLatch = new CountDownLatch(1);
        SSLWebSocketClient client = new SSLWebSocketClient(address, port, endpointIdentificationAlgorithm);
        WebSocketServer server = new SSLWebSocketServer(port, countServerDownLatch);

        server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(serverContext));
        if (clientContext != null) {
            client.setSocketFactory(clientContext.getSocketFactory());
        }
        server.start();
        countServerDownLatch.await();
        client.connectBlocking(1, TimeUnit.SECONDS);
        return client;
    }


    private static class SSLWebSocketClient extends WebSocketClient {
        private final String endpointIdentificationAlgorithm;
        public boolean onSSLError = false;
        public boolean onOpen = false;

        public SSLWebSocketClient(String address, int port, String endpointIdentificationAlgorithm) throws URISyntaxException {
            super(new URI("wss://"+ address + ':' +port));
            this.endpointIdentificationAlgorithm = endpointIdentificationAlgorithm;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            this.onOpen = true;
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
            if (ex instanceof SSLHandshakeException) {
                this.onSSLError = true;
            }
        }

        @Override
        protected void onSetSSLParameters(SSLParameters sslParameters) {
            // Always call super to ensure hostname validation is active by default
            super.onSetSSLParameters(sslParameters);
            if (endpointIdentificationAlgorithm != null) {
                sslParameters.setEndpointIdentificationAlgorithm(endpointIdentificationAlgorithm);
            }
        }

    };


    private static class SSLWebSocketServer extends WebSocketServer {
        private final CountDownLatch countServerDownLatch;


        public SSLWebSocketServer(int port, CountDownLatch countServerDownLatch) {
            super(new InetSocketAddress(port));
            this.countServerDownLatch = countServerDownLatch;
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
            countServerDownLatch.countDown();
        }
    }
}
