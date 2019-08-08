package org.java_websocket.issues;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

public class Issue834Test {

    @Test(timeout = 1000)
    public void testNoNewThreads() throws IOException {

        Set<Thread> threadSet1 = Thread.getAllStackTraces().keySet();

        new WebSocketServer(new InetSocketAddress(SocketUtil.getAvailablePort())) {
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
        };

        Set<Thread> threadSet2 = Thread.getAllStackTraces().keySet();

        //checks that no threads are started in the constructor
        Assert.assertEquals(threadSet1, threadSet2);

    }

}
