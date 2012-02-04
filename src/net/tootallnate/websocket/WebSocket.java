package net.tootallnate.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.tootallnate.websocket.Draft.HandshakeState;
import net.tootallnate.websocket.Framedata.Opcode;
import net.tootallnate.websocket.drafts.Draft_10;
import net.tootallnate.websocket.drafts.Draft_17;
import net.tootallnate.websocket.drafts.Draft_75;
import net.tootallnate.websocket.drafts.Draft_76;
import net.tootallnate.websocket.exeptions.IncompleteHandshakeException;
import net.tootallnate.websocket.exeptions.InvalidDataException;
import net.tootallnate.websocket.exeptions.InvalidFrameException;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;

/**
 * Represents one end (client or server) of a single WebSocket connection.
 * Takes care of the "handshake" phase, then allows for easy sending of
 * text frames, and recieving frames through an event-based model.
 * 
 * This is an inner class, used by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>, and should never need to be instantiated directly
 * by your code. However, instances are exposed in <tt>WebSocketServer</tt> through the <i>onClientOpen</i>, <i>onClientClose</i>,
 * <i>onClientMessage</i> callbacks.
 * 
 * @author Nathan Rajlich
 */
public final class WebSocket {
	// CONSTANTS ///////////////////////////////////////////////////////////////
	public enum Role {
		CLIENT, SERVER
	}
	/**
	 * The default port of WebSockets, as defined in the spec. If the nullary
	 * constructor is used, DEFAULT_PORT will be the port the WebSocketServer
	 * is binded to. Note that ports under 1024 usually require root permissions.
	 */
	public static final int DEFAULT_PORT = 80;

	public static/*final*/boolean DEBUG = false; // must be final in the future in order to take advantage of VM optimization

	/**
	 * Internally used to determine whether to receive data as part of the
	 * remote handshake, or as part of a text frame.
	 */
	private boolean handshakeComplete = false;

	private boolean closeHandshakeSent = false;
	private boolean connectionClosed = false;
	private boolean isClosePending = false;
	/**
	 * The listener to notify of WebSocket events.
	 */
	private WebSocketListener wsl;
	/**
	 * Buffer where data is read to from the socket
	 */
	private ByteBuffer socketBuffer;
	/**
	 * Queue of buffers that need to be sent to the client.
	 */
	private BlockingQueue<ByteBuffer> bufferQueue;

	private Draft draft = null;

	private Role role;

	private Framedata currentframe;

	private Handshakedata handshakerequest = null;

	public List<Draft> known_drafts;

	private static final byte[] FLASH_POLICY_REQUEST = Charsetfunctions.utf8Bytes( "<policy-file-request/>" );

	private int flash_policy_index = 0;

	private SocketChannel sockchannel;

	// CONSTRUCTOR /////////////////////////////////////////////////////////////
	/**
	 * Used in {@link WebSocketServer} and {@link WebSocketClient}.
	 * 
	 * @param socketChannel
	 *            The <tt>SocketChannel</tt> instance to read and
	 *            write to. The channel should already be registered
	 *            with a Selector before construction of this object.
	 * @param listener
	 *            The {@link WebSocketListener} to notify of events when
	 *            they occur.
	 */
	public WebSocket( WebSocketListener listener , Draft draft , SocketChannel sockchannel ) {
		init( listener, draft, sockchannel );
	}

	public WebSocket( WebSocketListener listener , List<Draft> drafts , SocketChannel sockchannel ) {
		init( listener, null, sockchannel );
		this.role = Role.SERVER;
		if( known_drafts == null || known_drafts.isEmpty() ) {
			known_drafts = new ArrayList<Draft>( 1 );
			known_drafts.add( new Draft_17() );
			known_drafts.add( new Draft_10() );
			known_drafts.add( new Draft_76() );
			known_drafts.add( new Draft_75() );
		} else {
			known_drafts = drafts;
		}
	}

