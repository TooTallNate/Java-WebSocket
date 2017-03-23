package org.java_websocket.exceptions;

/**
 * exception which indicates the websocket is not yet connected (READYSTATE.OPEN)
 */
public class WebsocketNotConnectedException extends RuntimeException {

    /**
     * Serializable
     */
    private static final long serialVersionUID = -785314021592982715L;

    /**
     * constructor for a WebsocketNotConnectedException
     */
    public WebsocketNotConnectedException() {
        super();
    }
}
