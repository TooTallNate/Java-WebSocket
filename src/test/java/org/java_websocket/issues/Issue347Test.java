package org.java_websocket.issues;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.java_websocket.util.ThreadCheck;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// this tests whether the client gets properly closed while it is connecting. the method
// WebSocketClient.closeConnection(...) is called shortly after WebSocketClient.connect();
// the client is using a socket that takes at least 100ms to connect. then we check for zombies.

@RunWith(Parameterized.class)
public class Issue347Test {

    private static final int NUMBER_OF_TESTS = 20;
    private static final int DELAY_MULTIPLIER = 10;

    private static WebSocketServer server;
    private static int port;

    @Parameterized.Parameter
    public int delay;

    @Rule
    public ThreadCheck zombies = new ThreadCheck();

    @BeforeClass
    public static void startServer() throws Exception {
        port = SocketUtil.getAvailablePort();
        server = new WebSocketServer(new InetSocketAddress(port) , 16) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {}

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

            @Override
            public void onMessage(WebSocket conn, String message) {}

            @Override
            public void onError(WebSocket conn, Exception ex) {}

            @Override
            public void onStart() {}
        };
        server.setConnectionLostTimeout(0);
        server.start();
    }

    @AfterClass
    public static void stopServer() throws InterruptedException, IOException {
        server.stop();
    }

    @Test(timeout = 5000)
    public void runTestScenario() throws Exception {
        final WebSocketClient client = new WebSocketClient( new URI("ws://localhost:" + port)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {}

            @Override
            public void onMessage( String message ) {}

            @Override
            public void onClose( int code, String reason, boolean remote ) {}

            @Override
            public void onError( Exception ex ) {}
        };
        client.setSocket(new SlowSocket());
        client.connect();
        Thread.sleep(delay);
        client.closeConnection(1000, "foo");
    }

    @Parameterized.Parameters
    public static Collection<Integer[]> data() {
        List<Integer[]> ret = new ArrayList<Integer[]>(NUMBER_OF_TESTS);
        for (int i = 0; i < NUMBER_OF_TESTS; i++) ret.add(new Integer[]{i * DELAY_MULTIPLIER});
        return ret;
    }

    private static class SlowSocket extends Socket {
        @Override public void connect(SocketAddress socketAddress, int i) throws IOException {
            sleep(100);
            super.connect(socketAddress, i);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
