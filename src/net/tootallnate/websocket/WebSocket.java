package net.tootallnate.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.tootallnate.websocket.Draft.HandshakeState;
import net.tootallnate.websocket.Framedata.Opcode;
import net.tootallnate.websocket.drafts.*;
import net.tootallnate.websocket.exeptions.InvalidFrameException;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;

import java.util.concurrent.BlockingQueue;

/**
 * Represents one end (client or server) of a single WebSocket connection.
 * Takes care of the "handshake" phase, then allows for easy sending of
 * text frames, and recieving frames through an event-based model.
 *
 * This is an inner class, used by <tt>WebSocketClient</tt> and
 * <tt>WebSocketServer</tt>, and should never need to be instantiated directly
 * by your code. However, instances are exposed in <tt>WebSocketServer</tt>
 * through the <i>onClientOpen</i>, <i>onClientClose</i>,
 * <i>onClientMessage</i> callbacks.
 * @author Nathan Rajlich
 */
public final class WebSocket {
  // CONSTANTS ///////////////////////////////////////////////////////////////
  public enum Role{
		CLIENT, SERVER
  }
  /**
   * The default port of WebSockets, as defined in the spec. If the nullary
   * constructor is used, DEFAULT_PORT will be the port the WebSocketServer
   * is binded to. Note that ports under 1024 usually require root permissions.
   */
  public static final int DEFAULT_PORT = 80;

  /** 
   * The byte representing CR, or Carriage Return, or \r
   */
  public static final byte CR = (byte)0x0D;
  /**
   * The byte representing LF, or Line Feed, or \n
   */
  public static final byte LF = (byte)0x0A;
  /**
   * The byte representing the beginning of a WebSocket text frame.
   */
  public static final byte START_OF_FRAME = (byte)0x00;
  /**
   * The byte representing the end of a WebSocket text frame.
   */
  public static final byte END_OF_FRAME = (byte)0xFF;

  public static /*final*/ boolean DEBUG = false; //must be final in the future in order to take advantage of VM optimization


  // INSTANCE PROPERTIES /////////////////////////////////////////////////////
  /**
   * The <tt>SocketChannel</tt> instance to use for this server connection.
   * This is used to read and write data to.
   */
  private final SocketChannel socketChannel;
  /**
   * Internally used to determine whether to recieve data as part of the
   * remote handshake, or as part of a text frame.
   */
  private boolean handshakeComplete;
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
  /**
   * Lock object to ensure that data is sent from the bufferQueue in
   * the proper order.
   */
  private Object bufferQueueMutex = new Object();
  
  private boolean readingState = false;
  
  private Draft draft = null;
  
  private Role role;
  
  private Framedata currentframe;
  
  private Handshakedata handshakerequest = null;
  
  public List<Draft> known_drafts;
  
  private final int maxpayloadsize;


  // CONSTRUCTOR /////////////////////////////////////////////////////////////
  /**
   * Used in {@link WebSocketServer} and {@link WebSocketClient}.
   * @param socketChannel The <tt>SocketChannel</tt> instance to read and
   *                      write to. The channel should already be registered
   *                      with a Selector before construction of this object.
   * @param bufferQueue The Queue that we should use to buffer data that
   *                     hasn't been sent to the client yet.
   * @param listener The {@link WebSocketListener} to notify of events when
   *                 they occur.
   */
  public WebSocket(SocketChannel socketChannel, BlockingQueue<ByteBuffer> bufferQueue, WebSocketListener listener, Draft draft, int maxpayloadsize) {
    this.socketChannel = socketChannel;
    this.bufferQueue = bufferQueue;
    this.handshakeComplete = false;
    this.socketBuffer = ByteBuffer.allocate(8192);
    this.wsl = listener;
    this.role = Role.CLIENT;
    this.draft = draft; 
    this.maxpayloadsize = maxpayloadsize;
  }
  
  public WebSocket(SocketChannel socketChannel, BlockingQueue<ByteBuffer> bufferQueue, WebSocketListener listener,  List<Draft> drafts , int maxpayloadsize ) {
	    this.socketChannel = socketChannel;
	    this.bufferQueue = bufferQueue;
	    this.handshakeComplete = false;
	    this.socketBuffer = ByteBuffer.allocate(8192);
	    this.wsl = listener;
	    this.role = Role.SERVER;
	    this.draft = null;
	    this.maxpayloadsize = maxpayloadsize;
	    if( known_drafts == null || known_drafts.isEmpty () ){
	    	known_drafts = new ArrayList<Draft> ( 1 );
	    	known_drafts.add ( new Draft_17 () );
	    	known_drafts.add ( new Draft_10 () );
	    	known_drafts.add ( new Draft_76 () );
	    	known_drafts.add ( new Draft_75 () );
	    }
	    else{
	    	known_drafts = drafts;
	    }
	  }

