package org.java_websocket;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import org.java_websocket.drafts.Draft;

/**
 * Interface to encapsulate the required methods for a websocket factory
 */
public interface WebSocketServerFactoryClosing extends WebSocketFactory {
    /**
     * Allows to shutdown the websocket factory for a clean shutdown
     */
    void close();
}