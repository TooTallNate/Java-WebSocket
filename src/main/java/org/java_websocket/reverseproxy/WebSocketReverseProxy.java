package org.java_websocket.reverseproxy;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.logging.Logger;

/**
 *                       -----------------------------
 *     WS CLIENT <---->  || serverSide - clientSide || <----> WS SERVER
 *                       -----------------------------
 */
public class WebSocketReverseProxy implements Runnable {

    private static WebSocketReverseProxyClientSide clientSide;
    private static WebSocketReverseProxyServerSide serverSide;

    private static Logger logger = Logger.getLogger(WebSocketReverseProxy.class.getName());

    private String wsUrl;

    private static final int SERVER_SIDE_PORT = 1234;
    private Draft_6455 draft;

    private Thread selectorThread;

    public void start() {
        if (selectorThread != null) {
            throw new IllegalStateException(getClass().getName() + " can only be started once.");
        }
        new Thread(this).start();
        selectorThread = Thread.currentThread();
    }

    public WebSocketReverseProxy(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    private void initServerSide() {
        Protocol protocol = new Protocol("binary");
        DefaultExtension defaultExtension = new DefaultExtension();
        draft = new Draft_6455(Collections.<IExtension>singletonList(defaultExtension), Collections.<IProtocol>singletonList(protocol));
        serverSide = new WebSocketReverseProxyServerSide(SERVER_SIDE_PORT, this, Collections.<Draft>singletonList(draft));
        serverSide.start();
        logger.info("[REVERSE-PROXY] Server side started on port " + SERVER_SIDE_PORT);
    }

    @Override
    public void run() {
        initServerSide();
        boolean interrupted = false;
        while (!interrupted) {
            interrupted = selectorThread.isInterrupted();
        }
    }

    public void initClientSide() throws URISyntaxException {
        URI uri = new URI(wsUrl);
        clientSide = new WebSocketReverseProxyClientSide(uri, this, draft);
        clientSide.connect();
        logger.info("[REVERSE-PROXY] Client side connected to " + wsUrl);
    }

    public void proxyMsgServerToClientSide(ByteBuffer msg) {
        try {
            if (clientSide == null) {
                initClientSide();
            }
            clientSide.receiveProxiedMsg(msg);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void proxyMsgClientToServerSide(ByteBuffer msg) {
        try {
            serverSide.receiveProxiedMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
