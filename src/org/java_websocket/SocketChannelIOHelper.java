package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.java_websocket.drafts.Draft;

public class SocketChannelIOHelper {

	public static boolean read( final ByteBuffer buf, WebSocketImpl ws, SocketChannel channel ) throws IOException {
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

	public static boolean batch( WebSocketImpl ws, SocketChannel sockchannel ) throws IOException {
		ByteBuffer buffer = ws.outQueue.peek();
		while ( buffer != null ) {
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
