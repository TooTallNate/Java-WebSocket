import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.CustomSSLWebSocketServerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example for using the CustomSSLWebSocketServerFactory to allow just specific cipher suites
 */
public class SSLServerCustomWebsocketFactoryExample {

    /*
     * Keystore with certificate created like so (in JKS format):
     *
     *keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
     */
    public static void main(String[] args) throws Exception {
        WebSocketImpl.DEBUG = true;

        ChatServer chatserver = new ChatServer(8887); // Firefox does allow multible ssl connection only via port 443 //tested on FF16

        // load up the key store
        String STORETYPE = "JKS";
        String KEYSTORE = "keystore.jks";
        String STOREPASSWORD = "storepassword";
        String KEYPASSWORD = "keypassword";

        KeyStore ks = KeyStore.getInstance(STORETYPE);
        File kf = new File(KEYSTORE);
        ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, KEYPASSWORD.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        //Lets remove some ciphers and protocols
        SSLEngine engine = sslContext.createSSLEngine();
        List<String> ciphers = new ArrayList<String>( Arrays.asList(engine.getEnabledCipherSuites()));
        ciphers.remove("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        List<String> protocols = new ArrayList<String>( Arrays.asList(engine.getEnabledProtocols()));
        protocols.remove("SSLv3");
        CustomSSLWebSocketServerFactory factory = new CustomSSLWebSocketServerFactory(sslContext, protocols.toArray(new String[]{}), ciphers.toArray(new String[]{}));

        // Different example just using specific ciphers and protocols
        /*
        String[] enabledProtocols = {"TLSv1.2"};
		String[] enabledCipherSuites = {"TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"};
        CustomSSLWebSocketServerFactory factory = new CustomSSLWebSocketServerFactory(sslContext, enabledProtocols,enabledCipherSuites);
        */
        chatserver.setWebSocketFactory(factory);

        chatserver.start();

    }
}
