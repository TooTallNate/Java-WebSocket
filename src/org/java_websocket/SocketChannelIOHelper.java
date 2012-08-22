package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import org.java_websocket.drafts.Draft;

public class SocketChannelIOHelper {
	
	private static ByteBuffer emptybuffer = ByteBuffer.allocate( 0 );

	public static boolean read( final ByteBuffer buf, WebSocketImpl ws, ByteChannel channel ) throws IOException {
		buf.clear();
		int read = channel.read( buf );
		buf.flip();

		if( read == -1 ) {
			Draft draft = ws.getDraft();
			ws.eot( null );
			return false;
		}
		return read != 0;
	}

	/** Returns whether the whole outQueue has been flushed */
	public static boolean batch( WebSocketImpl ws, ByteChannel sockchannel ) throws IOException {
		ByteBuffer buffer = ws.outQueue.peek();

		if( buffer == null ) {
			return sockchannel.write( emptybuffer ) == 0;
		}

		while ( buffer != null ) {// FIXME writing as much as possible is unfair!!
			/*int written = */sockchannel.write( buffer );
			if( buffer.remaining() > 0 ) {
				return false;
			} else {
				ws.outQueue.poll(); // Buffer finished. Remove it.
				buffer = ws.outQueue.peek();
			}
		}
		if( ws.isClosed() ) {
			synchronized ( ws ) {
				sockchannel.close();
			}
		}
		return true;
	}
}
