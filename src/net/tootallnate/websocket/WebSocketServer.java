package net.tootallnate.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <tt>WebSocketServer</tt> is an abstract class that only takes care of the
 * HTTP handshake portion of WebSockets. It's up to a subclass to add
 * functionality/purpose to the server.
 * @author Nathan Rajlich
 */
public abstract class WebSocketServer extends WebSocketAdapter implements Runnable {

  // INSTANCE PROPERTIES /////////////////////////////////////////////////////
  /**
   * Holds the list of active WebSocket connections. "Active" means WebSocket
   * handshake is complete and socket can be written to, or read from.
   */
  private final CopyOnWriteArraySet<WebSocket> connections;
  /**
   * The port number that this WebSocket server should listen on. Default is
   * WebSocket.DEFAULT_PORT.
   */
  private int port;
  /**
   * The socket channel for this WebSocket server.
   */
  private ServerSocketChannel server;
  /**
   * The 'Selector' used to get event keys from the underlying socket.
   */
  private Selector selector;
  /**
   * The Draft of the WebSocket protocol the Server is adhering to.
   */
  private Draft draft;
  
  private Thread thread;


  // CONSTRUCTORS ////////////////////////////////////////////////////////////
  /**
   * Nullary constructor. Creates a WebSocketServer that will attempt to
   * listen on port WebSocket.DEFAULT_PORT.
   */
  public WebSocketServer() {
    this(WebSocket.DEFAULT_PORT, null );
  }
  
  /**
   * Creates a WebSocketServer that will attempt to listen on port
   * <var>port</var>.
   * @param port The port number this server should listen on.
   */
  public WebSocketServer(int port) {
    this(port, null );
  }

  /**
   * Creates a WebSocketServer that will attempt to listen on port <var>port</var>,
   * and comply with <tt>Draft</tt> version <var>draft</var>.
   * @param port The port number this server should listen on.
   * @param draft The version of the WebSocket protocol that this server
   *              instance should comply to.
   */
  public WebSocketServer(int port, Draft draft) {
    this.connections = new CopyOnWriteArraySet<WebSocket>();
    this.draft = draft;
    setPort(port);
  }

  /**
   * Starts the server thread that binds to the currently set port number and
   * listeners for WebSocket connection requests.
   */
  public void start() throws IllegalStateException {
	if( thread != null )
		throw new IllegalStateException("Already started");
    new Thread(this).start();
  }

  /**
   * Closes all connected clients sockets, then closes the underlying
   * ServerSocketChannel, effectively killing the server socket thread and
   * freeing the port the server was bound to.
   * @throws IOException When socket related I/O errors occur.
   */
  public void stop() throws IOException {
    for (WebSocket ws : connections) {
      ws.close();
    }
    thread.interrupt();
    this.server.close();
    
  }

  /**
   * Sends <var>text</var> to all currently connected WebSocket clients.
   * @param text The String to send across the network.
   * @throws IOException When socket related I/O errors occur.
   */
  public void sendToAll(String text) throws IOException {
    for (WebSocket c : this.connections) {
      c.send(text);
    }
  }

  /**
   * Sends <var>text</var> to all currently connected WebSocket clients,
   * except for the specified <var>connection</var>.
   * @param connection The {@link WebSocket} connection to ignore.
   * @param text The String to send to every connection except <var>connection</var>.
   * @throws IOException When socket related I/O errors occur.
   */
  public void sendToAllExcept(WebSocket connection, String text) throws IOException {
    if (connection == null) {
      throw new NullPointerException("'connection' cannot be null");
    }
    
    for (WebSocket c : this.connections) {
      if (!connection.equals(c)) {
        c.send(text);
      }
    }
  }

  /**
   * Sends <var>text</var> to all currently connected WebSocket clients,
   * except for those found in the Set <var>connections</var>.
   * @param connections
   * @param text
   * @throws IOException When socket related I/O errors occur.
   */
  public void sendToAllExcept(Set<WebSocket> connections, String text) throws IOException {
    if (connections == null) {
      throw new NullPointerException("'connections' cannot be null");
    }
    
    for (WebSocket c : this.connections) {
      if (!connections.contains(c)) {
        c.send(text);
      }
    }
  }

