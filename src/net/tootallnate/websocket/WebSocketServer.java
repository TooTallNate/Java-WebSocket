package net.tootallnate.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <tt>WebSocketServer</tt> is an abstract class that only takes care of the
 * HTTP handshake portion of WebSockets. It's up to a subclass to add
 * functionality/purpose to the server.
 * @author Nathan Rajlich
 */
public abstract class WebSocketServer implements Runnable, WebSocketListener {


  // CONSTANTS ///////////////////////////////////////////////////////////////
  /**
   * The value of <var>handshake</var> when a Flash client requests a policy
   * file on this server.
   */
  private static final String FLASH_POLICY_REQUEST = "<policy-file-request/>\0";


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
  private WebSocketDraft draft;


  // CONSTRUCTORS ////////////////////////////////////////////////////////////
  /**
   * Nullary constructor. Creates a WebSocketServer that will attempt to
   * listen on port WebSocket.DEFAULT_PORT.
   */
  public WebSocketServer() {
    this(WebSocket.DEFAULT_PORT, WebSocketDraft.AUTO);
  }
  
  /**
   * Creates a WebSocketServer that will attempt to listen on port
   * <var>port</var>.
   * @param port The port number this server should listen on.
   */
  public WebSocketServer(int port) {
    this(port, WebSocketDraft.AUTO);
  }

  /**
   * Creates a WebSocketServer that will attempt to listen on port <var>port</var>,
   * and comply with <tt>WebSocketDraft</tt> version <var>draft</var>.
   * @param port The port number this server should listen on.
   * @param draft The version of the WebSocket protocol that this server
   *              instance should comply to.
   */
  public WebSocketServer(int port, WebSocketDraft draft) {
    this.connections = new CopyOnWriteArraySet<WebSocket>();
    this.draft = draft;
    setPort(port);
  }

