package org.java_websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft.CloseHandshakeType;
import org.java_websocket.drafts.Draft.HandshakeState;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.exceptions.IncompleteHandshakeException;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.CloseFrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer.WebSocketWorker;
import org.java_websocket.util.Charsetfunctions;

/**
 * Represents one end (client or server) of a single WebSocketImpl connection.
 * Takes care of the "handshake" phase, then allows for easy sending of
 * text frames, and receiving frames through an event-based model.
 * 
 * This is an inner class, used by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>, and should never need to be instantiated directly
 * by your code. However, instances are exposed in <tt>WebSocketServer</tt> through the <i>onClientOpen</i>, <i>onClientClose</i>,
 * <i>onClientMessage</i> callbacks.
 * 
 */
public class WebSocketImpl extends WebSocket {

	public SelectionKey key;

	/* only used to optain the socket addresses*/
	public final Socket socket;
	/** the possibly wrapped channel object whose selection is controlled by {@link #key} */
	public ByteChannel channel;
	/**
	 * Queue of buffers that need to be sent to the client.
	 */
	public final BlockingQueue<ByteBuffer> outQueue;
	/**
	 * Queue of buffers that need to be processed
	 */
	public final BlockingQueue<ByteBuffer> inQueue;

	/**
	 * Helper variable ment to store the thread which ( exclusively ) triggers this objects decode method.
	 **/
	public volatile WebSocketWorker workerThread; // TODO reset worker?


	/**
	 * Determines whether to receive data as part of the
	 * handshake, or as part of text/data frame transmitted over the websocket.
	 */
	private volatile boolean handshakeComplete = false;
	/**
	 * Determines whether we sent already a request to Close the connection or not.
	 */
	private volatile boolean closeHandshakeSent = false;
	/**
	 * Determines whether the connection is open or not
	 */
	private volatile boolean connectionClosed = false;


	/**
	 * The listener to notify of WebSocket events.
	 */
	private final WebSocketListener wsl;

	private List<Draft> knownDrafts;

	private Draft draft = null;

	private Role role;

	/** used to join continuous frames */
	private Framedata tempContiniousFrame;// FIXME out of mem risk

	/** the bytes of an incomplete received handshake */
	private ByteBuffer tmpHandshakeBytes;

	/** stores the handshake sent by this websocket ( Role.CLIENT only ) */
	private ClientHandshake handshakerequest = null;

	/**
	 * crates a websocket with server role
	 */
	public WebSocketImpl( WebSocketListener listener , List<Draft> drafts , Socket sock ) {
		this( listener, (Draft) null, sock );
		this.role = Role.SERVER;
		if( knownDrafts == null || knownDrafts.isEmpty() ) {
			knownDrafts = new ArrayList<Draft>( 1 );
			knownDrafts.add( new Draft_17() );
			knownDrafts.add( new Draft_10() );
			knownDrafts.add( new Draft_76() );
			knownDrafts.add( new Draft_75() );
		} else {
			knownDrafts = drafts;
		}
	}

	/**
	 * crates a websocket with client role
	 */
	public WebSocketImpl( WebSocketListener listener , Draft draft , Socket sock ) {
		this.outQueue = new LinkedBlockingQueue<ByteBuffer>();
		inQueue = new LinkedBlockingQueue<ByteBuffer>();
		this.wsl = listener;
		this.role = Role.CLIENT;
		this.draft = draft;
		this.socket = sock;
	}

	/**
	 * Should be called when a Selector has a key that is writable for this
	 * WebSocketImpl's SocketChannel connection.
	 * 
	 * @throws IOException
	 *             When socket related I/O errors occur.
	 * @throws InterruptedException
	 */
	public void decode( ByteBuffer socketBuffer ) throws IOException {
		if( !socketBuffer.hasRemaining() || connectionClosed )
			return;

		if( DEBUG )
			System.out.println( "process(" + socketBuffer.remaining() + "): {" + ( socketBuffer.remaining() > 1000 ? "too big to display" : new String( socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining() ) ) + "}" );

		if( handshakeComplete ) {
			decodeFrames( socketBuffer );
		} else {
			if( decodeHandshake( socketBuffer ) ) {
				decodeFrames( socketBuffer );
			}
		}
		assert ( isClosing() || isClosed() || !socketBuffer.hasRemaining() );
	}

