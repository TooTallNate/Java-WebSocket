package org.java_websocket.reverseproxy;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Logger;

public class WebSocketReverseProxyServerSide extends WebSocketServer {

    private static Logger logger = Logger.getLogger(WebSocketReverseProxyServerSide.class.getName());
    private WebSocketReverseProxy reverseProxy;
    private WebSocket conn;

    public WebSocketReverseProxyServerSide(int port, WebSocketReverseProxy reverseProxy, List<Draft> draftList) {
        super(new InetSocketAddress(port), draftList);
        this.reverseProxy = reverseProxy;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.fine("[REVERSE-PROXY] [SERVER SIDE] Open connection - client connected successfully. Starting client connection");
        this.conn = conn;
        try {
            this.reverseProxy.initClientSide();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.fine("[REVERSE-PROXY] [SERVER SIDE] Close connection: " + reason + " " + code + " " + remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.fine("[REVERSE-PROXY] [SERVER SIDE] Message received: " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        logger.finer("[REVERSE-PROXY] [SERVER SIDE] Proxing message to client");
        this.reverseProxy.proxyMsgServerToClientSide(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.fine("[REVERSE-PROXY] [SERVER SIDE] Error " + ex.getLocalizedMessage());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        logger.fine("[REVERSE-PROXY] [SERVER SIDE] Started");
    }

    public void receiveProxiedMessage(ByteBuffer msg) {
        logger.finer("[REVERSE-PROXY] [SERVER SIDE] Sending msg to client (noVNC)");
        conn.send(msg);
    }
}
