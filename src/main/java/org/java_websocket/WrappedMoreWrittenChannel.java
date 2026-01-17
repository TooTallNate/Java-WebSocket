package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface WrappedMoreWrittenChannel extends ByteChannel {
    /**
     * Gets called when {@link #isNeedWrite()} ()} requires a additional rite
     *
     * @throws IOException may be thrown due to an error while writing
     */
    void writeMore() throws IOException;
}