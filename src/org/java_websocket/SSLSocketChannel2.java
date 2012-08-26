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

	public SSLSocketChannel2( SelectionKey key , SSLEngine sslEngine ) throws IOException {
		this.sc = (SocketChannel) key.channel();
		this.key = key;
		this.sslEngine = sslEngine;

		key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );

		sslEngine.setEnableSessionCreation( true );
		SSLSession session = sslEngine.getSession();
		createBuffers( session );

		// there is not yet any user data
		inData.flip();
		inCrypt.flip();
		outCrypt.flip();

		wrap( emptybuffer );

		processHandshake();
	}

	private boolean processHandshake() throws IOException {
		if( res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP ) {
			if( !inCrypt.hasRemaining() )
				inCrypt.clear();
			sc.read( inCrypt );
			inCrypt.flip();
			unwrap();
			if( res.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.FINISHED ) {
				// if( !outData.hasRemaining() )
				// outData.clear();
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
		if( !outCrypt.hasRemaining() )
			outCrypt.clear();
		res = sslEngine.wrap( b, outCrypt );
		outCrypt.flip();
		return outCrypt;
	}

	private synchronized ByteBuffer unwrap() throws SSLException {
		inData.compact();
		while ( inCrypt.hasRemaining() ) {
			res = sslEngine.unwrap( inCrypt, inData );
			if( res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
				// Task
				Runnable task;
				while ( ( task = sslEngine.getDelegatedTask() ) != null ) {
					task.run();
				}
			} else if( res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED ) {
				break;
			} else if( res.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
				break;
			} else if( res.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW ) {
				assert ( false );
			}
		}
		inData.flip();
		return inData;
	}

	private void createBuffers( SSLSession session ) {
		int appBufferMax = session.getApplicationBufferSize();
		int netBufferMax = session.getPacketBufferSize();

		inData = ByteBuffer.allocate( 65536 );

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
			limit = Math.min( inData.remaining(), dst.remaining() );
			for( int i = 0 ; i < limit ; i++ ) {
				dst.put( inData.get() );
				amount++;
			}
			return amount;
		}
		// test if some bytes left from last read (e.g. BUFFER_UNDERFLOW)
		if( inCrypt.hasRemaining() ) {
			unwrap();
			inData.flip();
			limit = Math.min( inData.limit(), dst.remaining() );
			for( int i = 0 ; i < limit ; i++ ) {
				dst.put( inData.get() );
				amount++;
			}
			if( res.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
				inCrypt.clear();
				inCrypt.flip();
				return amount;
			}
		}
		if( !inCrypt.hasRemaining() )
			inCrypt.clear();
		else
			inCrypt.compact();

		if( sc.read( inCrypt ) == -1 ) {
			inCrypt.clear();
			inCrypt.flip();
			return -1;
		}
		inCrypt.flip();
		unwrap();
		// write in dst
		// inData.flip();
		limit = Math.min( inData.limit(), dst.remaining() );
		for( int i = 0 ; i < limit ; i++ ) {
			dst.put( inData.get() );
			amount++;
		}
		return amount;

	}

	public boolean isConnected() {
		return sc.isConnected();
	}

	public void close() throws IOException {
		sslEngine.closeOutbound();
		sslEngine.getSession().invalidate();
		outCrypt.compact();
		int wr = sc.write( wrap( emptybuffer ) );
		sc.close();
	}

	private boolean isHandShakeComplete(){
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
	public void write() throws IOException {
		write( emptybuffer );
	}
}