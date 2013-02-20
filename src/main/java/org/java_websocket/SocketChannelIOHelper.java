package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public class SocketChannelIOHelper {

	public static boolean read( final ByteBuffer buf, WebSocketImpl ws, ByteChannel channel ) throws IOException {
		buf.clear();
		int read = channel.read( buf );
		buf.flip();

		if( read == -1 ) {
			ws.eot();
			return false;
		}
		return read != 0;
	}

	/**
	 * @see WrappedByteChannel#readMore(ByteBuffer)
	 * @return returns whether there is more data left which can be obtained via {@link #readMore(ByteBuffer, WebSocketImpl, WrappedByteChannel)}
	 **/
	public static boolean readMore( final ByteBuffer buf, WebSocketImpl ws, WrappedByteChannel channel ) throws IOException {
		buf.clear();
		int read = channel.readMore( buf );
		buf.flip();

		if( read == -1 ) {
			ws.eot();
			return false;
		}
		return channel.isNeedRead();
	}

	/** Returns whether the whole outQueue has been flushed */
	public static boolean batch( WebSocketImpl ws, ByteChannel sockchannel ) throws IOException {
		ByteBuffer buffer = ws.outQueue.peek();
		WrappedByteChannel c = null;

		if( buffer == null ) {
			if( sockchannel instanceof WrappedByteChannel ) {
				c = (WrappedByteChannel) sockchannel;
				if( c.isNeedWrite() ) {
					c.writeMore();
				}
			}
		} else {
			do {// FIXME writing as much as possible is unfair!!
				/*int written = */sockchannel.write( buffer );
				if( buffer.remaining() > 0 ) {
					return false;
				} else {
					ws.outQueue.poll(); // Buffer finished. Remove it.
					buffer = ws.outQueue.peek();
				}
			} while ( buffer != null );
		}

		if( ws.outQueue.isEmpty() && ws.isFlushAndClose() /*&& ( c == null || c.isNeedWrite() )*/) {
			synchronized ( ws ) {
				ws.closeConnection();
			}
		}
		return c != null ? !( (WrappedByteChannel) sockchannel ).isNeedWrite() : true;
	}

	public static void writeBlocking( WebSocketImpl ws, ByteChannel channel ) throws InterruptedException , IOException {
		assert ( channel instanceof AbstractSelectableChannel == true ? ( (AbstractSelectableChannel) channel ).isBlocking() : true );
		assert ( channel instanceof WrappedByteChannel == true ? ( (WrappedByteChannel) channel ).isBlocking() : true );

		ByteBuffer buf = ws.outQueue.take();
		while ( buf.hasRemaining() )
			channel.write( buf );
	}

}
