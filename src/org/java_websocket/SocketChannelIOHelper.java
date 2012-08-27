package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class SocketChannelIOHelper {

	public static boolean read( final ByteBuffer buf, WebSocketImpl ws, ByteChannel channel ) throws IOException {
		buf.clear();
		int read = channel.read( buf );
		buf.flip();

		if( read == -1 ) {
			ws.eot( null );
			return false;
		}
		return read != 0;
	}

	/** Returns whether the whole outQueue has been flushed */
	public static boolean batch( WebSocketImpl ws, ByteChannel sockchannel ) throws IOException {
		ByteBuffer buffer = ws.outQueue.peek();

		if( buffer == null ) {
			if( sockchannel instanceof WrappedByteChannel ) {
				WrappedByteChannel c = (WrappedByteChannel) sockchannel;
				if( c.isNeedWrite() ) {
					c.writeMore();
					return !c.isNeedWrite();
				}
				return true;
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

		if( ws.isClosed() ) {
			synchronized ( ws ) {
				sockchannel.close();
			}
		}
		return sockchannel instanceof WrappedByteChannel == true ? !( (WrappedByteChannel) sockchannel ).isNeedWrite() : true;
	}

}
