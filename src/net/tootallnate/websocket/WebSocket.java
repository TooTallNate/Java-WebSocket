package net.tootallnate.websocket;

import java.io.IOException;
import java.math.BigInteger;
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
  public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
  /**
   * The byte representing CR, or Carriage Return, or \r
   */
  public static final byte CR = (byte) 0x0D;
  /**
   * The byte representing LF, or Line Feed, or \n
   */
  public static final byte LF = (byte) 0x0A;
  /**
   * The byte representing the beginning of a WebSocket text frame.
   */
  public static final byte START_OF_FRAME = (byte) 0x00;
  /**
   * The byte representing the end of a WebSocket text frame.
   */
  public static final byte END_OF_FRAME = (byte) 0xFF;

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
   * The 1-byte buffer reused throughout the WebSocket connection to read
   * data.
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

  private WebSocketDraft webSocketDraft = null;

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
  WebSocket(SocketChannel socketChannel,
      BlockingQueue<ByteBuffer> bufferQueue, WebSocketListener listener) {
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
   * 
   * @throws IOException
   *             When socket related I/O errors occur.
   * @throws NoSuchAlgorithmException
   */
  void handleRead() throws IOException, NoSuchAlgorithmException {
    this.buffer.rewind();

    int bytesRead = -1;
    try {
      bytesRead = this.socketChannel.read(this.buffer);
    } catch (Exception ex) {
    }

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
   * 
   * @throws IOException
   *             When socket related I/O errors occur.
   */
  public void close() throws IOException {
    this.socketChannel.close();
    this.wsl.onClose(this);
  }

  /**
   * @return True if all of the text was sent to the client by this thread.
   *         False if some of the text had to be buffered to be sent later.
   */
  public boolean send(String text) throws IOException {
    if (!this.handshakeComplete)
      throw new NotYetConnectedException();
    if (text == null)
      throw new NullPointerException(
          "Cannot send 'null' data to a WebSocket.");
    byte[] textBytes = text.getBytes(UTF8_CHARSET);
    ByteBuffer b;
    if (this.webSocketDraft == WebSocketDraft.VERSION_07) {
      long payloadLength = textBytes.length;
      long frameLength=2+payloadLength; //base header is two bytes
      PayloadLengthType payloadLengthType;
      byte payloadLengthByte=0;
      byte[] extendedPayloadLengthBytes=null;
      if(payloadLength<126){
        payloadLengthType=PayloadLengthType.SHORT_7BIT;
        payloadLengthByte=(byte)payloadLength;
      }else if(payloadLength<65536){
        payloadLengthType=PayloadLengthType.EXTENDED_16BIT;
        payloadLengthByte=0x07E; //if payload length byte = 127, the client will expect a 16-bit extended payload length field
        extendedPayloadLengthBytes = new byte[2];
        extendedPayloadLengthBytes[1] = (byte)(payloadLength & 0x00FF);
        extendedPayloadLengthBytes[0] = (byte)((payloadLength & 0xFF00)>>8);
        frameLength+=2;
      }else{
        payloadLengthType=PayloadLengthType.EXTENDED_64BIT;
        payloadLengthByte=0x07F; //if payload length byte = 127, the client will expect a 64-bit extended payload length field
        extendedPayloadLengthBytes = new byte[8];
        extendedPayloadLengthBytes[7] = (byte)(payloadLength & 0x00FF);
        extendedPayloadLengthBytes[6] = (byte)((payloadLength>>8) & 0x00FF);
        extendedPayloadLengthBytes[5] = (byte)((payloadLength>>16) & 0x00FF);
        extendedPayloadLengthBytes[4] = (byte)((payloadLength>>24) & 0x00FF);
        extendedPayloadLengthBytes[3] = (byte)((payloadLength>>32) & 0x00FF);
        extendedPayloadLengthBytes[2] = (byte)((payloadLength>>40) & 0x00FF);
        extendedPayloadLengthBytes[1] = (byte)((payloadLength>>48) & 0x00FF);
        extendedPayloadLengthBytes[0] = (byte)((payloadLength>>54) & 0x00FF);
        frameLength+=8;
      }
      //Byte 1 - set FIN to 1, RSV1,2,3  to 0, opcode to 0x01 (text frame)
      //Byte 2 - Mask is 0 - by protocol definition server messages are unmasked, remaining value based on payload length
      b = ByteBuffer.allocate((int)frameLength);
      b.put((byte)0x81);
      b.put(payloadLengthByte);
      if(extendedPayloadLengthBytes!=null){
        for (byte lengthByte:extendedPayloadLengthBytes){
          b.put(lengthByte);
        }
      }
      b.put(textBytes);
      b.rewind();
    }else{
      // Get 'text' into a WebSocket "frame" of bytes
      b = ByteBuffer.allocate(textBytes.length + 2);
      b.put(START_OF_FRAME);
      b.put(textBytes);
      b.put(END_OF_FRAME);
      b.rewind();
    }
    // See if we have any backlog that needs to be sent first
    if (handleWrite()) {
      // Write the ByteBuffer to the socket
      this.socketChannel.write(b);
    }
    // If we didn't get it all sent, add it to the buffer of buffers
    if (b.remaining() > 0) {
      if (!this.bufferQueue.offer(b)) {
        throw new IOException(
            "Buffers are full, message could not be sent to"
                + this.socketChannel.socket()
                    .getRemoteSocketAddress());
      }
      return false;
    }
    return true;
  }

  boolean hasBufferedData() {
    return !this.bufferQueue.isEmpty();
  }

  /**
   * @return True if all data has been sent to the client, false if there is
   *         still some buffered.
   */
  boolean handleWrite() throws IOException {
    synchronized (this.bufferQueueMutex) {
      ByteBuffer buffer = this.bufferQueue.peek();
      while (buffer != null) {
        this.socketChannel.write(buffer);
        if (buffer.remaining() > 0) {
          return false; // Didn't finish this buffer. There's more to
                  // send.
        } else {
          this.bufferQueue.poll(); // Buffer finished. Remove it.
          buffer = this.bufferQueue.peek();
        }
      }
      return true;
    }
  }

  public SocketChannel socketChannel() {
    return this.socketChannel;
  }
  
  private int frame_byte_count;
  private int frame_rsv1;
  private int frame_rsv2;
  private int frame_rsv3;
  private int frame_opcode;
  private boolean frame_last_fragment;
  private boolean frame_masked;
  private byte[] frame_masking_key_buffer;
  private long frame_masking_key;
  private int frame_masking_key_byte_count;
  private long frame_payload_length;
  private boolean frame_payload_length_overflow;
  private StringBuffer frame_payload_text;
  private byte[] frame_extended_payload_length_buffer;
  private enum PayloadLengthType {SHORT_7BIT,EXTENDED_16BIT,EXTENDED_64BIT};
  private PayloadLengthType frame_payload_length_type;
  private enum FrameType {CONTINUATION,TEXT,BINARY,RESERVED,CONNECTION_CLOSE,PING,PONG};
  private FrameType frame_type;
  private long payload_frame_position;
  private String formatByteString(String str){
    while(str.length()<8){
      str = "0"+str;
    }
    return str.substring(0,4)+" "+str.substring(4,8);
  }
  // PRIVATE INSTANCE METHODS ////////////////////////////////////////////////
  private void recieveFrame() {
    byte newestByte = this.buffer.get();
    System.out.println("recieveFrame "+formatByteString(Integer.toString((0x000000FF & ((int)newestByte)),2)));
    if (this.webSocketDraft == WebSocketDraft.VERSION_07) {
      /*DRAFT 07 Frames can arrive in multiple fragments
       * Each fragment */
      if(!readingState){
        frame_byte_count=1;
        frame_masking_key_byte_count=0;
        frame_payload_length=0;
        frame_payload_length_overflow=false;
        frame_masking_key_buffer = new byte[4];
        payload_frame_position=0;
        frame_payload_text = new StringBuffer();
        System.out.println("Start reading for a frame fragment for draft 07");
        /*
         * first byte is FIN,RSV1,RSV2,RSV3,OPCODE(4 bits)
         * */
        if((newestByte & 0x80) == 0x80){
          System.out.println("FIN is set to 1, this is the last fragment");
          frame_last_fragment=true;
        }else{
          System.out.println("FIN is set to 0, there area additional frame fragments");
          frame_last_fragment=false;
        }
        if((newestByte&0x40)==0x40){
          System.out.println("RSV1 is set to 1");
          frame_rsv1=1;
        }else{
          System.out.println("RSV1 is set to 0");
          frame_rsv1=0;
        }
        if((newestByte&0x20)==0x20){
          System.out.println("RSV2 is set to 1");
          frame_rsv2=1;
        }else{
          System.out.println("RSV2 is set to 0");
          frame_rsv2=0;
        }
        if((newestByte&0x10)==0x10){
          System.out.println("RSV3 is set to 1");
          frame_rsv3=1;
        }else{
          System.out.println("RSV3 is set to 0");
          frame_rsv3=0;
        }
        short opcode = (short)(newestByte & 0x0F);
        if(opcode==0x08){
          System.out.println("Opcode: Connection Close");
          frame_type=FrameType.CONNECTION_CLOSE;
        }else if((opcode >=0x03 && opcode <= 0x07)||(opcode >=0x0B && opcode <= 0x0F)){
          System.out.println("Opcode:"+Integer.toString(opcode,16)+" Reserved for Future Use");
          frame_type=FrameType.RESERVED;
        }else if(opcode==0x02){
          System.out.println("Opcode: Binary Frame");
          frame_type=FrameType.BINARY;
        }else if(opcode==0x01){
          System.out.println("Opcode: Text Frame");
          frame_type=FrameType.TEXT;
        }else if(opcode==0x00){
          System.out.println("Opcode: Continuation Frame");
          frame_type=FrameType.CONTINUATION;
        }else if(opcode==0x0A){
          System.out.println("Opcode: Ping");
          frame_type=FrameType.PING;
        }else if(opcode==0x0B){
          System.out.println("Opcode: Pong");
          frame_type=FrameType.PONG;
        }else{
          System.out.println("Opcode: ???? "+Integer.toString(opcode,16));
          frame_type=null;
        }
        readingState=true;
      }else{
        frame_byte_count++;
        System.out.println("Reading byte "+frame_byte_count+" for a frame fragment already in progress for draft 07");
        if(frame_byte_count==2){
          System.out.println("Byte is mask + payload");
          if((newestByte & 0x80)==0x80){
            System.out.println("Payload is masked");
            frame_masked=true;
          }else{
            System.out.println("Payload is not masked");
            frame_masked=false;
          }
          int payloadLength=0x07F&newestByte;
          System.out.println("Payload length = "+payloadLength);
          int payload_length=0x7F&newestByte;
          if(payload_length==126){
            System.out.println("Payload length is extended 16 bit");
            frame_payload_length_type=PayloadLengthType.EXTENDED_16BIT;
            frame_extended_payload_length_buffer = new byte[2];
          }else if(payload_length==127){
            System.out.println("Payload length is extended 64 bit");
            frame_payload_length_type=PayloadLengthType.EXTENDED_64BIT;
            frame_extended_payload_length_buffer = new byte[8];
          }else{
            System.out.println("Final Payload length is "+payloadLength);
            frame_payload_length=payload_length;
            
          }
        }else if(frame_byte_count<=4&&frame_payload_length_type==PayloadLengthType.EXTENDED_16BIT){
          System.out.println("Processing an extended-16 payload length");
          frame_extended_payload_length_buffer[frame_byte_count-3]=newestByte;
          if(frame_byte_count==4){
            System.out.println("Frame payload length complete");
            //process payload length buffer as an unsigned integer
            //cast the bytes to ints so that we don't get negative numbers
            int firstByte=0;
            int secondByte=0;
            firstByte = (0x000000FF & ((int)frame_extended_payload_length_buffer[0]));
            secondByte = (0x000000FF & ((int)frame_extended_payload_length_buffer[1]));
            frame_payload_length = (long)(firstByte<<8|secondByte);
            System.out.println("Payload Length = "+frame_payload_length);
          }
        }else if(frame_byte_count<=10&&frame_payload_length_type==PayloadLengthType.EXTENDED_64BIT){
          System.out.println("Processing an extended-64 payload length");
          frame_extended_payload_length_buffer[frame_byte_count-3]=newestByte;
          if(frame_byte_count==10){
            System.out.println("Frame payload length complete");
            //process payload length buffer as an unsigned integer
            //Problem - java longs are 64-bit signed; the incoming wire data is 64-bit bytes
            //this causes an issue in that we can potentially lose the last byte on a maximum
            //sized frame.  Handle it by processing extra bytes from the payload if the
            //payload length is negative.
            int byte0=(0x000000FF & ((int)frame_extended_payload_length_buffer[0]));
            int byte1=(0x000000FF & ((int)frame_extended_payload_length_buffer[1]));
            int byte2=(0x000000FF & ((int)frame_extended_payload_length_buffer[2]));
            int byte3=(0x000000FF & ((int)frame_extended_payload_length_buffer[3]));
            int byte4=(0x000000FF & ((int)frame_extended_payload_length_buffer[4]));
            int byte5=(0x000000FF & ((int)frame_extended_payload_length_buffer[5]));
            int byte6=(0x000000FF & ((int)frame_extended_payload_length_buffer[6]));
            int byte7=(0x000000FF & ((int)frame_extended_payload_length_buffer[7]));
            frame_payload_length = (long)(byte0<<54|byte1<<48|byte2<<40|byte3<<32|byte4<<24|byte5<<16|byte6<<8|byte7);
            if(frame_payload_length<0){
              frame_payload_length = frame_payload_length*-1;
              frame_payload_length_overflow=true;
              System.out.println("Frame payload length (extended-64bit) overflow!  TODO: HANDLE OVERFLOW CASE");
            }
          }
        }else if(frame_masked&&frame_masking_key_byte_count<4){
          System.out.println("Setting frame masking key buffer byte "+frame_masking_key_byte_count);
          frame_masking_key_buffer[frame_masking_key_byte_count++]=newestByte;
          if(frame_masking_key_byte_count==4){ //end of masking key
            System.out.println("Finished processing frame masking key");
            long byte0=(0x000000FF & ((int)frame_masking_key_buffer[0]));
            int byte1=(0x000000FF & ((int)frame_masking_key_buffer[1]));
            int byte2=(0x000000FF & ((int)frame_masking_key_buffer[2]));
            int byte3=(0x000000FF & ((int)frame_masking_key_buffer[3]));
            frame_masking_key=(long)(byte0<<24|byte1<<16|byte2<<8|byte3);
            System.out.println("Masking key:  "+Long.toString(frame_masking_key,2));
          }
        }else if (frame_masked){
          System.out.println("Processing masked payload data "+newestByte);
          int mask_byte_index = (int) (payload_frame_position%4);
          System.out.println("Applying mask transformation for index "+mask_byte_index+" to byte");
          byte transformedByte = (byte) (newestByte ^ frame_masking_key_buffer[mask_byte_index]);
          System.out.println("Transformed byte: "+(char)transformedByte);
          frame_payload_text.append(new String(new byte[] {transformedByte},UTF8_CHARSET));
          payload_frame_position++;
          if(frame_payload_text.length()==frame_payload_length){
            System.out.println("End of text frame.  Payload Data: "+frame_payload_text.toString());
            readingState=false;
            String frame_text = frame_payload_text.toString();
            frame_payload_text=null;
            this.wsl.onMessage(this, frame_text);
          }
        }else{
          System.out.println("Processing unmasked payload data "+newestByte);
          frame_payload_text.append(new String(new byte[] {newestByte},UTF8_CHARSET));
          payload_frame_position++;
        }
      }
    } else {
      if (newestByte == START_OF_FRAME && !readingState) { // Beginning of Frame
        System.out.println("frame start");
        this.currentFrame = null;
        readingState = true;
      } else if (newestByte == END_OF_FRAME && readingState) { // End of Frame
        System.out.println("frame end");
        readingState = false;
        String textFrame = null;
        // currentFrame will be null if END_OF_FRAME was send directly after
        // START_OF_FRAME, thus we will send 'null' as the sent message.
        if (this.currentFrame != null) {
          textFrame = new String(this.currentFrame.array(),
              UTF8_CHARSET);
        }
        this.wsl.onMessage(this, textFrame);
      } else { // Regular frame data, add to current frame buffer
        ByteBuffer frame = ByteBuffer
            .allocate((this.currentFrame != null ? this.currentFrame
                .capacity() : 0)
                + this.buffer.capacity());
        if (this.currentFrame != null) {
          this.currentFrame.rewind();
          frame.put(this.currentFrame);
        }
        frame.put(newestByte);
        this.currentFrame = frame;
        System.out.println(frame);
        System.out.println(new BigInteger(frame.array()).toString(2));
        System.out.println();
      }
    }
  }

  private void recieveHandshake() throws IOException,
      NoSuchAlgorithmException {
    ByteBuffer ch = ByteBuffer
        .allocate((this.remoteHandshake != null ? this.remoteHandshake
            .capacity() : 0) + this.buffer.capacity());
    if (this.remoteHandshake != null) {
      this.remoteHandshake.rewind();
      ch.put(this.remoteHandshake);
    }
    ch.put(this.buffer);
    this.remoteHandshake = ch;
    byte[] h = this.remoteHandshake.array();
    System.out.println(new String(h));
    String handshake = new String(this.remoteHandshake.array(),
        UTF8_CHARSET);
    // If the ByteBuffer contains 16 random bytes, and ends with
    // 0x0D 0x0A 0x0D 0x0A (or two CRLFs), then the client
    // handshake is complete for Draft 76 Client.
    if ((h.length >= 20 && h[h.length - 20] == CR && h[h.length - 19] == LF
        && h[h.length - 18] == CR && h[h.length - 17] == LF)) {
      completeHandshake(new byte[] { h[h.length - 16], h[h.length - 15],
          h[h.length - 14], h[h.length - 13], h[h.length - 12],
          h[h.length - 11], h[h.length - 10], h[h.length - 9],
          h[h.length - 8], h[h.length - 7], h[h.length - 6],
          h[h.length - 5], h[h.length - 4], h[h.length - 3],
          h[h.length - 2], h[h.length - 1] });

      // If the ByteBuffer contains 8 random bytes,ends with
      // 0x0D 0x0A 0x0D 0x0A (or two CRLFs), and the response
      // contains Sec-WebSocket-Key1 then the client
      // handshake is complete for Draft 76 Server.
    } else if ((h.length >= 12 && h[h.length - 12] == CR
        && h[h.length - 11] == LF && h[h.length - 10] == CR && h[h.length - 9] == LF)
        && new String(this.remoteHandshake.array(), UTF8_CHARSET)
            .contains("Sec-WebSocket-Key1")) {
      completeHandshake(new byte[] { h[h.length - 8], h[h.length - 7],
          h[h.length - 6], h[h.length - 5], h[h.length - 4],
          h[h.length - 3], h[h.length - 2], h[h.length - 1] });

      // Consider Draft 75, and the Flash Security Policy
      // Request edge-case.
    } else if ((h.length >= 4 && h[h.length - 4] == CR
        && h[h.length - 3] == LF && h[h.length - 2] == CR && h[h.length - 1] == LF)
        && !(new String(this.remoteHandshake.array(), UTF8_CHARSET)
            .contains("Sec"))
        || (h.length == 23 && h[h.length - 1] == 0)) {
      completeHandshake(null);
    } else if ((h.length >= 4 && h[h.length - 4] == CR
        && h[h.length - 3] == LF && h[h.length - 2] == CR && h[h.length - 1] == LF)
        && handshake.contains("Sec-WebSocket-Version")
        && handshake.contains("Sec-WebSocket-Origin")
        && handshake.contains("Sec-WebSocket-Key")) {
      completeHandshake(h);
    }
  }

  private void completeHandshake(byte[] handShakeBody) throws IOException,
      NoSuchAlgorithmException {
    byte[] handshakeBytes = this.remoteHandshake.array();
    String handshake = new String(handshakeBytes, UTF8_CHARSET);
    this.handshakeComplete = true;
    if (this.wsl.onHandshakeRecieved(this, handshake, handShakeBody)) {
      this.wsl.onOpen(this);
    } else {
      close();
    }
  }

  public WebSocketDraft getWebSocketDraft() {
    return webSocketDraft;
  }

  public void setWebSocketDraft(WebSocketDraft websocketDraft) {
    this.webSocketDraft = websocketDraft;
  }

}