  /**
   * Should be called when a Selector has a key that is writable for this
   * WebSocket's SocketChannel connection.
   * @throws IOException When socket related I/O errors occur.
   * @throws NoSuchAlgorithmException 
   */
  public void handleRead() throws IOException {

    int bytesRead = -1;
    
    socketBuffer.rewind();
    bytesRead = this.socketChannel.read(this.socketBuffer);
    
    
    if ( bytesRead == -1 )  {
      close();
    } 
    else if( bytesRead > maxpayloadsize ){
    	wsl.onError ( this , new RuntimeException("recived packet to big") );
    	abort ( "recived packet to big" );
    }
    else if ( bytesRead > 0) {
		if(DEBUG) System.out.println( "got("+bytesRead+"): {" + new String( socketBuffer.array() , 0 , bytesRead ) + "}" );
		if( !handshakeComplete ){
			if(draft.isFlashEdgeCase( socketBuffer.array() , bytesRead )){
				channelWrite( ByteBuffer.wrap( wsl.getFlashPolicy( this ).getBytes( Draft.UTF8_CHARSET ) ) );
				return;
			}
			try{
				Handshakedata handshake;
				HandshakeState handshakestate = null;
				
				if( role == Role.SERVER ){
					if( draft == null ){
						handshake = Draft.translateHandshakeHttp( socketBuffer.array () , bytesRead );
						for( Draft d : known_drafts ){
							handshakestate = d.acceptHandshakeAsServer( handshake );
							if( handshakestate == HandshakeState.MATCHED ){
								HandshakeBuilder response = wsl.onHandshakeRecievedAsServer ( this , d , handshake  );
								channelWrite ( d.createHandshake ( d.postProcessHandshakeResponseAsServer ( handshake , response ) , role ) );
								draft = d;
								this.handshakeComplete = true;
								open();
								return;
							}
							else if ( handshakestate == HandshakeState.MATCHING ){
								if( draft != null ){
									throw new InvalidHandshakeException( "multible drafts matching" );
								}
								draft = d;
							}
						}
						if( draft == null ){
							abort( "no draft matches");
						}
						return;
					}
					else{
						//special case for multiple step handshakes
						handshake = draft.translateHandshake( socketBuffer.array () , bytesRead );
						handshakestate = draft.acceptHandshakeAsServer( handshake );

						if( handshakestate == HandshakeState.MATCHED ){
							open();
						}
						else if ( handshakestate != HandshakeState.MATCHING) {
							abort( "the handshake did finaly not match" );
						}
						return;
					}
				}
				else if( role == Role.CLIENT){
					handshake= draft.translateHandshake( socketBuffer.array () , bytesRead );
					handshakestate = draft.acceptHandshakeAsClient ( handshakerequest , handshake );
					if( handshakestate == HandshakeState.MATCHED ){
						this.handshakeComplete = true;
						open();
					}
					else if ( handshakestate == HandshakeState.MATCHING ) {
						return;
					}
					else{
						abort( "draft "+draft.getClass ().getSimpleName ()+" or server refuses handshake");
					}
				}
			}
			catch (InvalidHandshakeException e) {
				abort( "draft "+draft+" refuses handshake: " + e.getMessage ());
			}
		}
		else{
			//Receiving frames
			List<Framedata> frames = draft.translateFrame ( socketBuffer , bytesRead );
			for( Framedata f : frames){
				Opcode curop = f.getOpcode ();
				if( curop == null )// Ignore undefined opcodes
					continue;
				else if( curop == Opcode.CLOSING){
					sendFrame ( new FramedataImpl1 ( Opcode.CLOSING ) );
					close();
					continue;
				}
				else if( curop == Opcode.PING){
					sendFrame ( new FramedataImpl1 ( Opcode.PONG ) );
					continue;
				}
				else if( curop == Opcode.PONG){
					wsl.onPong ();
					continue;
				}
				if( currentframe == null){
					if( f.isFin () ){
						if( f.getOpcode () == Opcode.TEXT ){
							wsl.onMessage ( this , new String ( f.getPayloadData () , Draft.UTF8_CHARSET ) );
						}
						else if( f.getOpcode () == Opcode.BINARY ){
							wsl.onMessage ( this , f.getPayloadData () );
						}
						else{
							if(DEBUG) System.out.println ( "Ignoring frame:" + f.toString() );
						}
					}
					else{
						currentframe = f;
					}
				}
				else if( f.getOpcode() == Opcode.CONTINIOUS ){
					try {
						currentframe.append ( f );
					} catch ( InvalidFrameException e ) {
						wsl.onError ( this, e );
						abort( "invalid frame: " +e.getMessage () );
					}
					if( f.isFin () ){
						wsl.onMessage ( this , new String ( f.getPayloadData () , Draft.UTF8_CHARSET ) );
						currentframe = null;
					}
				}
			}
		}
	}
  }

