package org.java_websocket.util;
import java.io.IOException;
import java.net.ServerSocket;

public class SocketUtil {
	public static int getAvailablePort() throws IOException {
		ServerSocket srv = null;
		try {
			srv = new ServerSocket( 0 );
			return srv.getLocalPort();
		} finally {
			if( srv != null ) {
				srv.close();
			}
		}
	}
}
