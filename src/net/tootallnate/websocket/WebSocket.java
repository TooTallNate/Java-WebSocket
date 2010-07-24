package net.tootallnate.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

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
  public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
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
   * The bytes that make up the remote handshake.
   */
  private ByteBuffer remoteHandshake;
  /**
   * The bytes that make up the current text frame being read.
   */
  private ByteBuffer currentFrame;


  // CONSTRUCTOR /////////////////////////////////////////////////////////////
  /**
   * Used in {@link WebSocketServer} and {@link WebSocketClient}.
   * @param socketChannel The <tt>SocketChannel</tt> instance to read and
   *                      write to. The channel should already be registered
   *                      with a Selector before construction of this object.
   * @param listener The {@link WebSocketListener} to notify of events when
   *                 they occur.
   */
  WebSocket(SocketChannel socketChannel, WebSocketListener listener) {
    this.socketChannel = socketChannel;
    this.handshakeComplete = false;
    this.remoteHandshake = this.currentFrame = null;
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
    this.buffer.rewind();
    
    int bytesRead = -1;
    try {
      bytesRead = this.socketChannel.read(this.buffer);
    } catch(Exception ex) {}
    
    if (bytesRead == -1) {
      close();
    } else if (bytesRead > 0) {
      this.buffer.rewind();

      if (!this.handshakeComplete) {
        recieveHandshake();
      } else {
        recieveFrame();
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

  public void send(String text) throws IOException {
    if (!this.handshakeComplete) throw new NotYetConnectedException();
    if (text == null) throw new NullPointerException("Cannot send 'null' data to a WebSocket.");

    // Get 'text' into a WebSocket "frame" of bytes
    byte[] textBytes = text.getBytes(UTF8_CHARSET);
    ByteBuffer b = ByteBuffer.allocate(textBytes.length + 2);
    b.put(START_OF_FRAME);
    b.put(textBytes);
    b.put(END_OF_FRAME);

    // Write the ByteBuffer to the socket
    b.rewind();
    this.socketChannel.write(b);
  }

  public SocketChannel socketChannel() {
    return this.socketChannel;
  }

  // PRIVATE INSTANCE METHODS ////////////////////////////////////////////////
  private void recieveFrame() {
    byte newestByte = this.buffer.get();

    if (newestByte == START_OF_FRAME) { // Beginning of Frame
      this.currentFrame = null;

    } else if (newestByte == END_OF_FRAME) { // End of Frame
      String textFrame = null;
      // currentFrame will be null if END_OF_FRAME was send directly after
      // START_OF_FRAME, thus we will send 'null' as the sent message.
      if (this.currentFrame != null)
        textFrame = new String(this.currentFrame.array(), UTF8_CHARSET);
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

  private void recieveHandshake() throws IOException, NoSuchAlgorithmException {
    ByteBuffer ch = ByteBuffer.allocate((this.remoteHandshake != null ? this.remoteHandshake.capacity() : 0) + this.buffer.capacity());
    if (this.remoteHandshake != null) {
      this.remoteHandshake.rewind();
      ch.put(this.remoteHandshake);
    }
    ch.put(this.buffer);
    this.remoteHandshake = ch;

    // If the ByteBuffer contains 8 random bytes and ends with
    // 0x0D 0x0A 0x0D 0x0A (or two CRLFs), then the client
    // handshake is complete for Draft 76.
    byte[] h = this.remoteHandshake.array();
    if ((h.length>=12 && h[h.length-12] == CR
        && h[h.length-11] == LF
        && h[h.length-10] == CR
        && h[h.length-9] == LF)) {
  	  byte[] key3 = new byte[8];
      key3[0]=h[h.length-8];
      key3[1]=h[h.length-7];
      key3[2]=h[h.length-6];
      key3[3]=h[h.length-5];
      key3[4]=h[h.length-4];
      key3[5]=h[h.length-3];
      key3[6]=h[h.length-2];
      key3[7]=h[h.length-1];
      completeHandshake(key3);
      
    // Consider Draft 75, and the Flash Security Policy
    // Request edge-case.
    } else if ((h.length>=4 && h[h.length-4] == CR
        && h[h.length-3] == LF
        && h[h.length-2] == CR
        && h[h.length-1] == LF) && !(new String(this.remoteHandshake.array(), UTF8_CHARSET).contains("Sec")) ||
        (h.length==23 && h[h.length-1] == 0) ) {
    		
  		completeHandshake(null);
  	}    
  }

  private void completeHandshake(byte[] key3) throws IOException, NoSuchAlgorithmException {
  	byte[] handshakeBytes = this.remoteHandshake.array();
    String handshake = new String(handshakeBytes, UTF8_CHARSET);
    this.handshakeComplete = true;
    if (this.wsl.onHandshakeRecieved(this, handshake, key3)) {
      this.wsl.onOpen(this);
    } else {
      close();
    }
  }
}
