/*
 * Copyright (c) 2010-2020 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.CustomSSLWebSocketServerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
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
    ChatServer chatserver = new ChatServer(
        8887); // Firefox does allow multible ssl connection only via port 443 //tested on FF16

    // load up the key store
    String STORETYPE = "JKS";
    String KEYSTORE = Paths.get("src", "test", "java", "org", "java_websocket", "keystore.jks")
        .toString();
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
    List<String> ciphers = new ArrayList<String>(Arrays.asList(engine.getEnabledCipherSuites()));
    ciphers.remove("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
    List<String> protocols = new ArrayList<String>(Arrays.asList(engine.getEnabledProtocols()));
    protocols.remove("SSLv3");
    CustomSSLWebSocketServerFactory factory = new CustomSSLWebSocketServerFactory(sslContext,
        protocols.toArray(new String[]{}), ciphers.toArray(new String[]{}));

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