  // PUBLIC INSTANCE METHODS /////////////////////////////////////////////////
  public void abort( ){
	  abort ( "" );
  }
  
  public void abort( String problemmessage ) {
	  if(DEBUG){
		System.out.println ( "Aborting: " + problemmessage );
	  }
	  close();
  }
  /**
   * Closes the underlying SocketChannel, and calls the listener's onClose
   * event handler.
   */
  public void close() {
	//TODO Send HTTP error here in some cases / create abort method
	draft.reset();
	currentframe = null;
	handshakerequest = null;
    try {
		this.socketChannel.close();
	} catch ( IOException e ) {
	}
    this.wsl.onClose(this);
  }

  /**
   * @return True if all of the text was sent to the client by this thread or the given data is empty
   *    False if some of the text had to be buffered to be sent later.
   * @throws IOException 
   */
  public boolean send( String text ) throws IOException, IllegalArgumentException, NotYetConnectedException {
    if ( text == null ) throw new IllegalArgumentException( "Cannot send 'null' data to a WebSocket." );
    return send( draft.createFrames ( text , role == Role.CLIENT ) );
  }
  
	//TODO there should be a send for bytebuffers
	public boolean send( byte[] bytes ) throws IOException, IllegalArgumentException, NotYetConnectedException {
		if ( bytes == null ) throw new IllegalArgumentException( "Cannot send 'null' data to a WebSocket." );
		return send( draft.createFrames ( bytes , role == Role.CLIENT ) );
	}
  
  
	private boolean send( Collection<Framedata> frames ) throws IOException{ //TODO instead of throwing or returning an error this method maybe should block on queue jams
		if (!this.handshakeComplete) throw new NotYetConnectedException();
		if( frames.isEmpty () ){
			return true;
		}  
		boolean sendall = true;
		for( Framedata f : frames ){
			sendall &= sendFrame ( f ); //TODO high frequently calls to sendFrame are inefficient.
		}
		return sendall;
  }
  
  
  
  public boolean sendFrame( Framedata framedata ) throws IOException{
	ByteBuffer b = draft.createBinaryFrame ( framedata );
	// See if we have any backlog that needs to be sent first
	if (handleWrite()) {
		// Write the ByteBuffer to the socket
		channelWrite( b );
	}

	// If we didn't get it all sent, add it to the buffer of buffers
	if (b.remaining() > 0) {
		if (!this.bufferQueue.offer(b)) {
	        throw new IOException("Buffers are full, message could not be sent to" +
	            this.socketChannel.socket().getRemoteSocketAddress());
		}
		return false;
	}
	return true;
  }
  
  boolean hasBufferedData() {
    return !this.bufferQueue.isEmpty();
  }

  /**
   * @return True if all data has been sent to the client, false if there
   *    is still some buffered.
   */
  boolean handleWrite() throws IOException {
    synchronized (this.bufferQueueMutex) {
      ByteBuffer buffer = this.bufferQueue.peek();
      while (buffer != null) {
        channelWrite( buffer );
        if (buffer.remaining() > 0) {
          return false; // Didn't finish this buffer.  There's more to send.
        } else {
          this.bufferQueue.poll();  // Buffer finished.  Remove it.
          buffer = this.bufferQueue.peek();
        }
      }
      return true;
    }
  }

  public SocketChannel socketChannel() {
    return this.socketChannel;
  }
  
  public void startHandshake( HandshakeBuilder handshakedata ) throws IOException, InvalidHandshakeException{
    if( handshakeComplete ) 
    throw new IllegalStateException ( "Handshake has allready been sent." );
    this.handshakerequest  = handshakedata;
    channelWrite( draft.createHandshake( draft.postProcessHandshakeRequestAsClient( handshakedata ) , role ) );
  }
  
  private void channelWrite(ByteBuffer buf) throws IOException{
	  if(DEBUG) System.out.println("write("+buf.array().length+"): {"+new String(buf.array())+"}");
	  //printBytes ( buf , buf.capacity () );
	  buf.rewind ();
	  socketChannel.write(buf);
  }
  
  private void channelWrite( List<ByteBuffer> bufs) throws IOException{
	for( ByteBuffer b : bufs ){
		channelWrite( b );
	}
  }
  
  
  private void open(){
	  if(DEBUG) System.out.println ( "open using draft: " + draft.getClass ().getSimpleName () );
	  handshakeComplete = true;
	  wsl.onOpen ( this );
  }
  
  public int getPort(){
	  return socketChannel.socket().getLocalPort();
  }
  
  @Override
	public String toString( ) {
		return super.toString(); //its nice to be able to set breakpoints here
	}
}
