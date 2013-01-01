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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Implements the relevant portions of the SocketChannel interface with the SSLEngine wrapper.
 */
public class SSLSocketChannel2 implements ByteChannel, WrappedByteChannel {
	protected static ByteBuffer emptybuffer = ByteBuffer.allocate( 0 );

	protected ExecutorService exec;

	protected List<Future<?>> tasks;

	/** raw payload incomming */
	protected ByteBuffer inData;
	/** encrypted data outgoing */
	protected ByteBuffer outCrypt;
	/** encrypted data incoming */
	protected ByteBuffer inCrypt;

	protected SocketChannel sc;
	protected SelectionKey key;

	protected SSLEngineResult res;
	protected SSLEngine sslEngine;
	protected final boolean isblocking;

	public SSLSocketChannel2( SocketChannel channel , SSLEngine sslEngine , ExecutorService exec , SelectionKey key ) throws IOException {
		this.sc = channel;

		this.sslEngine = sslEngine;
		this.exec = exec;

		tasks = new ArrayList<Future<?>>( 3 );
		if( key != null ) {
			key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );
			this.key = key;
		}
		isblocking = channel.isBlocking();

		sslEngine.setEnableSessionCreation( true );
		SSLSession session = sslEngine.getSession();
		createBuffers( session );

		sc.write( wrap( emptybuffer ) );// initializes res
		processHandshake();
	}

	private void consumeFutureUninterruptible( Future<?> f ) {
		try {
			boolean interrupted = false;
			while ( true ) {
				try {
					f.get();
					break;
				} catch ( InterruptedException e ) {
					interrupted = true;
				}
			}
			if( interrupted )
				Thread.currentThread().interrupt();
		} catch ( ExecutionException e ) {
			throw new RuntimeException( e );
		}
	}

	private synchronized void processHandshake() throws IOException {
		if( !tasks.isEmpty() ) {
			Iterator<Future<?>> it = tasks.iterator();
			while ( it.hasNext() ) {
				Future<?> f = it.next();
				if( f.isDone() ) {
					it.remove();
				} else {
					if( isBlocking() )
						consumeFutureUninterruptible( f );
					return;
				}
			}
		}

		if( res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP ) {
			inCrypt.compact();
			int read = sc.read( inCrypt );
			if( read == -1 ) {
				throw new IOException( "connection closed unexpectedly by peer" );
			}
			inCrypt.flip();
			inData.compact();
			unwrap();
		}
		consumeDelegatedTasks();
		if( tasks.isEmpty() || res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_WRAP ) {
			sc.write( wrap( emptybuffer ) );
		}

	}

	private synchronized ByteBuffer wrap( ByteBuffer b ) throws SSLException {
		outCrypt.compact();
		res = sslEngine.wrap( b, outCrypt );
		outCrypt.flip();
		return outCrypt;
	}

	private synchronized ByteBuffer unwrap() throws SSLException {
		int rem;
		do {
			rem = inData.remaining();
			res = sslEngine.unwrap( inCrypt, inData );
		} while ( rem != inData.remaining() );

		inData.flip();
		return inData;
	}

	protected void consumeDelegatedTasks() {
		Runnable task;
		while ( ( task = sslEngine.getDelegatedTask() ) != null ) {
			tasks.add( exec.submit( task ) );
			// task.run();
		}
	}

	protected void createBuffers( SSLSession session ) {
		int appBufferMax = session.getApplicationBufferSize();
		int netBufferMax = session.getPacketBufferSize();

		inData = ByteBuffer.allocate( appBufferMax );
		outCrypt = ByteBuffer.allocate( netBufferMax );
		inCrypt = ByteBuffer.allocate( netBufferMax );
		inData.flip();
		inCrypt.flip();
		outCrypt.flip();
	}

	public int write( ByteBuffer src ) throws IOException {
		if( !isHandShakeComplete() ) {
			processHandshake();
			return 0;
		}
		int num = sc.write( wrap( src ) );
		return num;

	}

	public int read( ByteBuffer dst ) throws IOException {
		if( !dst.hasRemaining() )
			return 0;
		if( isBlocking() ) {
			while ( !isHandShakeComplete() ) {
				processHandshake();
			}
		} else {
			processHandshake();
			if( !isHandShakeComplete() ) {
				return 0;
			}
		}

		int purged = readRemaining( dst );
		if( purged != 0 )
			return purged;

		assert ( inData.position() == 0 );
		inData.clear();

		if( !inCrypt.hasRemaining() )
			inCrypt.clear();
		else
			inCrypt.compact();

		if( sc.read( inCrypt ) == -1 ) {
			return -1;
		}
		inCrypt.flip();
		unwrap();
		return transfereTo( inData, dst );

	}

	private int readRemaining( ByteBuffer dst ) throws SSLException {
		if( inData.hasRemaining() ) {
			return transfereTo( inData, dst );
		}
		assert ( !inData.hasRemaining() );
		inData.clear();
		// test if some bytes left from last read (e.g. BUFFER_UNDERFLOW)
		if( inCrypt.hasRemaining() ) {
			unwrap();
			int amount = transfereTo( inData, dst );
			if( amount > 0 )
				return amount;
		}
		return 0;
	}

	public boolean isConnected() {
		return sc.isConnected();
	}

	public void close() throws IOException {
		sslEngine.closeOutbound();
		sslEngine.getSession().invalidate();
		if( sc.isOpen() )
			sc.write( wrap( emptybuffer ) );// FIXME what if not all bytes can be written
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
		return inData.hasRemaining() || ( inCrypt.hasRemaining() && res.getStatus() != Status.BUFFER_UNDERFLOW );
	}

	@Override
	public int readMore( ByteBuffer dst ) throws SSLException {
		return readRemaining( dst );
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

	@Override
	public boolean isBlocking() {
		return isblocking;
	}

}