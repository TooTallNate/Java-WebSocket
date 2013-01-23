

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

public class SSLServerExample {

	/*
	 * Keystore with certificate created like so (in JKS format):
	 *
	 *keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
	 */
	public static void main( String[] args ) throws Exception {
		WebSocketImpl.DEBUG = true;

		ChatServer chatserver = new ChatServer( 8887 ); // Firefox does allow multible ssl connection only via port 443 //tested on FF16

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

		chatserver.setWebSocketFactory( new DefaultSSLWebSocketServerFactory( sslContext ) );

		chatserver.start();

	}
}
