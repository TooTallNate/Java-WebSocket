package org.java_websocket;

import java.io.IOException;
import java.nio.channels.ByteChannel;

public interface WrappedByteChannel extends ByteChannel {
	public boolean isNeedWrite();
	public void write() throws IOException;
}
