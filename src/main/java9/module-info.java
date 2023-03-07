/**
 * This module implements a barebones WebSocket server and client.
 */
module org.java_websocket {
    requires transitive org.slf4j;

    exports org.java_websocket;
    exports org.java_websocket.client;
    exports org.java_websocket.drafts;
    exports org.java_websocket.enums;
    exports org.java_websocket.exceptions;
    exports org.java_websocket.extensions;
    exports org.java_websocket.extensions.permessage_deflate;
    exports org.java_websocket.framing;
    exports org.java_websocket.handshake;
    exports org.java_websocket.interfaces;
    exports org.java_websocket.protocols;
    exports org.java_websocket.server;
}
