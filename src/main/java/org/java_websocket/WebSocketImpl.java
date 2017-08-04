/*
 * Copyright (c) 2010-2017 Nathan Rajlich
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

package org.java_websocket;

import org.java_websocket.drafts.*;
import org.java_websocket.drafts.Draft.CloseHandshakeType;
import org.java_websocket.drafts.Draft.HandshakeState;
import org.java_websocket.exceptions.IncompleteHandshakeException;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.*;
import org.java_websocket.server.WebSocketServer.WebSocketWorker;
import org.java_websocket.util.Charsetfunctions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents one end (client or server) of a single WebSocketImpl connection.
 * Takes care of the "handshake" phase, then allows for easy sending of
 * text frames, and receiving frames through an event-based model.
 */
public class WebSocketImpl implements WebSocket {
	public static int RCVBUF = 16384;

    /**
     * Activate debug mode for additional infos
     */
	public static boolean DEBUG = false; // must be final in the future in order to take advantage of VM optimization

	/**
	 * Queue of buffers that need to be sent to the client.
	 */
	public final BlockingQueue<ByteBuffer> outQueue;
	/**
	 * Queue of buffers that need to be processed
	 */
	public final BlockingQueue<ByteBuffer> inQueue;
	/**
	 * The listener to notify of WebSocket events.
	 */
	private final WebSocketListener wsl;
	public SelectionKey key;
	/**
	 * the possibly wrapped channel object whose selection is controlled by {@link #key}
	 */
	public ByteChannel channel;
	/**
	 * Helper variable meant to store the thread which ( exclusively ) triggers this objects decode method.
	 **/
	public volatile WebSocketWorker workerThread; // TODO reset worker?
	/**
	 * When true no further frames may be submitted to be sent
	 */
	private volatile boolean flushandclosestate = false;
	private READYSTATE readystate = READYSTATE.NOT_YET_CONNECTED;

    /**
     * A list of drafts available for this websocket
     */
	private List<Draft> knownDrafts;

    /**
     * The draft which is used by this websocket
     */
	private Draft draft = null;

    /**
     * The role which this websocket takes in the connection
     */
	private Role role;

    /**
     * The frame which had the opcode Continous set
     */
	private Framedata current_continuous_frame = null;

	/**
	 * the bytes of an incomplete received handshake
	 */
	private ByteBuffer tmpHandshakeBytes = ByteBuffer.allocate( 0 );

	/**
	 * stores the handshake sent by this websocket ( Role.CLIENT only )
	 */
	private ClientHandshake handshakerequest = null;

	private String closemessage = null;
	private Integer closecode = null;
	private Boolean closedremotely = null;

	private String resourceDescriptor = null;

	/**
	 * Attribute, when the last pong was recieved
	 */
	private long lastPong = System.currentTimeMillis();

	/**
	 * Attribut to synchronize the write
	 */
	private static final Object synchronizeWriteObject = new Object();

	/**
	 * Creates a websocket with server role
	 *
	 * @param listener The listener for this instance
	 * @param drafts The drafts which should be used
	 */
	public WebSocketImpl( WebSocketListener listener, List<Draft> drafts ) {
		this( listener, ( Draft ) null );
		this.role = Role.SERVER;
		// draft.copyInstance will be called when the draft is first needed
		if( drafts == null || drafts.isEmpty() ) {
			knownDrafts = new ArrayList<Draft>();
			knownDrafts.add(new Draft_6455());
		} else {
			knownDrafts = drafts;
		}
	}

	/**
	 * creates a websocket with client role
	 *
	 * @param listener The listener for this instance
	 * @param draft The draft which should be used
	 */
	public WebSocketImpl( WebSocketListener listener, Draft draft ) {
		if( listener == null || ( draft == null && role == Role.SERVER ) )// socket can be null because we want do be able to create the object without already having a bound channel
			throw new IllegalArgumentException( "parameters must not be null" );
		this.outQueue = new LinkedBlockingQueue<ByteBuffer>();
		inQueue = new LinkedBlockingQueue<ByteBuffer>();
		this.wsl = listener;
		this.role = Role.CLIENT;
		if( draft != null )
			this.draft = draft.copyInstance();
	}