	private void init( WebSocketListener listener, Draft draft, SocketChannel sockchannel ) {
		this.sockchannel = sockchannel;
		this.bufferQueue = new LinkedBlockingQueue<ByteBuffer>( 10 );
		this.socketBuffer = ByteBuffer.allocate( 65558 );
		socketBuffer.flip();
		this.wsl = listener;
		this.role = Role.CLIENT;
		this.draft = draft;
	}

	/**
	 * Should be called when a Selector has a key that is writable for this
	 * WebSocket's SocketChannel connection.
	 * 
	 * @throws IOException
	 *             When socket related I/O errors occur.
	 * @throws InterruptedException
	 */
	/*package public*/void handleRead() throws IOException {
		if( !socketBuffer.hasRemaining() ) {
			socketBuffer.rewind();
			socketBuffer.limit( socketBuffer.capacity() );
			if( sockchannel.read( socketBuffer ) == -1 ) {
				close( CloseFrame.ABNROMAL_CLOSE );
			}

			socketBuffer.flip();
		}

		if( socketBuffer.hasRemaining() ) {
			if( DEBUG )
				System.out.println( "process(" + socketBuffer.remaining() + "): {" + ( socketBuffer.remaining() > 1000 ? "too big to display" : new String( socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining() ) ) + "}" );
			if( !handshakeComplete ) {
				Handshakedata handshake;
				HandshakeState handshakestate = null;

				handshakestate = isFlashEdgeCase( socketBuffer );
				if( handshakestate == HandshakeState.MATCHED ) {
					channelWriteDirect( ByteBuffer.wrap( Charsetfunctions.utf8Bytes( wsl.getFlashPolicy( this ) ) ) );
					return;
				}
				socketBuffer.mark();
				try {
					if( role == Role.SERVER ) {
						if( draft == null ) {
							for( Draft d : known_drafts ) {
								try {
									d.setParseMode( role );
									socketBuffer.reset();
									handshake = d.translateHandshake( socketBuffer );
									handshakestate = d.acceptHandshakeAsServer( handshake );
									if( handshakestate == HandshakeState.MATCHED ) {
										HandshakeBuilder response = wsl.onHandshakeRecievedAsServer( this, d, handshake );
										writeDirect( d.createHandshake( d.postProcessHandshakeResponseAsServer( handshake, response ), role ) );
										draft = d;
										open( handshake );
										handleRead();
										return;
									} else if( handshakestate == HandshakeState.MATCHING ) {
										if( draft != null ) {
											throw new InvalidHandshakeException( "multible drafts matching" );
										}
										draft = d;
									}
								} catch ( InvalidHandshakeException e ) {
									// go on with an other draft
								} catch ( IncompleteHandshakeException e ) {
									if( socketBuffer.limit() == socketBuffer.capacity() ) {
										close( CloseFrame.TOOBIG, "handshake is to big" );
									}
									// read more bytes for the handshake
									socketBuffer.position( socketBuffer.limit() );
									socketBuffer.limit( socketBuffer.capacity() );
									return;
								}
							}
							if( draft == null ) {
								close( CloseFrame.PROTOCOL_ERROR, "no draft matches" );
							}
							return;
						} else {
							// special case for multiple step handshakes
							handshake = draft.translateHandshake( socketBuffer );
							handshakestate = draft.acceptHandshakeAsServer( handshake );

							if( handshakestate == HandshakeState.MATCHED ) {
								open( handshake );
								handleRead();
							} else if( handshakestate != HandshakeState.MATCHING ) {
								close( CloseFrame.PROTOCOL_ERROR, "the handshake did finaly not match" );
							}
							return;
						}
					} else if( role == Role.CLIENT ) {
						draft.setParseMode( role );
						handshake = draft.translateHandshake( socketBuffer );
						handshakestate = draft.acceptHandshakeAsClient( handshakerequest, handshake );
						if( handshakestate == HandshakeState.MATCHED ) {
							open( handshake );
							handleRead();
						} else if( handshakestate == HandshakeState.MATCHING ) {
							return;
						} else {
							close( CloseFrame.PROTOCOL_ERROR, "draft " + draft + " refuses handshake" );
						}
					}
				} catch ( InvalidHandshakeException e ) {
					close( e );
				}
			} else {
				// Receiving frames
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
								close( code, reason );
								closeConnection( code, reason, false );
							}
							continue;
						} else if( curop == Opcode.PING ) {
							wsl.onPing( this, f );
							continue;
						} else if( curop == Opcode.PONG ) {
							wsl.onPong( this, f );
							continue;
						} else {
							// process non control frames
							if( currentframe == null ) {
								if( f.getOpcode() == Opcode.CONTINIOUS ) {
									throw new InvalidFrameException( "unexpected continious frame" );
								} else if( f.isFin() ) {
									// receive normal onframe message
									deliverMessage( f );
								} else {
									// remember the frame whose payload is about to be continued
									currentframe = f;
								}
							} else if( f.getOpcode() == Opcode.CONTINIOUS ) {
								currentframe.append( f );
								if( f.isFin() ) {
									deliverMessage( currentframe );
									currentframe = null;
								}
							} else {
								throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "non control or continious frame expected" );
							}
						}
					}
				} catch ( InvalidDataException e1 ) {
					wsl.onError( this, e1 );
					close( e1 );
					return;
				}
			}
		}
	}

	// PUBLIC INSTANCE METHODS /////////////////////////////////////////////////

	/**
	 * sends the closing handshake.
	 * may be send in response to an other handshake.
	 */
	public void close( int code, String message ) {
		try {
			closeDirect( code, message );
		} catch ( IOException e ) {
			closeConnection( CloseFrame.ABNROMAL_CLOSE, true );
		}
	}

	public void closeDirect( int code, String message ) throws IOException {
		if( !closeHandshakeSent ) {
			if( handshakeComplete ) {
				flush();
				try {
					sendFrameDirect( new CloseFrameBuilder( code, message ) );
				} catch ( InvalidDataException e ) {
					wsl.onError( this, e );
					closeConnection( CloseFrame.ABNROMAL_CLOSE, "generated frame is invalid", false );
				}
			} else {
				closeConnection( CloseFrame.NEVERCONNECTED, false );
			}
			if( code == CloseFrame.PROTOCOL_ERROR )// this endpoint found a PROTOCOL_ERROR
				closeConnection( code, false );
			closeHandshakeSent = true;
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
	public void closeConnection( int code, String message, boolean remote ) {
		if( connectionClosed ) {
			return;
		}
		connectionClosed = true;
		try {
			sockchannel.close();
		} catch ( IOException e ) {
			wsl.onError( this, e );
		}
		this.wsl.onClose( this, code, message, remote );
		if( draft != null )
			draft.reset();
		currentframe = null;
		handshakerequest = null;
	}

	public void closeConnection( int code, boolean remote ) {
		closeConnection( code, "", remote );
	}

	public void close( int code ) {
		close( code, "" );
	}

	public void close( InvalidDataException e ) {
		close( e.getCloseCode(), e.getMessage() );
	}

	/**
	 * @return True if all of the text was sent to the client by this thread or the given data is empty
	 *         False if some of the text had to be buffered to be sent later.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void send( String text ) throws IllegalArgumentException , NotYetConnectedException , InterruptedException {
		if( text == null )
			throw new IllegalArgumentException( "Cannot send 'null' data to a WebSocket." );
		send( draft.createFrames( text, role == Role.CLIENT ) );
	}

	// TODO there should be a send for bytebuffers
	public void send( byte[] bytes ) throws IllegalArgumentException , NotYetConnectedException , InterruptedException {
		if( bytes == null )
			throw new IllegalArgumentException( "Cannot send 'null' data to a WebSocket." );
		send( draft.createFrames( bytes, role == Role.CLIENT ) );
	}

	private void send( Collection<Framedata> frames ) throws InterruptedException {
		if( !this.handshakeComplete )
			throw new NotYetConnectedException();
		for( Framedata f : frames ) {
			sendFrame( f );
		}
	}

	public void sendFrame( Framedata framedata ) throws InterruptedException {
		if( DEBUG )
			System.out.println( "send frame: " + framedata );
		channelWrite( draft.createBinaryFrame( framedata ) );
	}

	private void sendFrameDirect( Framedata framedata ) throws IOException {
		if( DEBUG )
			System.out.println( "send frame: " + framedata );
		channelWriteDirect( draft.createBinaryFrame( framedata ) );
	}

	boolean hasBufferedData() {
		return !this.bufferQueue.isEmpty();
	}

	/**
	 * @return True if all data has been sent to the client, false if there
	 *         is still some buffered.
	 */
	public void flush() throws IOException {
		ByteBuffer buffer = this.bufferQueue.peek();
		while ( buffer != null ) {
			sockchannel.write( buffer );
			if( buffer.remaining() > 0 ) {
				continue;
			} else {
				this.bufferQueue.poll(); // Buffer finished. Remove it.
				buffer = this.bufferQueue.peek();
			}
		}
	}

	public HandshakeState isFlashEdgeCase( ByteBuffer request ) {
		if( flash_policy_index >= FLASH_POLICY_REQUEST.length )
			return HandshakeState.NOT_MATCHED;
		request.mark();
		for( ; request.hasRemaining() && flash_policy_index < FLASH_POLICY_REQUEST.length ; flash_policy_index++ ) {
			if( FLASH_POLICY_REQUEST[ flash_policy_index ] != request.get() ) {
				request.reset();
				return HandshakeState.NOT_MATCHED;
			}
		}
		return request.remaining() >= FLASH_POLICY_REQUEST.length ? HandshakeState.MATCHED : HandshakeState.MATCHING;
	}

	public void startHandshake( HandshakeBuilder handshakedata ) throws InvalidHandshakeException , InterruptedException {
		if( handshakeComplete )
			throw new IllegalStateException( "Handshake has allready been sent." );
		this.handshakerequest = handshakedata;
		channelWrite( draft.createHandshake( draft.postProcessHandshakeRequestAsClient( handshakedata ), role ) );
	}

	private void channelWrite( ByteBuffer buf ) throws InterruptedException {
		if( DEBUG )
			System.out.println( "write(" + buf.limit() + "): {" + ( buf.limit() > 1000 ? "too big to display" : new String( buf.array() ) ) + "}" );
		buf.rewind();
		bufferQueue.put( buf );
		wsl.onWriteDemand( this );
	}

	private void channelWrite( List<ByteBuffer> bufs ) throws InterruptedException {
		for( ByteBuffer b : bufs ) {
			channelWrite( b );
		}
	}

	private void channelWriteDirect( ByteBuffer buf ) throws IOException {
		while ( buf.hasRemaining() )
			sockchannel.write( buf );
	}
	private void writeDirect( List<ByteBuffer> bufs ) throws IOException {
		for( ByteBuffer b : bufs ) {
			channelWriteDirect( b );
		}
	}
	private void deliverMessage( Framedata d ) throws InvalidDataException {
		if( d.getOpcode() == Opcode.TEXT ) {
			wsl.onMessage( this, Charsetfunctions.stringUtf8( d.getPayloadData() ) );
		} else if( d.getOpcode() == Opcode.BINARY ) {
			wsl.onMessage( this, d.getPayloadData() );
		} else {
			if( DEBUG )
				System.out.println( "Ignoring frame:" + d.toString() );
			assert ( false );
		}
	}

	private void open( Handshakedata d ) throws IOException {
		if( DEBUG )
			System.out.println( "open using draft: " + draft.getClass().getSimpleName() );
		handshakeComplete = true;
		wsl.onOpen( this, d );
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return (InetSocketAddress) sockchannel.socket().getRemoteSocketAddress();
	}

	public InetSocketAddress getLocalSocketAddress() {
		return (InetSocketAddress) sockchannel.socket().getLocalSocketAddress();
	}

	public boolean isClosed() {
		return connectionClosed;
	}

	@Override
	public String toString() {
		return super.toString(); // its nice to be able to set breakpoints here
	}

}
