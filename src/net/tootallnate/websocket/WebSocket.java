package net.tootallnate.websocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
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
  /**
   * The default port of WebSockets, as defined in the spec. If the nullary
   * constructor is used, DEFAULT_PORT will be the port the WebSocketServer
   * is binded to. Note that ports under 1024 usually require root permissions.
   */
  public static final int DEFAULT_PORT = 80;
  /**
   * The WebSocket protocol expects UTF-8 encoded bytes.
   */
  public static final String UTF8_CHARSET = "UTF-8";
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

  /**
   * @return True if all of the text was sent to the client by this thread.
   *    False if some of the text had to be buffered to be sent later.
   */
  public boolean send(String text) throws IOException {
    if (!this.handshakeComplete) throw new NotYetConnectedException();
    if (text == null) throw new NullPointerException("Cannot send 'null' data to a WebSocket.");

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
      this.socketChannel.write(b);
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
        this.socketChannel.write(buffer);
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
  private void recieveFrame() {
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
        try {
          textFrame = new String(this.currentFrame.array(), UTF8_CHARSET);
        } catch (UnsupportedEncodingException ex) {
          // TODO: Fire an 'onError' handler here
          ex.printStackTrace();
          textFrame = "";
        }
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

  private void recieveHandshake() throws IOException, NoSuchAlgorithmException {
    ByteBuffer ch = ByteBuffer.allocate((this.remoteHandshake != null ? this.remoteHandshake.capacity() : 0) + this.buffer.capacity());
    if (this.remoteHandshake != null) {
      this.remoteHandshake.rewind();
      ch.put(this.remoteHandshake);
    }
    ch.put(this.buffer);
    this.remoteHandshake = ch;
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

    // If the ByteBuffer contains 8 random bytes,ends with
    // 0x0D 0x0A 0x0D 0x0A (or two CRLFs), and the response
    // contains Sec-WebSocket-Key1 then the client
    // handshake is complete for Draft 76 Server.
    } else if ((h.length>=12 && h[h.length-12] == CR
        && h[h.length-11] == LF
        && h[h.length-10] == CR
        && h[h.length-9] == LF) && new String(this.remoteHandshake.array(), UTF8_CHARSET).contains("Sec-WebSocket-Key1")) {
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

  private void completeHandshake(byte[] handShakeBody) throws IOException, NoSuchAlgorithmException {
    byte[] handshakeBytes = this.remoteHandshake.array();
    String handshake = new String(handshakeBytes, UTF8_CHARSET);
    this.handshakeComplete = true;
    if (this.wsl.onHandshakeRecieved(this, handshake, handShakeBody)) {
      this.wsl.onOpen(this);
    } else {
      close();
    }
  }

}