	@Deprecated
	public WebSocketImpl( WebSocketListener listener, Draft draft, Socket socket ) {
		this( listener, draft );
	}

	@Deprecated
	public WebSocketImpl( WebSocketListener listener, List<Draft> drafts, Socket socket ) {
		this( listener, drafts );
	}

	/**
	 * Method to decode the provided ByteBuffer
	 * @param socketBuffer the ByteBuffer to decode
	 */
	public void decode( ByteBuffer socketBuffer ) {
		assert ( socketBuffer.hasRemaining() );

		if( DEBUG )
			System.out.println( "process(" + socketBuffer.remaining() + "): {" + ( socketBuffer.remaining() > 1000 ? "too big to display" : new String( socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining() ) ) + "}" );

		if( readystate != READYSTATE.NOT_YET_CONNECTED ) {
			if ( readystate == READYSTATE.OPEN ) {
				decodeFrames( socketBuffer );
			}
		} else {
			if( decodeHandshake( socketBuffer ) ) {
				assert ( tmpHandshakeBytes.hasRemaining() != socketBuffer.hasRemaining() || !socketBuffer.hasRemaining() ); // the buffers will never have remaining bytes at the same time

				if( socketBuffer.hasRemaining() ) {
					decodeFrames( socketBuffer );
				} else if( tmpHandshakeBytes.hasRemaining() ) {
					decodeFrames( tmpHandshakeBytes );
				}
			}
		}
		assert ( isClosing() || isFlushAndClose() || !socketBuffer.hasRemaining() );
	}

	/**
	 * Returns whether the handshake phase has is completed.
	 * In case of a broken handshake this will be never the case.
	 **/
	private boolean decodeHandshake( ByteBuffer socketBufferNew ) {
		ByteBuffer socketBuffer;
		if( tmpHandshakeBytes.capacity() == 0 ) {
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
					try {
						write( Collections.singletonList( ByteBuffer.wrap(Charsetfunctions.utf8Bytes(wsl.getFlashPolicy(this)))));
						close(CloseFrame.FLASHPOLICY, "");
					} catch ( InvalidDataException e ) {
						close( CloseFrame.ABNORMAL_CLOSE, "remote peer closed connection before flashpolicy could be transmitted", true );
					}
					return false;
				}
			}
			HandshakeState handshakestate;