	/**
	 * Returns whether the handshake phase has is completed.
	 * In case of a broken handshake this will be never the case.
	 **/
	private boolean decodeHandshake( ByteBuffer socketBufferNew ) throws IOException {
		ByteBuffer socketBuffer;
		if( tmpHandshakeBytes == null ) {
			socketBuffer = socketBufferNew;
		} else {
			if( tmpHandshakeBytes.remaining() < socketBufferNew.remaining() ) {
				ByteBuffer buf = ByteBuffer.allocate( tmpHandshakeBytes.capacity() + socketBufferNew.remaining() );
				tmpHandshakeBytes.flip();
				buf.put( tmpHandshakeBytes );
				tmpHandshakeBytes = buf;
			}

			tmpHandshakeBytes.put( socketBufferNew );
			tmpHandshakeBytes.flip();
			socketBuffer = tmpHandshakeBytes;
		}
		socketBuffer.mark();
		try {
			if( draft == null ) {
				HandshakeState isflashedgecase = isFlashEdgeCase( socketBuffer );
				if( isflashedgecase == HandshakeState.MATCHED ) {
					write( ByteBuffer.wrap( Charsetfunctions.utf8Bytes( wsl.getFlashPolicy( this ) ) ) );
					close( CloseFrame.FLASHPOLICY, "" );
					return false;
				}
			}
			HandshakeState handshakestate = null;

			try {
				if( role == Role.SERVER ) {
					if( draft == null ) {
						for( Draft d : knownDrafts ) {
							try {
								d.setParseMode( role );
								socketBuffer.reset();
								Handshakedata tmphandshake = d.translateHandshake( socketBuffer );
								if( tmphandshake instanceof ClientHandshake == false ) {
									closeConnection( CloseFrame.PROTOCOL_ERROR, "wrong http function", false );
									return false;
								}
								ClientHandshake handshake = (ClientHandshake) tmphandshake;
								handshakestate = d.acceptHandshakeAsServer( handshake );
								if( handshakestate == HandshakeState.MATCHED ) {
									ServerHandshakeBuilder response;
									try {
										response = wsl.onWebsocketHandshakeReceivedAsServer( this, d, handshake );
									} catch ( InvalidDataException e ) {
										closeConnection( e.getCloseCode(), e.getMessage(), false );
										return false;
									}
									write( d.createHandshake( d.postProcessHandshakeResponseAsServer( handshake, response ), role ) );
									draft = d;
									open( handshake );
									return true;
								}
							} catch ( InvalidHandshakeException e ) {
								// go on with an other draft
							}
						}
						if( draft == null ) {
							close( CloseFrame.PROTOCOL_ERROR, "no draft matches" );
						}
						return false;
					} else {
						// special case for multiple step handshakes
						Handshakedata tmphandshake = draft.translateHandshake( socketBuffer );
						if( tmphandshake instanceof ClientHandshake == false ) {
							closeConnection( CloseFrame.PROTOCOL_ERROR, "wrong http function", false );
							return false;
						}
						ClientHandshake handshake = (ClientHandshake) tmphandshake;
						handshakestate = draft.acceptHandshakeAsServer( handshake );

						if( handshakestate == HandshakeState.MATCHED ) {
							open( handshake );
							return true;
						} else {
							close( CloseFrame.PROTOCOL_ERROR, "the handshake did finaly not match" );
						}
						return false;
					}
				} else if( role == Role.CLIENT ) {
					draft.setParseMode( role );
					Handshakedata tmphandshake = draft.translateHandshake( socketBuffer );
					if( tmphandshake instanceof ServerHandshake == false ) {
						closeConnection( CloseFrame.PROTOCOL_ERROR, "Wwrong http function", false );
						return false;
					}
					ServerHandshake handshake = (ServerHandshake) tmphandshake;
					handshakestate = draft.acceptHandshakeAsClient( handshakerequest, handshake );
					if( handshakestate == HandshakeState.MATCHED ) {
						try {
							wsl.onWebsocketHandshakeReceivedAsClient( this, handshakerequest, handshake );
						} catch ( InvalidDataException e ) {
							closeConnection( e.getCloseCode(), e.getMessage(), false );
							return false;
						}
						open( handshake );
						return true;
					} else {
						close( CloseFrame.PROTOCOL_ERROR, "draft " + draft + " refuses handshake" );
					}
				}
			} catch ( InvalidHandshakeException e ) {
				close( e );
			}
		} catch ( IncompleteHandshakeException e ) {
			if( tmpHandshakeBytes == null ) {
				socketBuffer.reset();
				int newsize = e.getPreferedSize();
				if( newsize == 0 ) {
					newsize = socketBuffer.capacity() + 16;
				} else {
					assert ( e.getPreferedSize() >= socketBuffer.remaining() );
				}
				tmpHandshakeBytes = ByteBuffer.allocate( newsize );

				tmpHandshakeBytes.put( socketBufferNew );
				// tmpHandshakeBytes.flip();
			} else {
				tmpHandshakeBytes.position( tmpHandshakeBytes.limit() );
				tmpHandshakeBytes.limit( tmpHandshakeBytes.capacity() );
			}
		}
		return false;
	}

