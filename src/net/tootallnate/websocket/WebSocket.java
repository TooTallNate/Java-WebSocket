package net.tootallnate.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import websync.WebSocketClientTest;

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
   * The 1-byte buffer reused throughout the WebSocket connection to read data.
   */
  private ByteBuffer buffer;
  /**
   * Buffer where data is read to from the socket
   */
  private ByteBuffer socketBuffer;
  /**
   * The bytes that make up the remote handshake.
   */
  private ByteBuffer remoteHandshake;
  /**
   * The bytes that make up the current text frame being read.
   */
  private ByteBuffer currentFrame;
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
  
  private WebSocketDraft draft;
  


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
  WebSocket(SocketChannel socketChannel, BlockingQueue<ByteBuffer> bufferQueue, WebSocketListener listener) {
    this.socketChannel = socketChannel;
    this.bufferQueue = bufferQueue;
    this.handshakeComplete = false;
    this.remoteHandshake = this.currentFrame = null;
    this.socketBuffer = ByteBuffer.allocate(8192);
    this.buffer = ByteBuffer.allocate(1);
    this.wsl = listener;
  }

  /**
   * Should be called when a Selector has a key that is writable for this
   * WebSocket's SocketChannel connection.
   * @throws IOException When socket related I/O errors occur.
   * @throws NoSuchAlgorithmException 
   */
  void handleRead() throws IOException, NoSuchAlgorithmException {

    int bytesRead = -1;
    
    try {
      socketBuffer.rewind();
      bytesRead = this.socketChannel.read(this.socketBuffer);
    } catch(Exception ex) {}
    
    if (bytesRead == -1)  {
      close();
    } else if (bytesRead > 0) {
    	System.out.print( "got: {\n" + new String( socketBuffer.array() , 0 , bytesRead ) + "\n}" );
		if ( !this.handshakeComplete && recieveHandshake ( bytesRead ) ) {
		}
		else{ 
			recieveFrame(bytesRead);
		}
		if(true)
			return;
		{
	      for( int i = 0 ; i < bytesRead ; i++ ) {
	        buffer.rewind();
	        buffer.put( socketBuffer.get( i ) );
	
	        this.buffer.rewind();
	
	        if (!this.handshakeComplete) 
	        	recieveHandshake( bytesRead );
	        	//recieveHandshake75_76( bytesRead);
	        else 
	          recieveFrame75_76();    	      
	      }
		}
    }
  }

  // PUBLIC INSTANCE METHODS /////////////////////////////////////////////////
  /**
   * Closes the underlying SocketChannel, and calls the listener's onClose
   * event handler.
   * @throws IOException When socket related I/O errors occur.
   */
  public void close() throws IOException {
    this.socketChannel.close();
    this.wsl.onClose(this);
  }

  /**
   * @return True if all of the text was sent to the client by this thread.
   *    False if some of the text had to be buffered to be sent later.
   */
  public boolean send(String text) throws IOException {
    if (!this.handshakeComplete) throw new NotYetConnectedException();
    if ( text == null ) throw new NullPointerException( "Cannot send 'null' data to a WebSocket." );
    if( draft == WebSocketDraft.DRAFT10 ){
    	return send10( text);
    }
    else
    	return send75_76( text );
  }
  
  public boolean send75_76(String text) throws IOException {
     if (!this.handshakeComplete) throw new NotYetConnectedException();
     if ( text == null ) throw new NullPointerException( "Cannot send 'null' data to a WebSocket." );

    // Get 'text' into a WebSocket "frame" of bytes
    byte[] textBytes = text.getBytes(UTF8_CHARSET);
    ByteBuffer b = ByteBuffer.allocate(textBytes.length + 2);
    b.put(START_OF_FRAME);
    b.put(textBytes);
    b.put(END_OF_FRAME);
    b.rewind();

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

  private boolean send10( String text ) throws IOException {
	  boolean mask=false;
	  byte[] mes   = text.getBytes ( UTF8_CHARSET );
	  ByteBuffer b = ByteBuffer.allocate ( 2 + mes.length+( mask ? 4 : 0 ) );
	  ByteBuffer maskkey=  ByteBuffer.allocate ( 4 );
	  byte one = ( byte ) -127;
	  //if(mask)
	  //  one |= 1;
	  b.put ( one ); 						// b1 controll
	  b.put ( ( byte ) mes.length );		// b2 length
	  if( mask ){		
		  maskkey.putInt ( Integer.MIN_VALUE );
		  b.put ( maskkey.array () );		
		  for( int i = 0 ; i < mes.length ; i++){
			  b.put( ( byte ) ( mes[i] ^ maskkey.get ( i % 4 ) ) );
		  }
	  }
	  else
		  b.put ( mes );
	  
	  b.rewind ();
	  channelWrite ( b );
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

  // PRIVATE INSTANCE METHODS ////////////////////////////////////////////////
  private void recieveFrame75_76() {
    byte newestByte = this.buffer.get();

    if (newestByte == START_OF_FRAME && !readingState) { // Beginning of Frame
      this.currentFrame = null;
      readingState = true;

    } else if (newestByte == END_OF_FRAME && readingState) { // End of Frame
      readingState = false;
      String textFrame = null;
      // currentFrame will be null if END_OF_FRAME was send directly after
      // START_OF_FRAME, thus we will send 'null' as the sent message.
      if (this.currentFrame != null) {
          textFrame = new String( this.currentFrame.array() , UTF8_CHARSET );
      }
      this.wsl.onMessage(this, textFrame);

    } else { // Regular frame data, add to current frame buffer
      ByteBuffer frame = ByteBuffer.allocate((this.currentFrame != null ? this.currentFrame.capacity() : 0) + this.buffer.capacity());
      if (this.currentFrame != null) {
        this.currentFrame.rewind();
        frame.put(this.currentFrame);
      }
      frame.put(newestByte);
      this.currentFrame = frame;
    }
  }
  
  private void recieveFrame( int read) throws IOException {
	  byte[] b = ByteBuffer.allocate ( read ).put ( socketBuffer.array () , 0 , read ).array ();//socketBuffer.array ()
	  boolean FIN  =  b[0] >> 8 != 0;
	  boolean MASK =  ( b[0] &~1 ) != 0;
	  int payloadlength = (byte)( b[1] & ~(byte)128 );
	  System.out.println ( "pll: " + payloadlength );
	  int maskskeytart = payloadlength < 125 ? 1 + 1 : payloadlength == 126 ? 1 + 2 : 1 + 8 ;
	  int extdatastart = maskskeytart+4; //TODO allow extdata
	  int payloadstart = extdatastart; 
	  byte[] maskskey  = ByteBuffer.allocate ( 4 ).put (  b , maskskeytart , 4 ).array ();
	  //demasking the payloaddata 
	  ByteBuffer payload=ByteBuffer.allocate ( payloadlength );
	  for( int i = 0 ; i < payloadlength ; i++ ){
		  payload.put ( ( byte ) ( (byte)b[payloadstart + i] ^ (byte)maskskey[ i%4  ] ) );
	  }
	  
	  /*b[1]&=~(byte)128;
	  for( int i = 0 ; i < payloadlength ; i++ ){
		 b[ payloadstart + i - 4 ] =  ( byte ) ( (byte)b[payloadstart + i] ^ (byte)maskskey[ i%4  ] );
	  }
	  byte[] c = ByteBuffer.allocate ( read-4 ).put ( b , 0 , read-4 ).array ();
	  channelWrite ( ByteBuffer.wrap ( c ) );*/
	  this.wsl.onMessage ( this , new String ( payload.array () ,UTF8_CHARSET ) );
	  
  }

  private boolean recieveHandshake75_76(int bytesRead) throws IOException {
    ByteBuffer ch = ByteBuffer.allocate( ( this.remoteHandshake != null ? this.remoteHandshake.capacity() : 0 ) + this.buffer.capacity() );
    if ( this.remoteHandshake != null ) {
      this.remoteHandshake.rewind();
      ch.put( this.remoteHandshake );
    }
    ch.put(this.buffer);
    this.remoteHandshake = ch;
    //byte[] h2 = ByteBuffer.wrap(socketBuffer.array(),0, bytesRead).array();
    ByteBuffer b=ByteBuffer.allocate( bytesRead );
    b.put( socketBuffer.array() , 0 , bytesRead );
    byte[] h2 =  b.array();
    byte[] h = this.remoteHandshake.array();
    // If the ByteBuffer contains 16 random bytes, and ends with
    // 0x0D 0x0A 0x0D 0x0A (or two CRLFs), then the client
    // handshake is complete for Draft 76 Client.
    if((h.length >= 20 && h[h.length-20] == CR
            && h[h.length-19] == LF
            && h[h.length-18] == CR
            && h[h.length-17] == LF)) {
      completeHandshake(new byte[] {
        h[h.length-16],
        h[h.length-15],
        h[h.length-14],
        h[h.length-13],
        h[h.length-12],
        h[h.length-11],
        h[h.length-10],
        h[h.length-9],
        h[h.length-8],
        h[h.length-7],
        h[h.length-6],
        h[h.length-5],
        h[h.length-4],
        h[h.length-3],
        h[h.length-2],
        h[h.length-1]
      });
      draft = WebSocketDraft.DRAFT76;
      return true;  
    // If the ByteBuffer contains 8 random bytes,ends with
    // 0x0D 0x0A 0x0D 0x0A (or two CRLFs), and the response
    // contains Sec-WebSocket-Key1 then the client
    // handshake is complete for Draft 76 Server.
    } else if ((h.length>=12 && h[h.length-12] == CR
        && h[h.length-11] == LF
        && h[h.length-10] == CR
        && h[h.length-9] == LF) && new String(this.remoteHandshake.array(), UTF8_CHARSET).contains("Sec-WebSocket-Key1")) {
    	System.out.println("------>Draft 76  Sec-WebSocket-Key1 8<------"+new String(h));
      completeHandshake(new byte[] {
        h[h.length-8],
        h[h.length-7],
        h[h.length-6],
        h[h.length-5],
        h[h.length-4],
        h[h.length-3],
        h[h.length-2],
        h[h.length-1]
      });
      draft = WebSocketDraft.DRAFT76;
      return true;
      
    // Consider Draft 75, and the Flash Security Policy
    // Request edge-case.
    } else if ((h.length>=4 && h[h.length-4] == CR
        && h[h.length-3] == LF
        && h[h.length-2] == CR
        && h[h.length-1] == LF) && !(new String(this.remoteHandshake.array(), UTF8_CHARSET).contains("Sec")) ||
        (h.length==23 && h[h.length-1] == 0) ) {
    	System.out.println("------>Draft 75 / flash<------"+new String(h));
      completeHandshake(null);
      draft = WebSocketDraft.DRAFT75;
      return true;
    } 
    else 
    	return false;
  }
  
	private boolean recieveHandshake( int readcount ) throws IOException {
		ByteBuffer message = ByteBuffer.allocate ( readcount );
		message.put ( socketBuffer.array () , 0 , readcount );
		byte[] lines = message.array ();
		int previndex = 0;
		int index = findNewLine ( lines , previndex );
		if ( index == -1 )
			return false;
		String line = new String ( lines , previndex , index - previndex );
		if ( line.startsWith ( "GET" ) == false )
			return false;
		previndex = index + 2;
		index = findNewLine ( lines , previndex );
	  
		HashMap<String,String> elements = new HashMap<String,String> ( 10 );
		while ( index != -1 ) {
			line = new String ( lines , previndex , index - previndex );
			if ( index != previndex ) {
				String[] pair = line.split ( ":" , 2 );
				if ( pair.length != 2 )
					return false;
				elements.put ( pair[ 0 ] , pair[ 1 ] );
				System.out.println ( "Line: " + line );
			}
			previndex = index + 2;
			index = findNewLine ( lines , previndex );
		}
		draft = WebSocketDraft.DRAFT10;
		return this.handshakeComplete = this.wsl.onHandshakeRecieved ( this , elements );
  }

	private static int findNewLine( byte[] arr , int offset ) {
	  int len = arr.length - 1;
	  for ( int i = offset ; i < len ; i++ ) 
		if( arr[i] == (byte)'\r' && arr[ i + 1 ] == (byte)'\n' )
			return i;
		return -1;
  }
 

  private void completeHandshake(byte[] handShakeBody) throws IOException {
    byte[] handshakeBytes = this.remoteHandshake.array();
    String handshake = new String(handshakeBytes, UTF8_CHARSET);
    this.handshakeComplete = true;
    if (this.wsl.onHandshakeRecieved(this, handshake, handShakeBody)) {
      this.wsl.onOpen(this);
    } else {
      close();
    }
  }
  public void channelWrite(ByteBuffer buf) throws IOException{
	  System.out.println("write: {\n"+new String(buf.array())+"}");
	  printBytes ( buf , buf.capacity () );
	  socketChannel.write(buf);
  }
  public void printBytes(ByteBuffer buf, int len){
	  for( int i = 0 ; i < 2 && i < len; i++ ){
		  System.out.println (Integer.toBinaryString ( buf.get ( i ) ));
	  }
	  
  }

}
