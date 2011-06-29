package net.tootallnate.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The <tt>WebSocketClient</tt> is an abstract class that expects a valid
 * "ws://" URI to connect to. When connected, an instance recieves important
 * events related to the life of the connection. A subclass must implement
 * <var>onOpen</var>, <var>onClose</var>, and <var>onMessage</var> to be
 * useful. An instance can send messages to it's connected server via the
 * <var>send</var> method.
 * @author Nathan Rajlich
 */
public abstract class WebSocketClient implements Runnable, WebSocketListener {


  // INSTANCE PROPERTIES /////////////////////////////////////////////////////
  /**
   * The URI this client is supposed to connect to.
   */
  private URI uri = null;
  /**
   * The WebSocket instance this client object wraps.
   */
  private WebSocket conn = null;
  /**
   * The SocketChannel instance this client uses.
   */
  private SocketChannel client = null;
  /**
   * The 'Selector' used to get event keys from the underlying socket.
   */
  private Selector selector = null;
  /**
   * Keeps track of whether or not the client thread should continue running.
   */
  private boolean running = false;
  /**
   * The Draft of the WebSocket protocol the Client is adhering to.
   */
  private WebSocketDraft draft = null;
  /**
   * Number 1 used in handshake 
   */
  private int number1 = 0;
  /**
   * Number 2 used in handshake
   */
  private int number2 = 0;
  /**
   * Key3 used in handshake
   */
  private byte[] key3 = null;

  // CONSTRUCTORS ////////////////////////////////////////////////////////////
  public WebSocketClient(URI serverURI) {
    this(serverURI, WebSocketDraft.DRAFT76);
  }

  /**
   * Constructs a WebSocketClient instance and sets it to the connect to the
   * specified URI. The client does not attampt to connect automatically. You
   * must call <var>connect</var> first to initiate the socket connection.
   * @param serverUri The <tt>URI</tt> of the WebSocket server to connect to.
   * @throws IllegalArgumentException If <var>draft</var>
   * is <code>WebSocketDraft.AUTO</code>
   */
  public WebSocketClient(URI serverUri, WebSocketDraft draft) {
    this.uri = serverUri;
    if (draft == WebSocketDraft.AUTO) {
      throw new IllegalArgumentException(draft + " is meant for `WebSocketServer` only!");
    }
    this.draft = draft;
  }

  // PUBLIC INSTANCE METHODS /////////////////////////////////////////////////
  /**
   * Gets the URI that this WebSocketClient is connected to.
   * @return The <tt>URI</tt> for this WebSocketClient.
   */
  public URI getURI() {
    return uri;
  }
  
  @Override
  public WebSocketDraft getDraft() {
    return draft;
  }

  /**
   * Starts a background thread that attempts and maintains a WebSocket
   * connection to the URI specified in the constructor or via <var>setURI</var>.
   * <var>setURI</var>.
   */
  public void connect() {
    if (!running) {
      this.running = true;
      (new Thread(this)).start();
    }
  }

  /**
   * Calls <var>close</var> on the underlying SocketChannel, which in turn
   * closes the socket connection, and ends the client socket thread.
   * @throws IOException When socket related I/O errors occur.
   */
  public void close() throws IOException 
  {    
	  if (running)
	  {
		  selector.wakeup();
		  conn.close();
	  }
  }

  /**
   * Sends <var>text</var> to the connected WebSocket server.
   * @param text The String to send to the WebSocket server.
   * @throws IOException When socket related I/O errors occur.
   */
  public void send(String text) throws IOException {
    if (conn != null) 
    {
      conn.send(text);
    }
  }
  
  /**
   * Reinitializes and prepares the class to be used for reconnect.
   * @return
   */
  public void releaseAndInitialize()
  {
	  conn = null;
	  client = null;
	  selector = null;
	  running = false;
	  draft = null;
	  number1 = 0;
	  number2 = 0;
	  key3 = null;
  }
  
  private boolean tryToConnect(InetSocketAddress remote) {
    // The WebSocket constructor expects a SocketChannel that is
    // non-blocking, and has a Selector attached to it.
    try {
      client = SocketChannel.open();
      client.configureBlocking(false);
      client.connect(remote);

      selector = Selector.open();

      this.conn = new WebSocket(client, new LinkedBlockingQueue<ByteBuffer>(), this);
      // At first, we're only interested in the 'CONNECT' keys.
      client.register(selector, SelectionKey.OP_CONNECT);

    } catch (IOException ex) {
    	onIOError(conn, ex);
      return false;
    }
    
    return true;
  }

  // Runnable IMPLEMENTATION /////////////////////////////////////////////////
  public void run() {
    running = tryToConnect(new InetSocketAddress(uri.getHost(), getPort()));

    while (this.running) {
      try {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> i = keys.iterator();

        while (i.hasNext()) {
          SelectionKey key = i.next();
          i.remove();

          if (key.isConnectable()) {
            finishConnect();
          }

          if (key.isReadable()) {
            conn.handleRead();
          }
        }
      } catch (IOException ex) {
    	  onIOError(conn, ex);
      } catch (NoSuchAlgorithmException ex) {
        ex.printStackTrace();
      }
    }
    
    //System.err.println("WebSocketClient thread ended!");
  }
  
  private int getPort() {
    int port = uri.getPort();
    return port == -1 ? WebSocket.DEFAULT_PORT : port;
  }
  
  private void finishConnect() throws IOException {
    if (client.isConnectionPending()) {
      client.finishConnect();
    }

    // Now that we're connected, re-register for only 'READ' keys.
    client.register(selector, SelectionKey.OP_READ);

    sendHandshake();
  }
  
