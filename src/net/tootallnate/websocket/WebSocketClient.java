package net.tootallnate.websocket;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import net.tootallnate.websocket.drafts.Draft_10;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;

/**
 * The <tt>WebSocketClient</tt> is an abstract class that expects a valid
 * "ws://" URI to connect to. When connected, an instance recieves important
 * events related to the life of the connection. A subclass must implement
 * <var>onOpen</var>, <var>onClose</var>, and <var>onMessage</var> to be
 * useful. An instance can send messages to it's connected server via the
 * <var>send</var> method.
 * @author Nathan Rajlich
 */
public abstract class WebSocketClient extends WebSocketAdapter implements  Runnable {


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
  private Draft draft = null;

  // CONSTRUCTORS ////////////////////////////////////////////////////////////
  public WebSocketClient(URI serverURI) {
    this(serverURI, new Draft_10() );
  }
  
  public WebSocketClient( Draft draft , URI serverURI ) {
  	this(serverURI, draft );
  }

  /**
   * Constructs a WebSocketClient instance and sets it to the connect to the
   * specified URI. The client does not attampt to connect automatically. You
   * must call <var>connect</var> first to initiate the socket connection.
   * @param serverUri The <tt>URI</tt> of the WebSocket server to connect to.
   * @throws IllegalArgumentException If <var>draft</var>
   * is <code>Draft.AUTO</code>
   */
  public WebSocketClient(URI serverUri, Draft draft) {
    this.uri = serverUri;
    if (draft == null) {
      throw new IllegalArgumentException("null is permitted for `WebSocketServer` only!");
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
  
  public Draft getDraft() {
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
   */
  public void close() 
  {    
	  if (running)
	  {
		  // must be called to stop do loop
		  running = false;  
		  
		  // call this inside IF because it can be null if the connection has't started
		  // but user is calling close()
		  if (selector != null && conn != null)
		  {
			  selector.wakeup();
			  conn.close();
			  // close() is synchronously calling onClose(conn) so we don't have to
		  }
		  else
		  {
			  // connection has't started but the onClose events should be triggered
			  onClose(conn);
		  }
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
  private void releaseAndInitialize()
  {
	  conn = null;
	  client = null;
	  selector = null;
	  running = false;
	  draft = null;
  }
  
  private boolean tryToConnect(InetSocketAddress remote) {
    // The WebSocket constructor expects a SocketChannel that is
    // non-blocking, and has a Selector attached to it.
    try {
      client = SocketChannel.open();
      client.configureBlocking(false);
      client.connect(remote);

      selector = Selector.open();

      this.conn = new WebSocket( client , new LinkedBlockingQueue<ByteBuffer>() , this , draft , Integer.MAX_VALUE );
      // the client/selector can be null when closing the connection before its start
      // so we have to call this part inside IF
      if (client != null)
      {
          // At first, we're only interested in the 'CONNECT' keys.
          client.register(selector, SelectionKey.OP_CONNECT);
      }

    } catch (IOException ex) {
    	onError(conn, ex);
      return false;
    }
    
    return true;
  }

  // Runnable IMPLEMENTATION /////////////////////////////////////////////////
  public void run() {
    try {
		running = tryToConnect( new InetSocketAddress( uri.getHost() , getPort() ) );
	} catch ( IllegalArgumentException e ) {//
		onError(conn, e);
		conn.close();
		return;
	}
	catch (SecurityException e) {
		onError(conn, e);
		conn.close();
		return;
	}

    while (this.running) {
      SelectionKey key = null;
      try {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> i = keys.iterator();

        while (i.hasNext()) {
          key = i.next();
          i.remove();

          if (key.isConnectable()) {
            finishConnect();
          }

          if (key.isReadable()) {
            conn.handleRead();
          }
        }
      } catch (InvalidHandshakeException e) {
    	  onError( e );
    	  close();
    	  return;
      }
      catch (IOException e) {
    	  //if(e instanceof ConnectException == false)
    	  onError( e );
    	  close();
    	  return;
      }
    }
  }
  
  private int getPort() {
    int port = uri.getPort();
    return port == -1 ? WebSocket.DEFAULT_PORT : port;
  }
  
  private void finishConnect() throws IOException, InvalidHandshakeException {
    if (client.isConnectionPending()) {
      client.finishConnect();
    }

    // Now that we're connected, re-register for only 'READ' keys.
    client.register(selector, SelectionKey.OP_READ);

    sendHandshake();
  }
  
  private void sendHandshake() throws IOException, InvalidHandshakeException {
	String path;
	String part1 = uri.getPath();
	String part2 = uri.getQuery();
	if( part1 != null )
		path = part1;
	else
		path = "/";
     if( part2 != null)
    	 path += "?" + part2;
    int port = getPort();
    String host = uri.getHost() + (port != WebSocket.DEFAULT_PORT ? ":" + port : "");

    HandshakedataImpl1 handshake = new HandshakedataImpl1();
    handshake.setResourceDescriptor ( path );
    handshake.put ( "Host" , host );
    //handshake.put ( "Origin" , origin );
    conn.startHandshake ( handshake );
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
	  onClose();
	  releaseAndInitialize();
  }

  /**
   * Calls subclass' implementation of <var>onIOError</var>.
   * @param conn
   */
  public void onError(WebSocket conn, IOException ex) 
  {
	  releaseAndInitialize();
	  onError(ex);
  }

  // ABTRACT METHODS /////////////////////////////////////////////////////////
  public abstract void onMessage(String message);
  public abstract void onOpen();
  public abstract void onClose();
  public abstract void onError( Exception ex);
}