  /**
   * Returns a WebSocket[] of currently connected clients.
   * @return The currently connected clients in a WebSocket[].
   */
  public WebSocket[] connections() {
    return this.connections.toArray(new WebSocket[0]);
  }

  /**
   * Sets the port that this WebSocketServer should listen on.
   * @param port The port number to listen on.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Gets the port number that this server listens on.
   * @return The port number.
   */
  public int getPort() {
    return this.port;
  }

  public Draft getDraft() {
    return this.draft;
  }


  // Runnable IMPLEMENTATION /////////////////////////////////////////////////
  public void run() {
	if( thread != null )
		throw new IllegalStateException( "This instance of "+getClass().getSimpleName()+" can only be started once the same time." );
	thread = Thread.currentThread();
	try {
      server = ServerSocketChannel.open();
      server.configureBlocking(false);
      server.socket().bind(new java.net.InetSocketAddress(port));

      selector = Selector.open();
      server.register(selector, server.validOps());
    } catch (IOException ex) {
    	onError( null , ex );
    	return;
    }

    while( !thread.isInterrupted() ) {
      SelectionKey key = null;
      WebSocket conn = null;
      try {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> i = keys.iterator();

        while(i.hasNext()) {
          key = i.next();

          // Remove the current key
          i.remove();

          // if isAcceptable == true
          // then a client required a connection
          if (key.isAcceptable()) {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            WebSocket c = new WebSocket(client, new LinkedBlockingQueue<ByteBuffer>(), this, Collections.singletonList ( draft ) , Integer.MAX_VALUE );
            client.register(selector, SelectionKey.OP_READ, c);
          }

          // if isReadable == true
          // then the server is ready to read
          if (key.isReadable()) {
            conn = (WebSocket)key.attachment();
            conn.handleRead();
          }

          // if isWritable == true
          // then we need to send the rest of the data to the client
          if (key.isValid() && key.isWritable()) {
            conn = (WebSocket)key.attachment();
            if (conn.handleWrite()) {
              conn.socketChannel().register(selector,
                  SelectionKey.OP_READ, conn);
            }
          }
        }
        Iterator<WebSocket> it = this.connections.iterator();
        while ( it.hasNext() ) {
          // We have to do this check here, and not in the thread that
          // adds the buffered data to the WebSocket, because the
          // Selector is not thread-safe, and can only be accessed
          // by this thread.
          conn = it.next();
          if (conn.hasBufferedData()) {
            conn.socketChannel().register(selector,
                SelectionKey.OP_READ | SelectionKey.OP_WRITE, conn);
          }
        }
      } catch (IOException ex) {
          if( key != null )
              key.cancel();
          onError( conn , ex );//conn may be null here
      }
    }
  }

  /**
   * Gets the XML string that should be returned if a client requests a Flash
   * security policy.
   *
   * The default implementation allows access from all remote domains, but
   * only on the port that this WebSocketServer is listening on.
   *
   * This is specifically implemented for gitime's WebSocket client for Flash:
   *     http://github.com/gimite/web-socket-js
   *
   * @return An XML String that comforms to Flash's security policy. You MUST
   *         not include the null char at the end, it is appended automatically.
   */
  protected String getFlashSecurityPolicy() {
    return "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\""
              + getPort() + "\" /></cross-domain-policy>";
  }

  public void onMessage(WebSocket conn, String message) {
    onClientMessage(conn, message);
  }

  public void onOpen(WebSocket conn) {
    if (this.connections.add(conn)) {
      onClientOpen(conn);
    }
  }

  public void onClose(WebSocket conn) {
    if (this.connections.remove(conn)) {
      onClientClose(conn);
    }
  }

  // ABTRACT METHODS /////////////////////////////////////////////////////////
  public abstract void onClientOpen(WebSocket conn);
  public abstract void onClientClose(WebSocket conn);
  public abstract void onClientMessage(WebSocket conn, String message);
  /** @param conn may be null if the error does not belong to a single connection */
  public abstract void onError(WebSocket conn,Exception ex);

}
