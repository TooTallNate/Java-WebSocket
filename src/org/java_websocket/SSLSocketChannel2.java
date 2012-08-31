/**
 * Copyright (C) 2003 Alexander Kout
 * Originally from the jFxp project (http://jfxp.sourceforge.net/).
 * Copied with permission June 11, 2012 by Femi Omojola (fomojola@ideasynthesis.com).
 */
package org.java_websocket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Implements the relevant portions of the SocketChannel interface with the SSLEngine wrapper.
 */
public class SSLSocketChannel2 implements ByteChannel, WrappedByteChannel {
	private static ByteBuffer emptybuffer = ByteBuffer.allocate( 0 );

	private ExecutorService exec;

	/** raw payload incomming */
	private ByteBuffer inData;
	/** encrypted data outgoing */
	private ByteBuffer outCrypt;
	/** encrypted data incoming */
	private ByteBuffer inCrypt;

	private SocketChannel sc;
	private SelectionKey key;

	private SSLEngineResult res;
	private SSLEngine sslEngine;

	public SSLSocketChannel2( SelectionKey key , SSLEngine sslEngine , ExecutorService exec ) throws IOException {
		this.sc = (SocketChannel) key.channel();
		this.key = key;
		this.sslEngine = sslEngine;
		this.exec = exec;

		this.key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );

		sslEngine.setEnableSessionCreation( true );
		SSLSession session = sslEngine.getSession();
		createBuffers( session );

		// there is not yet any user data
		inData.flip();
		inCrypt.flip();
		outCrypt.flip();

		sc.write( wrap( emptybuffer ) );

		processHandshake();
	}

	private boolean processHandshake() throws IOException {
		if( res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP ) {
			inCrypt.compact();
			sc.read( inCrypt );
			inCrypt.flip();
			unwrap();
			if( res.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.FINISHED ) {
				sc.write( wrap( emptybuffer ) );
			}
		} else if( res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_WRAP ) {
			sc.write( wrap( emptybuffer ) );
		} else {
			assert ( false );
		}

		return false;
	}

	private synchronized ByteBuffer wrap( ByteBuffer b ) throws SSLException {
		outCrypt.compact();
		res = sslEngine.wrap( b, outCrypt );
		outCrypt.flip();
		return outCrypt;
	}

	private synchronized ByteBuffer unwrap() throws SSLException {
		inData.compact();
		while ( inCrypt.hasRemaining() ) {
			res = sslEngine.unwrap( inCrypt, inData );
			if( res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
				Runnable task;
				while ( ( task = sslEngine.getDelegatedTask() ) != null ) {
					task.run();
				}
			} else if( res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED ) {
				break;
			} else if( res.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
				break;
			} else if( res.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW ) {
				throw new RuntimeException( "destenation buffer to small" );
			}
		}
		inData.flip();
		return inData;
	}

	protected void createBuffers( SSLSession session ) {
		int appBufferMax = session.getApplicationBufferSize();
		int netBufferMax = session.getPacketBufferSize();

		inData = ByteBuffer.allocate( appBufferMax );
		outCrypt = ByteBuffer.allocate( netBufferMax );
		inCrypt = ByteBuffer.allocate( netBufferMax );
	}

	public int write( ByteBuffer src ) throws IOException {
		if( !isHandShakeComplete() ) {
			processHandshake();
			return 0;
		} else {
			return sc.write( wrap( src ) );
		}
	}

	public int read( ByteBuffer dst ) throws IOException {
		if( !isHandShakeComplete() ) {
			processHandshake();
			return 0;
		}
		int amount = 0, limit;
		// test if there was a buffer overflow in dst
		if( inData.hasRemaining() ) {
			return transfereTo( inData, dst );
		}
		// test if some bytes left from last read (e.g. BUFFER_UNDERFLOW)
		if( inCrypt.hasRemaining() ) {
			unwrap();
			amount += transfereTo( inData, dst );
		}
		if( !inCrypt.hasRemaining() )
			inCrypt.clear();
		else
			inCrypt.compact();

		if( sc.read( inCrypt ) == -1 ) {
			return -1;
		}
		inCrypt.flip();
		unwrap();
		amount += transfereTo( inData, dst );
		return amount;

	}

	public boolean isConnected() {
		return sc.isConnected();
	}

	public void close() throws IOException {
		sslEngine.closeOutbound();
		sslEngine.getSession().invalidate();
		int wr = sc.write( wrap( emptybuffer ) );
		sc.close();
	}

	private boolean isHandShakeComplete() {
		HandshakeStatus status = res.getHandshakeStatus();
		return status == SSLEngineResult.HandshakeStatus.FINISHED || status == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
	}

	public SelectableChannel configureBlocking( boolean b ) throws IOException {
		return sc.configureBlocking( b );
	}

	public boolean connect( SocketAddress remote ) throws IOException {
		return sc.connect( remote );
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

	@Override
	public boolean isOpen() {
		return sc.isOpen();
	}

	@Override
	public boolean isNeedWrite() {
		return outCrypt.hasRemaining() || !isHandShakeComplete();
	}

	@Override
	public void writeMore() throws IOException {
		write( outCrypt );
	}

	@Override
	public boolean isNeedRead() {
		return inData.hasRemaining();
	}

	@Override
	public int readMore( ByteBuffer dst ) {
		return transfereTo( inData, dst );
	}

	private int transfereTo( ByteBuffer from, ByteBuffer to ) {
		int fremain = from.remaining();
		int toremain = to.remaining();
		if( fremain > toremain ) {
			// FIXME there should be a more efficient transfer method
			int limit = Math.min( fremain, toremain );
			for( int i = 0 ; i < limit ; i++ ) {
				to.put( from.get() );
			}
			return limit;
		} else {
			to.put( from );
			return fremain;
		}

	}

}