  /**
   * Starts the server thread that binds to the currently set port number and
   * listeners for WebSocket connection requests.
   */
  public void start() {
    (new Thread(this)).start();
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

  public WebSocketDraft getDraft() {
    return this.draft;
  }


  // Runnable IMPLEMENTATION /////////////////////////////////////////////////
  public void run() {
    try {
      server = ServerSocketChannel.open();
      server.configureBlocking(false);
      server.socket().bind(new java.net.InetSocketAddress(port));

      selector = Selector.open();
      server.register(selector, server.validOps());
    } catch (IOException ex) {
      ex.printStackTrace();
      return;
    }

    while(true) {
      try {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> i = keys.iterator();

        while(i.hasNext()) {
          SelectionKey key = i.next();

          // Remove the current key
          i.remove();

          // if isAcceptable == true
          // then a client required a connection
          if (key.isAcceptable()) {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            WebSocket c = new WebSocket(client, new LinkedBlockingQueue<ByteBuffer>(), this);
            client.register(selector, SelectionKey.OP_READ, c);
          }

          // if isReadable == true
          // then the server is ready to read
          if (key.isReadable()) {
            WebSocket conn = (WebSocket)key.attachment();
            conn.handleRead();
          }

          // if isWritable == true
          // then we need to send the rest of the data to the client
          if (key.isValid() && key.isWritable()) {
            WebSocket conn = (WebSocket)key.attachment();
            if (conn.handleWrite()) {
              conn.socketChannel().register(selector,
                  SelectionKey.OP_READ, conn);
            }
          }
        }

        for (WebSocket conn : this.connections) {
          // We have to do this check here, and not in the thread that
          // adds the buffered data to the WebSocket, because the
          // Selector is not thread-safe, and can only be accessed
          // by this thread.
          if (conn.hasBufferedData()) {
            conn.socketChannel().register(selector,
                SelectionKey.OP_READ | SelectionKey.OP_WRITE, conn);
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (RuntimeException ex) {
        ex.printStackTrace();
      } catch (NoSuchAlgorithmException ex) {
        ex.printStackTrace();
      }
    }
    
    //System.err.println("WebSocketServer thread ended!");
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


  // WebSocketListener IMPLEMENTATION ////////////////////////////////////////
  /**
   * Called by a {@link WebSocket} instance when a client connection has
   * finished sending a handshake. This method verifies that the handshake is
   * a valid WebSocket cliend request. Then sends a WebSocket server handshake
   * if it is valid, or closes the connection if it is not.
   * @param conn The {@link WebSocket} instance who's handshake has been recieved.
   * @param handshake The entire UTF-8 decoded handshake from the connection.
   * @return True if the client sent a valid WebSocket handshake and this server
   *         successfully sent a WebSocket server handshake, false otherwise.
   * @throws IOException When socket related I/O errors occur.
   * @throws NoSuchAlgorithmException 
   */
  public boolean onHandshakeRecieved(WebSocket conn, String handshake, byte[] key3) throws IOException, NoSuchAlgorithmException {
    
    // If a Flash client requested the Policy File...
    if (FLASH_POLICY_REQUEST.equals(handshake)) {
      String policy = getFlashSecurityPolicy() + "\0";
      conn.socketChannel().write(ByteBuffer.wrap(policy.getBytes(WebSocket.UTF8_CHARSET)));
      return false;
    }
    
    String[] requestLines = handshake.split("\r\n");
    boolean isWebSocketRequest = true;
    String line = requestLines[0].trim();
    String path = null;
    if (!(line.startsWith("GET") && line.endsWith("HTTP/1.1"))) {
      isWebSocketRequest = false;
    } else {
      String[] firstLineTokens = line.split(" ");
      path = firstLineTokens[1];
    }
    
    // 'p' will hold the HTTP headers
    Properties p = new Properties();
    for (int i = 1; i < requestLines.length; i++) {
      line = requestLines[i];
      int firstColon = line.indexOf(":");
      if (firstColon != -1) {
        p.setProperty(line.substring(0, firstColon).trim(), line.substring(firstColon+1).trim());
      }
    }
    String prop = p.getProperty("Upgrade");
    if (prop == null || !prop.equals("WebSocket")) {
      isWebSocketRequest = false;
    }
    prop = p.getProperty("Connection");
    if (prop == null || !prop.equals("Upgrade")) {
      isWebSocketRequest = false;
    }
    String key1 = p.getProperty("Sec-WebSocket-Key1");
    String key2 = p.getProperty("Sec-WebSocket-Key2");
    String headerPrefix = "";
    byte[] responseChallenge = null;
    switch (this.draft) {
      case DRAFT75:
        if (key1 != null || key2 != null || key3 != null) {
          isWebSocketRequest = false;
        }
        break;
      case DRAFT76:
        if (key1 == null || key2 == null || key3 == null) {
          isWebSocketRequest = false;
        }
        break;  
    }
    if (isWebSocketRequest) {
      if (key1 != null && key2 != null && key3 != null) {
        headerPrefix = "Sec-";
        byte[] part1 = this.getPart(key1);
        byte[] part2 = this.getPart(key2);
        byte[] challenge = new byte[16];
        challenge[0] = part1[0];
        challenge[1] = part1[1];
        challenge[2] = part1[2];
        challenge[3] = part1[3];
        challenge[4] = part2[0];
        challenge[5] = part2[1];
        challenge[6] = part2[2];
        challenge[7] = part2[3];
        challenge[8] = key3[0];
        challenge[9] = key3[1];
        challenge[10] = key3[2];
        challenge[11] = key3[3];
        challenge[12] = key3[4];
        challenge[13] = key3[5];
        challenge[14] = key3[6];
        challenge[15] = key3[7];
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        responseChallenge = md5.digest(challenge);
      }

      String responseHandshake = "HTTP/1.1 101 Web Socket Protocol Handshake\r\n" +
                                 "Upgrade: WebSocket\r\n" +
                                 "Connection: Upgrade\r\n";
      responseHandshake += headerPrefix+"WebSocket-Origin: " + p.getProperty("Origin") + "\r\n";
      responseHandshake += headerPrefix+"WebSocket-Location: ws://" + p.getProperty("Host") + path + "\r\n";

      if (p.containsKey(headerPrefix+"WebSocket-Protocol")) {
        responseHandshake += headerPrefix+"WebSocket-Protocol: " + p.getProperty("WebSocket-Protocol") + "\r\n";
      }
      if (p.containsKey("Cookie")){
        responseHandshake += "Cookie: " + p.getProperty("Cookie")+"\r\n";
      }
      responseHandshake += "\r\n"; // Signifies end of handshake
      //Can not use UTF-8 here because we might lose bytes in response during conversion
      conn.socketChannel().write(ByteBuffer.wrap(responseHandshake.getBytes()));
      //Only set when Draft 76
      if(responseChallenge!=null){
        conn.socketChannel().write(ByteBuffer.wrap(responseChallenge));
      }
      return true;
    }

    // If we got to here, then the client sent an invalid handshake, and we
    // return false to make the WebSocket object close the connection.
    return false;
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

  private byte[] getPart(String key) {
    long keyNumber = Long.parseLong(key.replaceAll("[^0-9]",""));
    long keySpace = key.split("\u0020").length - 1;
    long part = new Long(keyNumber / keySpace);
    return new byte[] {
      (byte)( part >> 24 ),
      (byte)( (part << 8) >> 24 ),
      (byte)( (part << 16) >> 24 ),
      (byte)( (part << 24) >> 24 )      
    };
  }

  // ABTRACT METHODS /////////////////////////////////////////////////////////
  public abstract void onClientOpen(WebSocket conn);
  public abstract void onClientClose(WebSocket conn);
  public abstract void onClientMessage(WebSocket conn, String message);
}
