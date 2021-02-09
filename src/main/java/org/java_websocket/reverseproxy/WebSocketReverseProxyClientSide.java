package org.java_websocket.reverseproxy;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

public class WebSocketReverseProxyClientSide extends WebSocketClient {

    private static Logger logger = Logger.getLogger(WebSocketReverseProxyClientSide.class.getName());
    private WebSocketReverseProxy reverseProxy;

    private void acceptAllCerts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory factory = sc.getSocketFactory();
            this.setSocketFactory(factory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebSocketReverseProxyClientSide(URI wsEsxiUri, WebSocketReverseProxy reverseProxy, Draft_6455 draft) {
        super(wsEsxiUri, draft);
        this.reverseProxy = reverseProxy;
        acceptAllCerts();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.fine("[REVERSE-PROXY] [CLIENT SIDE] Open connection");
    }

    @Override
    public void onMessage(String message) {
        logger.fine("[REVERSE-PROXY] [CLIENT SIDE] Message: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.fine("[REVERSE-PROXY] [CLIENT SIDE] Close connection: " + reason + " " + code + " " + remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.info("[REVERSE-PROXY] [CLIENT SIDE] Error: " + ex.getLocalizedMessage());
        ex.printStackTrace();
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        logger.finer("[REVERSE-PROXY] [CLIENT SIDE] Received frame, proxying to server");
        this.reverseProxy.proxyMsgClientToServerSide(bytes);
    }

    public void receiveProxiedMsg(ByteBuffer msg) {
        logger.finer("[REVERSE-PROXY] [CLIENT SIDE] Received proxied msg, sending to ESX host");
        this.getConnection().send(msg);
    }
}
