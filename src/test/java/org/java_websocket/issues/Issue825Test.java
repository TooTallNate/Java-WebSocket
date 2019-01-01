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
import org.java_websocket.util.SocketUtil;
import org.junit.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
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

public class Issue825Test {
    private CountDownLatch countClientDownLatch = new CountDownLatch(3);
    private CountDownLatch countServerDownLatch = new CountDownLatch(1);

    @Test(timeout = 15000)
    public void testIssue() throws IOException, URISyntaxException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, InterruptedException {
        int port = SocketUtil.getAvailablePort();
        final WebSocketClient webSocket = new WebSocketClient(new URI("wss://localhost:" + port)) {
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
        };
        WebSocketServer server = new MyWebSocketServer(port, countServerDownLatch, countClientDownLatch);

        // load up the key store
        String STORETYPE = "JKS";
        String KEYSTORE = String.format("src%1$stest%1$1sjava%1$1sorg%1$1sjava_websocket%1$1skeystore.jks", File.separator);
        String STOREPASSWORD = "storepassword";
        String KEYPASSWORD = "keypassword";

        KeyStore ks = KeyStore.getInstance(STORETYPE);
        File kf = new File(KEYSTORE);
        ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, KEYPASSWORD.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        webSocket.setSocketFactory(sslContext.getSocketFactory());
        server.start();
        countServerDownLatch.await();
        webSocket.connectBlocking();
        webSocket.send( "hi" );
        Thread.sleep( 10 );
        webSocket.closeBlocking();

        //Disconnect manually and reconnect blocking
        webSocket.reconnectBlocking();
        webSocket.send( "it's" );
        Thread.sleep( 10000 );
        webSocket.closeBlocking();
        //Disconnect manually and reconnect
        webSocket.reconnect();
        Thread.sleep( 100 );
        webSocket.send( "me" );
        Thread.sleep( 100 );
        webSocket.closeBlocking();
        countClientDownLatch.await();
    }


    private static class MyWebSocketServer extends WebSocketServer {
        private final CountDownLatch countServerDownLatch;
        private final CountDownLatch countClientDownLatch;


        public MyWebSocketServer(int port, CountDownLatch serverDownLatch, CountDownLatch countClientDownLatch) {
            super(new InetSocketAddress(port));
            this.countServerDownLatch = serverDownLatch;
            this.countClientDownLatch = countClientDownLatch;
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            countClientDownLatch.countDown();
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
