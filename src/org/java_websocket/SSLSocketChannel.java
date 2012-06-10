package org.java_websocket;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import javax.net.ssl.*;
import java.io.*;

/**
 * Implements the relevant portions of the SocketChannel interface with the SSLEngine wrapper.
 */
public class SSLSocketChannel
{
    private ByteBuffer clientIn, clientOut, cTOs, sTOc, wbuf;
    private SocketChannel sc;
    private SSLEngineResult res;
    private SSLEngine sslEngine;
    private int SSL;

    public SSLSocketChannel(SocketChannel sc, SSLEngine sslEngine) throws IOException
    {
	this.sc = sc;
	this.sslEngine = sslEngine;
	SSL = 1;
	try {
	    sslEngine.setEnableSessionCreation(true);
	    SSLSession session = sslEngine.getSession();
	    createBuffers(session);
	    // wrap
	    clientOut.clear();
	    sc.write(wrap(clientOut));
	    while (res.getHandshakeStatus() != 
		   SSLEngineResult.HandshakeStatus.FINISHED) {
		if (res.getHandshakeStatus() == 
		    SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
		    // unwrap
		    sTOc.clear();
		    while (sc.read(sTOc) < 1)
			Thread.sleep(20);
		    sTOc.flip();
		    unwrap(sTOc);
		    if (res.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.FINISHED) {
			clientOut.clear();
			sc.write(wrap(clientOut));
		    }
		} else if (res.getHandshakeStatus() == 
			   SSLEngineResult.HandshakeStatus.NEED_WRAP) {
		    // wrap
		    clientOut.clear();
		    sc.write(wrap(clientOut));
		} else {Thread.sleep(1000);}
	    }
	    clientIn.clear();
	    clientIn.flip();
	    SSL = 4;
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    SSL = 0;
	}
    }
	
    private synchronized ByteBuffer wrap(ByteBuffer b) throws SSLException {
	cTOs.clear();
	res = sslEngine.wrap(b, cTOs);
	cTOs.flip();
	return cTOs;
    }
	
    private synchronized ByteBuffer unwrap(ByteBuffer b) throws SSLException {
	clientIn.clear();
	int pos;
	while (b.hasRemaining()) {
	    res = sslEngine.unwrap(b, clientIn);
	    if (res.getHandshakeStatus() == 
		SSLEngineResult.HandshakeStatus.NEED_TASK) {
		// Task
		Runnable task;
		while ((task=sslEngine.getDelegatedTask()) != null)
		    {
			task.run();
		    }
	    } else if (res.getHandshakeStatus() == 
		       SSLEngineResult.HandshakeStatus.FINISHED) {
		return clientIn;
	    } else if (res.getStatus() == 
		       SSLEngineResult.Status.BUFFER_UNDERFLOW) {
		return clientIn;
	    }
	}
	return clientIn;
    }

    private void createBuffers(SSLSession session) {
	
	int appBufferMax = session.getApplicationBufferSize();
	int netBufferMax = session.getPacketBufferSize();
	
	clientIn = ByteBuffer.allocate(65536);
	clientOut = ByteBuffer.allocate(appBufferMax);
	wbuf = ByteBuffer.allocate(65536);
	
	cTOs = ByteBuffer.allocate(netBufferMax);
	sTOc = ByteBuffer.allocate(netBufferMax);
	
    }

    public int write(ByteBuffer src) throws IOException {
	if (SSL == 4) {
	    return sc.write(wrap(src));
	}
	return sc.write(src);
    }
	
    public int read(ByteBuffer dst) throws IOException {
	int amount = 0, limit;
	if (SSL == 4) {
	    // test if there was a buffer overflow in dst
	    if (clientIn.hasRemaining()) {
		limit = Math.min(clientIn.remaining(), dst.remaining());
		for (int i = 0; i < limit; i++) {
		    dst.put(clientIn.get());
		    amount++;
		}
		return amount;
	    }
	    // test if some bytes left from last read (e.g. BUFFER_UNDERFLOW)
	    if (sTOc.hasRemaining()) {
		unwrap(sTOc);
		clientIn.flip();
		limit = Math.min(clientIn.limit(), dst.remaining());
		for (int i = 0; i < limit; i++) {
		    dst.put(clientIn.get());
		    amount++;
		}
		if (res.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW) {
		    sTOc.clear();
		    sTOc.flip();
		    return amount;
		}
	    }
	    if (!sTOc.hasRemaining())
		sTOc.clear();
	    else
		sTOc.compact();
			
	    if (sc.read(sTOc) == -1) {
		sTOc.clear();
		sTOc.flip();
		return -1;
	    }
	    sTOc.flip();
	    unwrap(sTOc);
	    // write in dst
	    clientIn.flip();
	    limit = Math.min(clientIn.limit(), dst.remaining());
	    for (int i = 0; i < limit; i++) {
		dst.put(clientIn.get());
		amount++;
	    }
	    return amount;
	}
	return sc.read(dst);
    }
	
    public boolean isConnected() {
	return sc.isConnected();
    }
	
    public void close() throws IOException {
	if (SSL == 4) {
	    sslEngine.closeOutbound();
	    sslEngine.getSession().invalidate();
	    clientOut.clear();
	    sc.write(wrap(clientOut));
	    sc.close();
	} else
	    sc.close();
    }
	
    public SelectableChannel configureBlocking(boolean b) throws IOException {
	return sc.configureBlocking(b);
    }
	
    public boolean connect(SocketAddress remote) throws IOException {
	return sc.connect(remote);
    }
	
    public boolean finishConnect() throws IOException {
	return sc.finishConnect();
    }
	
    public Socket socket() {
	return sc.socket();
    }
	
    public boolean isInboundDone() {
	return sslEngine.isInboundDone();
    }
}