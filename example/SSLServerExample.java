import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.SSLSocketChannel2;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.server.WebSocketServer;

public class SSLServerExample {

	/*
	 * Keystore with certificate created like so (in JKS format):
	 *
	 *keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
	 */
	public static void main( String[] args ) throws Exception {
		WebSocket.DEBUG = true;

		ChatServer chatserver = new ChatServer( 8887 );

		// load up the key store
		String STORETYPE = "JKS";
		String KEYSTORE = "keystore.jks";
		String STOREPASSWORD = "storepassword";
		String KEYPASSWORD = "keypassword";

		KeyStore ks = KeyStore.getInstance( STORETYPE );
		File kf = new File( KEYSTORE );
		ks.load( new FileInputStream( kf ), STOREPASSWORD.toCharArray() );

		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
		kmf.init( ks, KEYPASSWORD.toCharArray() );
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		tmf.init( ks );

		SSLContext sslContext = null;
		sslContext = SSLContext.getInstance( "TLS" );
		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );

		chatserver.setWebSocketFactory( new SSLWebSocketServerFactory( sslContext ) );

		chatserver.start();

	}
}

class SSLWebSocketServerFactory implements WebSocketServer.WebSocketServerFactory {
	private SSLContext sslcontext;
	private ExecutorService exec = Executors.newSingleThreadScheduledExecutor();

	SSLWebSocketServerFactory( SSLContext sslContext ) {
		this.sslcontext = sslContext;
	}

	@Override
	public ByteChannel wrapChannel( SelectionKey c ) throws IOException {
		SSLEngine e = sslcontext.createSSLEngine();
		e.setUseClientMode( false );
		return new SSLSocketChannel2( c, e, exec );
	}

	@Override
	public WebSocketImpl createWebSocket( WebSocketAdapter a, Draft d, Socket c ) {
		return new WebSocketImpl( a, d, c );
	}

	@Override
	public WebSocketImpl createWebSocket( WebSocketAdapter a, List<Draft> d, Socket s ) {
		return new WebSocketImpl( a, d, s );
	}
}
