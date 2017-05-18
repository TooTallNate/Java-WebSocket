package org.java_websocket.drafts;

/**
 * Implementation for the RFC 6455 websocket protocol
 * This is the recommended class for your websocket connection
 */
public class Draft_6455 extends Draft_17 {

    @Override
    public Draft copyInstance() {
        return new Draft_6455();
    }
}
