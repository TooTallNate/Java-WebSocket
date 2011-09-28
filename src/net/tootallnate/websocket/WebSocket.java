package net.tootallnate.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
   * The WebSocket protocol expects UTF-8 encoded bytes.
   */ 
  public final static Charset  UTF8_CHARSET = Charset.forName ( "UTF-8" );
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
  
  public static final boolean DEBUG = true;


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
  WebSocket(SocketChannel socketChannel, BlockingQueue<ByteBuffer> bufferQueue, WebSocketListener listener, Draft draft) {
    this.socketChannel = socketChannel;
    this.bufferQueue = bufferQueue;
    this.handshakeComplete = false;
    this.socketBuffer = ByteBuffer.allocate(8192);
    this.wsl = listener;
    this.role = Role.CLIENT;
    this.draft = draft; 
  }
  
  WebSocket(SocketChannel socketChannel, BlockingQueue<ByteBuffer> bufferQueue, WebSocketListener listener,  List<Draft> drafts ) {
	    this.socketChannel = socketChannel;
	    this.bufferQueue = bufferQueue;
	    this.handshakeComplete = false;
	    this.socketBuffer = ByteBuffer.allocate(8192);
	    this.wsl = listener;
	    this.role = Role.SERVER;
	    this.draft = null;
	    if( known_drafts == null || known_drafts.isEmpty () ){
	    	known_drafts = new ArrayList<Draft> ( 1 );
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
  void handleRead() throws IOException, NoSuchAlgorithmException {

    int bytesRead = -1;
    
    socketBuffer.rewind();
    bytesRead = this.socketChannel.read(this.socketBuffer);
    
    
    if (bytesRead == -1)  {
      close();
    } else if (bytesRead > 0) {
		if(DEBUG) System.out.println( "got: {" + new String( socketBuffer.array() , 0 , bytesRead ) + "}" );
		if( !handshakeComplete ){
			try{
				Handshakedata handshake = Draft.translateHandshake ( socketBuffer.array () , bytesRead );
				if( role == Role.SERVER ){
					for( Draft d : known_drafts ){
						if(  d.acceptHandshakeAsServer( handshake ) ){
							HandshakeBuilder response = wsl.onHandshakeRecievedAsServer ( this , d , handshake  );
							channelWrite ( d.createHandshake ( d.postProcessHandshakeResponseAsServer ( handshake , response ) , role ) );
							draft = d;
							this.handshakeComplete = true;
							wsl.onOpen ( this );
							break;
						}
					}
					if( draft == null ){
						abort( "no draft matches");
						return;
					}
					if(DEBUG) System.out.println ( "Using draft: " + draft.getClass ().getSimpleName () );
				}
				else if( role == Role.CLIENT){
					if( draft.acceptHandshakeAsClient ( handshakerequest , handshake ) 
						&& wsl.onHandshakeRecievedAsClient ( this , handshakerequest , handshake )
					){
						this.handshakeComplete = true;
						wsl.onOpen ( this );
					}
					else{
						abort( "draft "+draft.getClass ().getSimpleName ()+" or server refuses handshake");
					}
				}
			}
			catch (InvalidHandshakeException e) {
				abort( "draft "+draft.getClass ().getSimpleName ()+" refuses handshake: " + e.getMessage ());
			}
		}
		else{
			List<Framedata> frames = draft.translateFrame ( socketBuffer.array () , bytesRead );
			for( Framedata f : frames){
				if( currentframe == null){
					if( f.isFin () ){
						wsl.onMessage ( this , new String ( f.getPayloadData () , UTF8_CHARSET ) );
					}
					else{
						currentframe = f;
					}
				}
				else{
					try {
						currentframe.append ( f );
					} catch ( InvalidFrameException e ) {
						wsl.onError ( e );
						abort( "invalid frame: " +e.getMessage () );
					}
					if( f.isFin () ){
						wsl.onMessage ( this , new String ( f.getPayloadData () , UTF8_CHARSET ) );
						currentframe = null;
					}
				}
			}
		
		}
    }
  }

  // PUBLIC INSTANCE METHODS /////////////////////////////////////////////////
  public void abort( ) throws IOException {
	  abort ( "" );
  }
  public void abort( String problemmessage ) throws IOException {
	  if(DEBUG) System.out.println ( "Aborting: " + problemmessage );
	  close();
  }
  /**
   * Closes the underlying SocketChannel, and calls the listener's onClose
   * event handler.
   * @throws IOException When socket related I/O errors occur.
   */
  public void close() throws IOException {
	//TODO Send HTTP error here in some cases / create abort method
	currentframe = null;
	handshakerequest = null;
    this.socketChannel.close();
    this.wsl.onClose(this);
  }

  /**
   * @return True if all of the text was sent to the client by this thread or the given data is empty
   *    False if some of the text had to be buffered to be sent later.
   * @throws IOException 
   */
  public boolean send(String text) throws IOException {
    if (!this.handshakeComplete) throw new NotYetConnectedException();
    if ( text == null ) throw new NullPointerException( "Cannot send 'null' data to a WebSocket." );
    boolean mask = role == Role.CLIENT;
    List<Framedata> frames = draft.createFrames ( text , mask );
    if( frames.isEmpty () ){
    	return true;
    }
    boolean sendall = true;
    for( Framedata f : frames ){
    	sendall = sendFrame ( f ); //TODO high frequently calls to sendFrame are inefficient.
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
  
  public void startHandshake( Handshakedata handshakedata ){
	if( handshakeComplete ) 
		throw new IllegalStateException ( "Handshake has allready been sent." );
	this.handshakerequest  = handshakedata;

  }
  
  private void channelWrite(ByteBuffer buf) throws IOException{
	  if(DEBUG) System.out.println("write: {"+new String(buf.array())+"}");
	  //printBytes ( buf , buf.capacity () );
	  buf.rewind ();
	  socketChannel.write(buf);
  }
  
  private void printBytes(ByteBuffer buf, int len){
	  for( int i = 0 ; i < 2 && i < len; i++ ){
		  System.out.println (Integer.toBinaryString ( buf.get ( i ) ));
	  }
	  
  }

}
