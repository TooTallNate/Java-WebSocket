package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import javax.net.ssl.SSLException;

public interface WrappedByteChannel extends ByteChannel {
	public boolean isNeedWrite();
	public void writeMore() throws IOException;

	/**
	 * returns whether readMore should be called to fetch data which has been decoded but not yet been returned.
	 * 
	 * @see #read(ByteBuffer)
	 * @see #readMore(ByteBuffer)
	 **/
	public boolean isNeedRead();
	/**
	 * This function does not read data from the underlying channel at all. It is just a way to fetch data which has already be received or decoded but was but was not yet returned to the user.
	 * This could be the case when the decoded data did not fit into the buffer the user passed to {@link #read(ByteBuffer)}.
	 **/
	public int readMore( ByteBuffer dst ) throws SSLException;
	public boolean isBlocking();
}