	private void decodeFrames( ByteBuffer socketBuffer ) {
		List<Framedata> frames;
		try {
			frames = draft.translateFrame( socketBuffer );
			for( Framedata f : frames ) {
				if( DEBUG )
					System.out.println( "matched frame: " + f );
				Opcode curop = f.getOpcode();
				if( curop == Opcode.CLOSING ) {
					int code = CloseFrame.NOCODE;
					String reason = "";
					if( f instanceof CloseFrame ) {
						CloseFrame cf = (CloseFrame) f;
						code = cf.getCloseCode();
						reason = cf.getMessage();
					}
					if( closeHandshakeSent ) {
						// complete the close handshake by disconnecting
						closeConnection( code, reason, true );
					} else {
						// echo close handshake
						if( draft.getCloseHandshakeType() == CloseHandshakeType.TWOWAY )
							close( code, reason );
						closeConnection( code, reason, false );
					}
					continue;
				} else if( curop == Opcode.PING ) {
					wsl.onWebsocketPing( this, f );
					continue;
				} else if( curop == Opcode.PONG ) {
					wsl.onWebsocketPong( this, f );
					continue;
				} else {
					// process non control frames
					if( tempContiniousFrame == null ) {
						if( f.getOpcode() == Opcode.CONTINUOUS ) {
							throw new InvalidFrameException( "unexpected continious frame" );
						} else if( f.isFin() ) {
							// receive normal onframe message
							deliverMessage( f );
						} else {
							// remember the frame whose payload is about to be continued
							tempContiniousFrame = f;
						}
					} else if( f.getOpcode() == Opcode.CONTINUOUS ) {
						tempContiniousFrame.append( f );
						if( f.isFin() ) {
							deliverMessage( tempContiniousFrame );
							tempContiniousFrame = null;
						}
					} else {
						throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "non control or continious frame expected" );
					}
				}
			}
		} catch ( InvalidDataException e1 ) {
			wsl.onWebsocketError( this, e1 );
			close( e1 );
			return;
		}
	}

	@Override
	public void close( int code, String message ) {
		if( !closeHandshakeSent ) {
			if( handshakeComplete ) {
				if( code == CloseFrame.ABNORMAL_CLOSE ) {
					closeConnection( code, true );
					closeHandshakeSent = true;
					return;
				}
				if( draft.getCloseHandshakeType() != CloseHandshakeType.NONE ) {
					try {
						sendFrame( new CloseFrameBuilder( code, message ) );
					} catch ( InvalidDataException e ) {
						wsl.onWebsocketError( this, e );
						closeConnection( CloseFrame.ABNORMAL_CLOSE, "generated frame is invalid", false );
					}
				} else {
					closeConnection( code, false );
				}
			} else if( code == CloseFrame.FLASHPOLICY ) {
				closeConnection( CloseFrame.FLASHPOLICY, true );
			} else {
				closeConnection( CloseFrame.NEVERCONNECTED, false );
			}
			if( code == CloseFrame.PROTOCOL_ERROR )// this endpoint found a PROTOCOL_ERROR
				closeConnection( code, false );
			closeHandshakeSent = true;
			tmpHandshakeBytes = null;
			return;
		}
	}

	/**
	 * closes the socket no matter if the closing handshake completed.
	 * Does not send any not yet written data before closing.
	 * Calling this method more than once will have no effect.
	 * 
	 * @param remote
	 *            Indicates who "generated" <code>code</code>.<br>
	 *            <code>true</code> means that this endpoint received the <code>code</code> from the other endpoint.<br>
	 *            false means this endpoint decided to send the given code,<br>
	 *            <code>remote</code> may also be true if this endpoint started the closing handshake since the other endpoint may not simply echo the <code>code</code> but close the connection the same time this endpoint does do but with an other <code>code</code>. <br>
	 **/

	protected synchronized void closeConnection( int code, String message, boolean remote ) {
		if( connectionClosed ) {
			return;
		}
		connectionClosed = true;

		if( key != null ) {
			// key.attach( null ); //see issue #114
			key.cancel();
			try {
				channel.close();
			} catch ( IOException e ) {
				wsl.onWebsocketError( this, e );
			}
		}
		// sockchannel.close();
		this.wsl.onWebsocketClose( this, code, message, remote );
		if( draft != null )
			draft.reset();
		tempContiniousFrame = null;
		handshakerequest = null;
	}

	protected void closeConnection( int code, boolean remote ) {
		closeConnection( code, "", remote );
	}

	public void eot( Exception e ) {
		if( e == null || e instanceof IOException ) {
			if( draft == null ) {
				closeConnection( CloseFrame.ABNORMAL_CLOSE, true );
			} else if( draft.getCloseHandshakeType() == CloseHandshakeType.NONE ) {
				closeConnection( CloseFrame.NORMAL, true );
			} else if( draft.getCloseHandshakeType() == CloseHandshakeType.ONEWAY ) {
				if( role == Role.SERVER )
					closeConnection( CloseFrame.ABNORMAL_CLOSE, true );
				else
					closeConnection( CloseFrame.NORMAL, true );
			} else {
				closeConnection( CloseFrame.ABNORMAL_CLOSE, true );
			}
		} else {
			closeConnection( CloseFrame.BUGGYCLOSE, e.toString(), false );
		}
	}

	@Override
	public void close( int code ) {
		close( code, "" );
	}

	@Override
	public void close( InvalidDataException e ) {
		close( e.getCloseCode(), e.getMessage() );
	}

	/**
	 * Send Text data to the other end.
	 * 
	 * @throws IllegalArgumentException
	 * @throws InterruptedException
	 * @throws NotYetConnectedException
	 */
	@Override
	public void send( String text ) throws NotYetConnectedException {
		if( text == null )
			throw new IllegalArgumentException( "Cannot send 'null' data to a WebSocketImpl." );
		send( draft.createFrames( text, role == Role.CLIENT ) );
	}

	/**
	 * Send Binary data (plain bytes) to the other end.
	 * 
	 * @throws IllegalArgumentException
	 * @throws InterruptedException
	 * @throws NotYetConnectedException
	 */
	@Override
	public void send( ByteBuffer bytes ) throws IllegalArgumentException , NotYetConnectedException , InterruptedException {
		if( bytes == null )
			throw new IllegalArgumentException( "Cannot send 'null' data to a WebSocketImpl." );
		send( draft.createFrames( bytes, role == Role.CLIENT ) );
	}

	@Override
	public void send( byte[] bytes ) throws IllegalArgumentException , NotYetConnectedException , InterruptedException {
		send( ByteBuffer.wrap( bytes ) );
	}

	private void send( Collection<Framedata> frames ) {
		if( !this.handshakeComplete )
			throw new NotYetConnectedException();
		for( Framedata f : frames ) {
			sendFrame( f );
		}
	}

	@Override
	public void sendFrame( Framedata framedata ) {
		if( DEBUG )
			System.out.println( "send frame: " + framedata );
		write( draft.createBinaryFrame( framedata ) );
	}

	@Override
	public boolean hasBufferedData() {
		return !this.outQueue.isEmpty();
	}

	private HandshakeState isFlashEdgeCase( ByteBuffer request ) throws IncompleteHandshakeException {
		request.mark();
		if( request.limit() > Draft.FLASH_POLICY_REQUEST.length ) {
			return HandshakeState.NOT_MATCHED;
		} else if( request.limit() < Draft.FLASH_POLICY_REQUEST.length ) {
			throw new IncompleteHandshakeException( Draft.FLASH_POLICY_REQUEST.length );
		} else {

			for( int flash_policy_index = 0 ; request.hasRemaining() ; flash_policy_index++ ) {
				if( Draft.FLASH_POLICY_REQUEST[ flash_policy_index ] != request.get() ) {
					request.reset();
					return HandshakeState.NOT_MATCHED;
				}
			}
			return HandshakeState.MATCHED;
		}
	}

	@Override
	public void startHandshake( ClientHandshakeBuilder handshakedata ) throws InvalidHandshakeException , InterruptedException {
		if( handshakeComplete )
			throw new IllegalStateException( "Handshake has already been sent." );

		// Store the Handshake Request we are about to send
		this.handshakerequest = draft.postProcessHandshakeRequestAsClient( handshakedata );

		// Notify Listener
		try {
			wsl.onWebsocketHandshakeSentAsClient( this, this.handshakerequest );
		} catch ( InvalidDataException e ) {
			// Stop if the client code throws an exception
			throw new InvalidHandshakeException( "Handshake data rejected by client." );
		}

		// Send
		write( draft.createHandshake( this.handshakerequest, role ) );
	}

	private void write( ByteBuffer buf ) {
		if( DEBUG )
			System.out.println( "write(" + buf.remaining() + "): {" + ( buf.remaining() > 1000 ? "too big to display" : new String( buf.array() ) ) + "}" );
		try {
			outQueue.put( buf );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
		wsl.onWriteDemand( this );
	}

	private void write( List<ByteBuffer> bufs ) {
		for( ByteBuffer b : bufs ) {
			write( b );
		}
	}

	private void deliverMessage( Framedata d ) throws InvalidDataException {
		try {
			if( d.getOpcode() == Opcode.TEXT ) {
				wsl.onWebsocketMessage( this, Charsetfunctions.stringUtf8( d.getPayloadData() ) );
			} else if( d.getOpcode() == Opcode.BINARY ) {
				wsl.onWebsocketMessage( this, d.getPayloadData() );
			} else {
				if( DEBUG )
					System.out.println( "Ignoring frame:" + d.toString() );
				assert ( false );
			}
		} catch ( RuntimeException e ) {
			wsl.onWebsocketError( this, e );
		}
	}

	private void open( Handshakedata d ) throws IOException {
		if( DEBUG )
			System.out.println( "open using draft: " + draft.getClass().getSimpleName() );
		handshakeComplete = true;
		wsl.onWebsocketOpen( this, d );
	}

	@Override
	public boolean isConnecting() {
		return ( !connectionClosed && !closeHandshakeSent && !handshakeComplete );
	}

	@Override
	public boolean isOpen() {
		return ( !connectionClosed && !closeHandshakeSent && handshakeComplete );
	}

	@Override
	public boolean isClosing() {
		return ( !connectionClosed && closeHandshakeSent );
	}

	@Override
	public boolean isClosed() {
		return connectionClosed;
	}

	/**
	 * Retrieve the WebSocketImpl 'readyState'.
	 * This represents the state of the connection.
	 * It returns a numerical value, as per W3C WebSockets specs.
	 * 
	 * @return Returns '0 = CONNECTING', '1 = OPEN', '2 = CLOSING' or '3 = CLOSED'
	 */
	@Override
	public int getReadyState() {
		if( isConnecting() ) {
			return READY_STATE_CONNECTING;
		} else if( isOpen() ) {
			return READY_STATE_OPEN;
		} else if( isClosing() ) {
			return READY_STATE_CLOSING;
		} else if( isClosed() ) {
			return READY_STATE_CLOSED;
		}
		assert ( false );
		return -1; // < This can't happen, by design!
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return super.toString(); // its nice to be able to set breakpoints here
	}

	@Override
	public InetSocketAddress getRemoteSocketAddress() {
		return (InetSocketAddress) socket.getRemoteSocketAddress();
	}

	@Override
	public InetSocketAddress getLocalSocketAddress() {
		return (InetSocketAddress) socket.getLocalSocketAddress();
	}

	@Override
	public Draft getDraft() {
		return draft;
	}

}
