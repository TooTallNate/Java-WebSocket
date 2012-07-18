// @formatter:off

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.SSLSocketChannel;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/*
 * Create the appropriate websocket server.
 */
public class SSLServer implements WebSocketServer.WebSocketServerFactory
{
    private static final String STORETYPE = "JKS";
    private static final String KEYSTORE = "keystore.jks";
    private static final String STOREPASSWORD = "storepassword";
    private static final String KEYPASSWORD = "keypassword";
    
    public static void main(String[] args) throws Exception
    {
		WebSocket.DEBUG = true;
	new SSLServer();
    }

    private SSLContext sslContext;
    
    void loadFromFile() throws Exception
    {
	// load up the key store
	KeyStore ks = KeyStore.getInstance(STORETYPE);
	File kf = new File(KEYSTORE);
	ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());
	
	KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	kmf.init(ks, KEYPASSWORD.toCharArray());
	TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	tmf.init(ks);
	
	sslContext = SSLContext.getInstance("TLS");
	sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    }

    /*
     * Keystore with certificate created like so (in JKS format):
     *
     keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
    */
    SSLServer() throws Exception
    {
	sslContext = null;
	loadFromFile();
	
	// create the web socket server
		WebSocketSource wsgateway = new WebSocketSource( 8887, InetAddress.getByName( "localhost" ) );
	wsgateway.setWebSocketFactory(this);
	wsgateway.start();
    }

    @Override
	public WebSocketImpl createWebSocket( WebSocketAdapter a, Draft d, Socket c ) {

		return new WebSocketImpl( a, d, c );
    }
    
    @Override
	public WebSocketImpl createWebSocket( WebSocketAdapter a, List<Draft> d, Socket s ) {
	if(sslContext != null) try{
		SSLEngine e = sslContext.createSSLEngine();
		e.setUseClientMode(false);
				return new WebSocketImpl( a, d, s );
			} catch ( Exception e1 ) {
			}
		return new WebSocketImpl( a, d, s );
    }
    
	@Override
	public ByteChannel wrapChannel( SocketChannel c ) throws IOException {
		if( sslContext == null )
			throw new IllegalArgumentException( "sslContext not initialized");
		SSLEngine e = sslContext.createSSLEngine();
		e.setUseClientMode( false );
		return new SSLSocketChannel( c, e );
	}

    class WebSocketSource extends WebSocketServer
    {
	private WebSocket handle;
	WebSocketSource(int port, InetAddress addr)
	{
	    super(new InetSocketAddress(addr, port));
	    handle = null;
	}

	@Override
	    public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) 
	{
	    System.err.println("---------------------------->Closed");
	    if(arg0 == handle) handle = null;
	}

	@Override
	    public void onError(WebSocket arg0, Exception arg1) {
	    // TODO Auto-generated method stub
	}

	@Override
	    public void onMessage(WebSocket arg0, String arg1)
	{
	    if(arg0 != handle){
		arg0.close(org.java_websocket.framing.CloseFrame.NORMAL);
		return;
	    }
	    
	    System.out.println("--------->["+arg1+"]");
	}

	@Override
	    public void onOpen(WebSocket arg0, ClientHandshake arg1)
	{
	    // nothing to see just yet
	    if(handle == null){
		handle = arg0;
	    }else if(handle != arg0){
		arg0.close(org.java_websocket.framing.CloseFrame.NORMAL);
	    }
	}
		
	void done()
	{
	    if(handle != null) handle.close(org.java_websocket.framing.CloseFrame.NORMAL);
	}
    }
}

//@formatter:on