  private void sendHandshake() throws IOException {
    String path = uri.getPath();
    if (path.indexOf("/") != 0) {
      path = "/" + path;
    }
    int port = getPort();
    String host = uri.getHost() + (port != WebSocket.DEFAULT_PORT ? ":" + port : "");
    String origin = null; // TODO: Make 'origin' configurable
    String request = "GET " + path + " HTTP/1.1\r\n" +
                      "Upgrade: WebSocket\r\n" +
                      "Connection: Upgrade\r\n" +
                      "Host: " + host + "\r\n" +
                      "Origin: " + origin + "\r\n";
    
    if (this.draft == WebSocketDraft.DRAFT76) {
      request += "Sec-WebSocket-Key1: " + this.generateKey() + "\r\n";
      request += "Sec-WebSocket-Key2: " + this.generateKey() + "\r\n";
      this.key3 = new byte[8];
      (new Random()).nextBytes(this.key3);
    }
    
    request += "\r\n";
    
    if (this.key3 != null) {
      conn.socketChannel().write(new ByteBuffer[] {
        ByteBuffer.wrap(request.getBytes(WebSocket.UTF8_CHARSET)),
        ByteBuffer.wrap(this.key3)
      });
    }
    else
    {
      conn.socketChannel().write(ByteBuffer.wrap(request.getBytes(WebSocket.UTF8_CHARSET)));
    }
  }

  private String generateKey() {
    Random r = new Random();
    long maxNumber = 4294967295L;
    long spaces = r.nextInt(12) + 1;
    int max = new Long(maxNumber / spaces).intValue();
    max = Math.abs(max);
    int number = r.nextInt(max) + 1;
    if (this.number1 == 0) {
      this.number1 = number;
    }
    else {
      this.number2 = number;
    }
    long product = number * spaces;
    String key = Long.toString(product);
    //always insert atleast one random character
    int numChars = r.nextInt(12)+1;
    for (int i=0; i < numChars; i++){
      int position = r.nextInt(key.length());
      position = Math.abs(position);
      char randChar = (char)(r.nextInt(95) + 33);
      //exclude numbers here
      if(randChar >= 48 && randChar <= 57){
        randChar -= 15;
      }
      key = new StringBuilder(key).insert(position, randChar).toString();
    }
    for (int i = 0; i < spaces; i++){
      int position = r.nextInt(key.length() - 1) + 1;
      position = Math.abs(position);
      key = new StringBuilder(key).insert(position,"\u0020").toString();
    }
    return key;
  }

  // WebSocketListener IMPLEMENTATION ////////////////////////////////////////
  /**
   * Parses the server's handshake to verify that it's a valid WebSocket
   * handshake.
   * @param conn The {@link WebSocket} instance who's handshake has been recieved.
   *             In the case of <tt>WebSocketClient</tt>, this.conn == conn.
   * @param handshake The entire UTF-8 decoded handshake from the connection.
   * @return <var>true</var> if <var>handshake</var> is a valid WebSocket server
   *         handshake, <var>false</var> otherwise.
   * @throws IOException When socket related I/O errors occur.
   * @throws NoSuchAlgorithmException 
   */
  public boolean onHandshakeRecieved(WebSocket conn, String handshake, byte[] reply) throws IOException, NoSuchAlgorithmException {
    // TODO: Do some parsing of the returned handshake, and close connection
    // (return false) if we recieved anything unexpected.
    if(this.draft == WebSocketDraft.DRAFT76) {
      if (reply == null) {
        return false;
      }
      byte[] challenge = new byte[] {
        (byte)( this.number1 >> 24 ),
        (byte)( (this.number1 << 8) >> 24 ),
        (byte)( (this.number1 << 16) >> 24 ),
        (byte)( (this.number1 << 24) >> 24 ),
        (byte)(  this.number2 >> 24 ),
        (byte)( (this.number2 << 8) >> 24 ),
        (byte)( (this.number2 << 16) >> 24 ),
        (byte)( (this.number2 << 24) >> 24 ),
        this.key3[0],
        this.key3[1],
        this.key3[2],
        this.key3[3],
        this.key3[4],
        this.key3[5],
        this.key3[6],
        this.key3[7]
      };
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] expected = md5.digest(challenge);
      for (int i = 0; i < reply.length; i++) {
        if (expected[i] != reply[i]) {
          return false;
        }
      } 
    }
    return true;
  }

  /**
   * Calls subclass' implementation of <var>onMessage</var>.
   * @param conn
   * @param message
   */
  public void onMessage(WebSocket conn, String message) {
    onMessage(message);
  }

  /**
   * Calls subclass' implementation of <var>onOpen</var>.
   * @param conn
   */
  public void onOpen(WebSocket conn) {
    onOpen();
  }

  /**
   * Calls subclass' implementation of <var>onClose</var>.
   * @param conn
   */
  public void onClose(WebSocket conn) 
  {
	  if (running)
	  {
		  onClose();
	  }
	  
	  releaseAndInitialize();
  }

  /**
   * Calls subclass' implementation of <var>onIOError</var>.
   * @param conn
   */
  public void onIOError(WebSocket conn, IOException ex) 
  {
	  releaseAndInitialize();
	  onIOError(ex);
  }

  // ABTRACT METHODS /////////////////////////////////////////////////////////
  public abstract void onMessage(String message);
  public abstract void onOpen();
  public abstract void onClose();
  public abstract void onIOError(IOException ex);
}