			try {
				if( role == Role.SERVER ) {
					if( draft == null ) {
						for( Draft d : knownDrafts ) {
							d = d.copyInstance();
							try {
								d.setParseMode( role );
								socketBuffer.reset();
								Handshakedata tmphandshake = d.translateHandshake( socketBuffer );
								if( !( tmphandshake instanceof ClientHandshake ) ) {
									closeConnectionDueToWrongHandshake( new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "wrong http function" ));
									return false;
								}
								ClientHandshake handshake = ( ClientHandshake ) tmphandshake;
								handshakestate = d.acceptHandshakeAsServer( handshake );
								if( handshakestate == HandshakeState.MATCHED ) {
									resourceDescriptor = handshake.getResourceDescriptor();
									ServerHandshakeBuilder response;
									try {
										response = wsl.onWebsocketHandshakeReceivedAsServer( this, d, handshake );
									} catch ( InvalidDataException e ) {
										closeConnectionDueToWrongHandshake( e );
										return false;
									} catch ( RuntimeException e ) {
										wsl.onWebsocketError( this, e );
										closeConnectionDueToInternalServerError( e );
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
							closeConnectionDueToWrongHandshake( new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "no draft matches"));
						}
						return false;
					} else {
						// special case for multiple step handshakes
						Handshakedata tmphandshake = draft.translateHandshake( socketBuffer );
						if( !( tmphandshake instanceof ClientHandshake ) ) {
							flushAndClose( CloseFrame.PROTOCOL_ERROR, "wrong http function", false );
							return false;
						}
						ClientHandshake handshake = ( ClientHandshake ) tmphandshake;
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
					if( !( tmphandshake instanceof ServerHandshake ) ) {
						flushAndClose( CloseFrame.PROTOCOL_ERROR, "wrong http function", false );
						return false;
					}
					ServerHandshake handshake = ( ServerHandshake ) tmphandshake;
					handshakestate = draft.acceptHandshakeAsClient( handshakerequest, handshake );
					if( handshakestate == HandshakeState.MATCHED ) {
						try {
							wsl.onWebsocketHandshakeReceivedAsClient( this, handshakerequest, handshake );
						} catch ( InvalidDataException e ) {
							flushAndClose( e.getCloseCode(), e.getMessage(), false );
							return false;
						} catch ( RuntimeException e ) {
							wsl.onWebsocketError( this, e );
							flushAndClose( CloseFrame.NEVER_CONNECTED, e.getMessage(), false );
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
			if( tmpHandshakeBytes.capacity() == 0 ) {
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

	/**
	 * Close the connection if the received handshake was not correct
	 * @param exception the InvalidDataException causing this problem
	 */
	private void closeConnectionDueToWrongHandshake( InvalidDataException exception ) {
		write(generateHttpResponseDueToError( 404 ));
		flushAndClose( exception.getCloseCode(), exception.getMessage(), false );
	}

	/**
	 * Close the connection if there was a server error by a RuntimeException
	 * @param exception the RuntimeException causing this problem
	 */
	private void closeConnectionDueToInternalServerError(RuntimeException exception) {
		write(generateHttpResponseDueToError( 500 ));
		flushAndClose( CloseFrame.NEVER_CONNECTED, exception.getMessage(), false );
	}

	/**
	 * Generate a simple response for the corresponding endpoint to indicate some error
	 * @param errorCode the http error code
	 * @return the complete response as ByteBuffer
	 */
	private ByteBuffer generateHttpResponseDueToError(int errorCode) {
		String errorCodeDescription;
		switch (errorCode) {
			case 404:
				errorCodeDescription = "404 WebSocket Upgrade Failure";
				break;
			case 500:
			default:
				errorCodeDescription =  "500 Internal Server Error";
		}
		return ByteBuffer.wrap( Charsetfunctions.asciiBytes( "HTTP/1.1 "+ errorCodeDescription +"\r\nContent-Type: text/html\nServer: TooTallNate Java-WebSocket\r\nContent-Length: " + (48 + errorCodeDescription.length()) +"\r\n\r\n<html><head></head><body><h1>" + errorCodeDescription + "</h1></body></html>" ));
	}

	private void decodeFrames( ByteBuffer socketBuffer ) {

		List<Framedata> frames;
		try {
			frames = draft.translateFrame( socketBuffer );
			for( Framedata f : frames ) {
				if( DEBUG )
					System.out.println( "matched frame: " + f );
				Opcode curop = f.getOpcode();
				boolean fin = f.isFin();
				//Not evaluating any further frames if the connection is in READYSTATE CLOSE
				if( readystate == READYSTATE.CLOSING )
					return;

				if( curop == Opcode.CLOSING ) {
					int code = CloseFrame.NOCODE;
					String reason = "";
					if( f instanceof CloseFrame ) {
						CloseFrame cf = ( CloseFrame ) f;
						code = cf.getCloseCode();
						reason = cf.getMessage();
					}
					if( readystate == READYSTATE.CLOSING ) {
						// complete the close handshake by disconnecting
						closeConnection( code, reason, true );
					} else {
						// echo close handshake
						if( draft.getCloseHandshakeType() == CloseHandshakeType.TWOWAY )
							close( code, reason, true );
						else
							flushAndClose( code, reason, false );
					}
					continue;
				} else if( curop == Opcode.PING ) {
					wsl.onWebsocketPing( this, f );
					continue;
				} else if( curop == Opcode.PONG ) {
					lastPong = System.currentTimeMillis();
					wsl.onWebsocketPong( this, f );
					continue;
				} else if( !fin || curop == Opcode.CONTINUOUS ) {
					if( curop != Opcode.CONTINUOUS ) {
						if( current_continuous_frame != null )
							throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "Previous continuous frame sequence not completed." );
						current_continuous_frame = f;
					} else if( fin ) {
						if( current_continuous_frame == null )
							throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence was not started." );
						//Check if the whole payload is valid utf8, when the opcode indicates a text
						if( current_continuous_frame.getOpcode() == Opcode.TEXT ) {
							//Checking a bit more from the frame before this one just to make sure all the code points are correct
							int off = Math.max( current_continuous_frame.getPayloadData().limit() - 64, 0 );
							current_continuous_frame.append( f );
							if( !Charsetfunctions.isValidUTF8( current_continuous_frame.getPayloadData(), off ) ) {
								throw new InvalidDataException( CloseFrame.NO_UTF8 );
							}
						}
						current_continuous_frame = null;
					} else if( current_continuous_frame == null ) {
						throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence was not started." );
					}
					//Check if the whole payload is valid utf8, when the opcode indicates a text
					if( curop == Opcode.TEXT ) {
						if( !Charsetfunctions.isValidUTF8( f.getPayloadData() ) ) {
							throw new InvalidDataException( CloseFrame.NO_UTF8 );
						}
					}
					//Checking if the current continous frame contains a correct payload with the other frames combined
					if( curop == Opcode.CONTINUOUS && current_continuous_frame != null && current_continuous_frame.getOpcode() == Opcode.TEXT ) {
						//Checking a bit more from the frame before this one just to make sure all the code points are correct
						int off = Math.max( current_continuous_frame.getPayloadData().limit() - 64, 0 );
						current_continuous_frame.append( f );
						if( !Charsetfunctions.isValidUTF8( current_continuous_frame.getPayloadData(), off ) ) {
							throw new InvalidDataException( CloseFrame.NO_UTF8 );
						}
					}
					try {
						wsl.onWebsocketMessageFragment( this, f );
					} catch ( RuntimeException e ) {
						wsl.onWebsocketError( this, e );
					}

				} else if( current_continuous_frame != null ) {
					throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence not completed." );
				} else if( curop == Opcode.TEXT ) {
					try {
						wsl.onWebsocketMessage( this, Charsetfunctions.stringUtf8( f.getPayloadData() ) );
					} catch ( RuntimeException e ) {
						wsl.onWebsocketError( this, e );
					}
				} else if( curop == Opcode.BINARY ) {
					try {
						wsl.onWebsocketMessage( this, f.getPayloadData() );
					} catch ( RuntimeException e ) {
						wsl.onWebsocketError( this, e );
					}
				} else {
					throw new InvalidDataException( CloseFrame.PROTOCOL_ERROR, "non control or continious frame expected" );
				}
			}
		} catch ( InvalidDataException e1 ) {
			wsl.onWebsocketError( this, e1 );
			close( e1 );
			return;
		}

	}

	private void close( int code, String message, boolean remote ) {
		if( readystate != READYSTATE.CLOSING && readystate != READYSTATE.CLOSED ) {
			if( readystate == READYSTATE.OPEN ) {
				if( code == CloseFrame.ABNORMAL_CLOSE ) {
					assert ( !remote );
					readystate = READYSTATE.CLOSING;
					flushAndClose( code, message, false );
					return;
				}
				if( draft.getCloseHandshakeType() != CloseHandshakeType.NONE ) {
					try {
						if( !remote ) {
							try {
								wsl.onWebsocketCloseInitiated( this, code, message );
							} catch ( RuntimeException e ) {
								wsl.onWebsocketError( this, e );
							}
						}
						CloseFrame closeFrame = new CloseFrame();
						closeFrame.setReason(message);
						closeFrame.setCode(code);
						try {
							closeFrame.isValid();
							sendFrame(closeFrame);
						} catch (InvalidDataException e) {
							//Rethrow invalid data exception
							throw e;
						}
					} catch ( InvalidDataException e ) {
						wsl.onWebsocketError( this, e );
						flushAndClose( CloseFrame.ABNORMAL_CLOSE, "generated frame is invalid", false );
					}
				}
				flushAndClose( code, message, remote );
			} else if( code == CloseFrame.FLASHPOLICY ) {
				assert ( remote );
				flushAndClose( CloseFrame.FLASHPOLICY, message, true );
			} else {
				flushAndClose( CloseFrame.NEVER_CONNECTED, message, false );
			}
			if( code == CloseFrame.PROTOCOL_ERROR )// this endpoint found a PROTOCOL_ERROR
				flushAndClose( code, message, remote );
			readystate = READYSTATE.CLOSING;
			tmpHandshakeBytes = null;
			return;
		}
	}

	@Override
	public void close( int code, String message ) {
		close( code, message, false );
	}

	/**
	 * This will close the connection immediately without a proper close handshake.
	 * The code and the message therefore won't be transfered over the wire also they will be forwarded to onClose/onWebsocketClose.
	 * @param code the closing code
	 * @param message the closing message
	 * @param remote Indicates who "generated" <code>code</code>.<br>
	 *               <code>true</code> means that this endpoint received the <code>code</code> from the other endpoint.<br>
	 *               false means this endpoint decided to send the given code,<br>
	 *               <code>remote</code> may also be true if this endpoint started the closing handshake since the other endpoint may not simply echo the <code>code</code> but close the connection the same time this endpoint does do but with an other <code>code</code>. <br>
	 **/
	protected synchronized void closeConnection( int code, String message, boolean remote ) {
		if( readystate == READYSTATE.CLOSED ) {
			return;
		}

		if( key != null ) {
			// key.attach( null ); //see issue #114
			key.cancel();
		}
		if( channel != null ) {
			try {
				channel.close();
			} catch ( IOException e ) {
				wsl.onWebsocketError( this, e );
			}
		}
		try {
			this.wsl.onWebsocketClose( this, code, message, remote );
		} catch ( RuntimeException e ) {
			wsl.onWebsocketError( this, e );
		}
		if( draft != null )
			draft.reset();
		handshakerequest = null;

		readystate = READYSTATE.CLOSED;
		this.outQueue.clear();
	}

	protected void closeConnection( int code, boolean remote ) {
		closeConnection( code, "", remote );
	}

	public void closeConnection() {
		if( closedremotely == null ) {
			throw new IllegalStateException( "this method must be used in conjuction with flushAndClose" );
		}
		closeConnection( closecode, closemessage, closedremotely );
	}

	public void closeConnection( int code, String message ) {
		closeConnection( code, message, false );
	}

	protected synchronized void flushAndClose( int code, String message, boolean remote ) {
		if( flushandclosestate ) {
			return;
		}
		closecode = code;
		closemessage = message;
		closedremotely = remote;

		flushandclosestate = true;

		wsl.onWriteDemand( this ); // ensures that all outgoing frames are flushed before closing the connection
		try {
			wsl.onWebsocketClosing( this, code, message, remote );
		} catch ( RuntimeException e ) {
			wsl.onWebsocketError( this, e );
		}
		if( draft != null )
			draft.reset();
		handshakerequest = null;
	}

	public void eot() {
		if( getReadyState() == READYSTATE.NOT_YET_CONNECTED ) {
			closeConnection( CloseFrame.NEVER_CONNECTED, true );
		} else if( flushandclosestate ) {
			closeConnection( closecode, closemessage, closedremotely );
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
	}

	@Override
	public void close( int code ) {
		close( code, "", false );
	}

	public void close( InvalidDataException e ) {
		close( e.getCloseCode(), e.getMessage(), false );
	}

	/**
	 * Send Text data to the other end.
	 *
	 * @throws NotYetConnectedException websocket is not yet connected
	 */
	@Override
	public void send( String text ) throws WebsocketNotConnectedException {
		if( text == null )
			throw new IllegalArgumentException( "Cannot send 'null' data to a WebSocketImpl." );
		send( draft.createFrames( text, role == Role.CLIENT ) );
	}

	/**
	 * Send Binary data (plain bytes) to the other end.
	 *
	 * @throws IllegalArgumentException the data is null
	 * @throws NotYetConnectedException websocket is not yet connected
	 */
	@Override
	public void send( ByteBuffer bytes ) throws IllegalArgumentException, WebsocketNotConnectedException {
		if( bytes == null )
			throw new IllegalArgumentException( "Cannot send 'null' data to a WebSocketImpl." );
		send( draft.createFrames( bytes, role == Role.CLIENT ) );
	}

	@Override
	public void send( byte[] bytes ) throws IllegalArgumentException, WebsocketNotConnectedException {
		send( ByteBuffer.wrap( bytes ) );
	}

	private void send( Collection<Framedata> frames ) {
		if( !isOpen() )
			throw new WebsocketNotConnectedException();
		ArrayList<ByteBuffer> outgoingFrames = new ArrayList<ByteBuffer>();
		for (Framedata f : frames) {
			if( DEBUG )
				System.out.println( "send frame: " + f );
			 outgoingFrames.add( draft.createBinaryFrame( f ) );
		}
		write( outgoingFrames );
	}

	@Override
	public void sendFragmentedFrame( Opcode op, ByteBuffer buffer, boolean fin ) {
		send( draft.continuousFrame( op, buffer, fin ) );
	}

	@Override
	public void sendFrame( Framedata framedata ) {
		send ( Collections.singletonList( framedata ) );
	}

	public void sendPing() throws NotYetConnectedException {
		sendFrame(new PingFrame());
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

			for( int flash_policy_index = 0; request.hasRemaining(); flash_policy_index++ ) {
				if( Draft.FLASH_POLICY_REQUEST[flash_policy_index] != request.get() ) {
					request.reset();
					return HandshakeState.NOT_MATCHED;
				}
			}
			return HandshakeState.MATCHED;
		}
	}

	public void startHandshake( ClientHandshakeBuilder handshakedata ) throws InvalidHandshakeException {
		assert ( readystate != READYSTATE.CONNECTING ) : "shall only be called once";

		// Store the Handshake Request we are about to send
		this.handshakerequest = draft.postProcessHandshakeRequestAsClient( handshakedata );

		resourceDescriptor = handshakedata.getResourceDescriptor();
		assert ( resourceDescriptor != null );

		// Notify Listener
		try {
			wsl.onWebsocketHandshakeSentAsClient( this, this.handshakerequest );
		} catch ( InvalidDataException e ) {
			// Stop if the client code throws an exception
			throw new InvalidHandshakeException( "Handshake data rejected by client." );
		} catch ( RuntimeException e ) {
			wsl.onWebsocketError( this, e );
			throw new InvalidHandshakeException( "rejected because of" + e );
		}

		// Send
		write( draft.createHandshake( this.handshakerequest, role ) );
	}

	private void write( ByteBuffer buf ) {
		if( DEBUG )
			System.out.println( "write(" + buf.remaining() + "): {" + ( buf.remaining() > 1000 ? "too big to display" : new String( buf.array() ) ) + "}" );

		outQueue.add( buf );
		/*try {
			outQueue.put( buf );
		} catch ( InterruptedException e ) {
			write( buf );
			Thread.currentThread().interrupt(); // keep the interrupted status
			e.printStackTrace();
		}*/
		wsl.onWriteDemand( this );
	}

	private void write( List<ByteBuffer> bufs ) {
		synchronized ( synchronizeWriteObject ) {
			for (ByteBuffer b : bufs) {
				write(b);
			}
		}
	}

	private void open( Handshakedata d ) {
		if( DEBUG )
			System.out.println( "open using draft: " + draft );
		readystate = READYSTATE.OPEN;
		try {
			wsl.onWebsocketOpen( this, d );
		} catch ( RuntimeException e ) {
			wsl.onWebsocketError( this, e );
		}
	}

	@Override
	public boolean isConnecting() {
		assert ( !flushandclosestate || readystate == READYSTATE.CONNECTING );
		return readystate == READYSTATE.CONNECTING; // ifflushandclosestate
	}

	@Override
	public boolean isOpen() {
		assert ( readystate != READYSTATE.OPEN || !flushandclosestate );
		return readystate == READYSTATE.OPEN;
	}

	@Override
	public boolean isClosing() {
		return readystate == READYSTATE.CLOSING;
	}

	@Override
	public boolean isFlushAndClose() {
		return flushandclosestate;
	}

	@Override
	public boolean isClosed() {
		return readystate == READYSTATE.CLOSED;
	}

	@Override
	public READYSTATE getReadyState() {
		return readystate;
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
		return wsl.getRemoteSocketAddress( this );
	}

	@Override
	public InetSocketAddress getLocalSocketAddress() {
		return wsl.getLocalSocketAddress( this );
	}

	@Override
	public Draft getDraft() {
		return draft;
	}

	@Override
	public void close() {
		close( CloseFrame.NORMAL );
	}

	@Override
	public String getResourceDescriptor() {
		return resourceDescriptor;
	}

	/**
	 * Getter for the last pong recieved
	 * @return the timestamp for the last recieved pong
	 */
	long getLastPong() {
		return lastPong;
	}
}
