package org.java_websocket.performance;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test improved ping/pongs for slow consumers.
 */
public class SlowConsumerTest {

    private static final Logger log = LoggerFactory.getLogger(SlowConsumerTest.class);

    @Test
    public void testClientStableClientConnectionForSlowConsumer() throws Exception {
        final CountDownLatch countServerStart = new CountDownLatch(1);
        int port = SocketUtil.getAvailablePort();

        AtomicInteger disconnectCounter = new AtomicInteger(0);

        TestServer server = new TestServer(new InetSocketAddress(port), countServerStart, disconnectCounter);
        server.setConnectionLostTimeout(2);
        server.start();
        countServerStart.await(10, TimeUnit.SECONDS);

        URI uri = new URI("ws://localhost:" + port);

        CountDownLatch countClientConnectionDownLatch = new CountDownLatch(1);
        CountDownLatch countReceivedMessagesDownLatch = new CountDownLatch(10);
        WebSocketClient client = new TestClient(uri,
                countClientConnectionDownLatch,
                countReceivedMessagesDownLatch,
                disconnectCounter);
        client.setConnectionLostTimeout(2);
        client.connectBlocking();
        countClientConnectionDownLatch.await(10, TimeUnit.SECONDS);

        // send 10 messages in burst
        server.startSending();

        countReceivedMessagesDownLatch.await(15, TimeUnit.SECONDS);
        Assertions.assertEquals(0, disconnectCounter.get(), "Server/Client disconnected.");
    }


    private static class TestServer extends  WebSocketServer {

        private final CountDownLatch countServerStart;
        private final AtomicInteger disconnectCounter;

        private WebSocket clientConnection;

        public TestServer(InetSocketAddress address, CountDownLatch countServerStart, AtomicInteger disconnectCounter) {
            super(address);
            this.countServerStart = countServerStart;
            this.disconnectCounter = disconnectCounter;
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            clientConnection = conn;
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            log.error("Connection lost, code: {}, reason: {}, remote: {}", code, reason, remote);
            disconnectCounter.incrementAndGet();
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            log.error("Connection lost on error", ex);
            disconnectCounter.incrementAndGet();
        }

        @Override
        public void onStart() {
            countServerStart.countDown();
        }

        // burst send 10 messages
        public void startSending() {
            if (clientConnection == null)
                throw new RuntimeException("No client connected");

            byte[] msg = new byte[1];
            for (int i = 0; i < 10; i++) {
                clientConnection.send(msg);
            }
        }

    }

    static class TestClient extends WebSocketClient {
        private final CountDownLatch onOpenLach;
        private final CountDownLatch messagesReceivedLatch;
        private final AtomicInteger disconnectCounter;

        public TestClient(URI uri, CountDownLatch latch, CountDownLatch messagesReceivedLatch, AtomicInteger disconnectCounter) {
            super(uri);
            onOpenLach = latch;
            this.messagesReceivedLatch = messagesReceivedLatch;
            this.disconnectCounter = disconnectCounter;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            onOpenLach.countDown();
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            try {
                // add some delay to message processing to simulate a slow consumer
                Thread.sleep(1000);
                messagesReceivedLatch.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.error("Connection lost, code: {}, reason: {}, remote: {}", code, reason, remote);
            disconnectCounter.incrementAndGet();
        }

        @Override
        public void onError(Exception ex) {
            log.error("Connection lost on error", ex);
            disconnectCounter.incrementAndGet();
        }
    }

}
