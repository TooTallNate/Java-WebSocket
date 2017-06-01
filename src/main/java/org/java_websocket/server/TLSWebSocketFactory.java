package org.java_websocket.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.java_websocket.SSLSocketChannel2;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;

/**
 * WebSocketFactory that can be configured to only support given TLS
 * protocols and given cipher suites.
 */
public class TLSWebSocketFactory implements WebSocketServer.WebSocketServerFactory {
	protected SSLContext sslcontext;
	protected ExecutorService executor;
	private String[] enabledProtocols;
	private String[] enabledCiphersuites;

	/**
	 * New TLSWebSocketFactory configured to only support given protocols and given cipher suites. 
	 *  
	 * @param sslContext - can not be <code>null</code> 
	 * @param enabledProtocols - only these protocols are enabled, when <code>null</code> default settings will be used.
	 * @param enabledCiphersuites - only these cipher suites are enabled, when <code>null</code> default settings will be used.
	 */
	public TLSWebSocketFactory(SSLContext sslContext, String[] enabledProtocols, String[] enabledCiphersuites) {
		if (sslContext == null) {
			throw new IllegalArgumentException("SSLContext == null");
		}
		this.sslcontext = sslContext;
		this.executor = Executors.newSingleThreadScheduledExecutor();
		this.enabledProtocols = enabledProtocols;
		this.enabledCiphersuites = enabledCiphersuites;
	}

	@Override
	public ByteChannel wrapChannel( SocketChannel channel, SelectionKey key ) throws IOException {
		SSLEngine engine = sslcontext.createSSLEngine();
		engine.setUseClientMode( true );
		if(enabledProtocols != null) {
			engine.setEnabledProtocols(enabledProtocols);
		}
		if(enabledCiphersuites != null) {
			engine.setEnabledCipherSuites(enabledCiphersuites);
		}
		return new SSLSocketChannel2(channel, engine, executor, key);
	}
	
	@Override
	public WebSocketImpl createWebSocket( WebSocketAdapter a, Draft d, Socket c ) {
		return new WebSocketImpl( a, d );
	}

	@Override
	public WebSocketImpl createWebSocket( WebSocketAdapter a, List<Draft> d, Socket s ) {
		return new WebSocketImpl( a, d );
	}

}
