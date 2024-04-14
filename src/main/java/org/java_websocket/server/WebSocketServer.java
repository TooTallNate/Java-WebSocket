/*
 * Copyright (c) 2010-2020 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.java_websocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.java_websocket.AbstractWebSocket;
import org.java_websocket.SocketChannelIOHelper;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketFactory;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.WrappedByteChannel;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.exceptions.WrappedIOException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>WebSocketServer</code> is an abstract class that only takes care of the
 * HTTP handshake portion of WebSockets. It's up to a subclass to add functionality/purpose to the
 * server.
 */
public abstract class WebSocketServer extends AbstractWebSocket implements Runnable {

  private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

  /**
   * Logger instance
   *
   * @since 1.4.0
   */
  private final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

  /**
   * Holds the list of active WebSocket connections. "Active" means WebSocket handshake is complete
   * and socket can be written to, or read from.
   */
  private final Collection<WebSocket> connections;
  /**
   * The port number that this WebSocket server should listen on. Default is
   * WebSocketImpl.DEFAULT_PORT.
   */
  private final InetSocketAddress address;
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
  private List<Draft> drafts;

  private Thread selectorthread;

  private final AtomicBoolean isclosed = new AtomicBoolean(false);

  protected List<WebSocketWorker> decoders;

  private List<WebSocketImpl> iqueue;
  private BlockingQueue<ByteBuffer> buffers;
  private int queueinvokes = 0;
  private final AtomicInteger queuesize = new AtomicInteger(0);

  private WebSocketServerFactory wsf = new DefaultWebSocketServerFactory();

  /**
   * Attribute which allows you to configure the socket "backlog" parameter which determines how
   * many client connections can be queued.
   *
   * @since 1.5.0
   */
  private int maxPendingConnections = -1;

  /**
   * Creates a WebSocketServer that will attempt to listen on port <var>WebSocketImpl.DEFAULT_PORT</var>.
   *
   * @see #WebSocketServer(InetSocketAddress, int, List, Collection) more details here
   */
  public WebSocketServer() {
    this(new InetSocketAddress(WebSocketImpl.DEFAULT_PORT), AVAILABLE_PROCESSORS, null);
  }

  /**
   * Creates a WebSocketServer that will attempt to bind/listen on the given <var>address</var>.
   *
   * @param address The address to listen to
   * @see #WebSocketServer(InetSocketAddress, int, List, Collection) more details here
   */
  public WebSocketServer(InetSocketAddress address) {
    this(address, AVAILABLE_PROCESSORS, null);
  }

  /**
   * @param address      The address (host:port) this server should listen on.
   * @param decodercount The number of {@link WebSocketWorker}s that will be used to process the
   *                     incoming network data. By default this will be <code>Runtime.getRuntime().availableProcessors()</code>
   * @see #WebSocketServer(InetSocketAddress, int, List, Collection) more details here
   */
  public WebSocketServer(InetSocketAddress address, int decodercount) {
    this(address, decodercount, null);
  }

  /**
   * @param address The address (host:port) this server should listen on.
   * @param drafts  The versions of the WebSocket protocol that this server instance should comply
   *                to. Clients that use an other protocol version will be rejected.
   * @see #WebSocketServer(InetSocketAddress, int, List, Collection) more details here
   */
  public WebSocketServer(InetSocketAddress address, List<Draft> drafts) {
    this(address, AVAILABLE_PROCESSORS, drafts);
  }

  /**
   * @param address      The address (host:port) this server should listen on.
   * @param decodercount The number of {@link WebSocketWorker}s that will be used to process the
   *                     incoming network data. By default this will be <code>Runtime.getRuntime().availableProcessors()</code>
   * @param drafts       The versions of the WebSocket protocol that this server instance should
   *                     comply to. Clients that use an other protocol version will be rejected.
   * @see #WebSocketServer(InetSocketAddress, int, List, Collection) more details here
   */
  public WebSocketServer(InetSocketAddress address, int decodercount, List<Draft> drafts) {
    this(address, decodercount, drafts, new HashSet<WebSocket>());
  }

  /**
   * Creates a WebSocketServer that will attempt to bind/listen on the given <var>address</var>, and
   * comply with <code>Draft</code> version <var>draft</var>.
   *
   * @param address              The address (host:port) this server should listen on.
   * @param decodercount         The number of {@link WebSocketWorker}s that will be used to process
   *                             the incoming network data. By default this will be
   *                             <code>Runtime.getRuntime().availableProcessors()</code>
   * @param drafts               The versions of the WebSocket protocol that this server instance
   *                             should comply to. Clients that use an other protocol version will
   *                             be rejected.
   * @param connectionscontainer Allows to specify a collection that will be used to store the
   *                             websockets in. <br> If you plan to often iterate through the
   *                             currently connected websockets you may want to use a collection
   *                             that does not require synchronization like a {@link
   *                             CopyOnWriteArraySet}. In that case make sure that you overload
   *                             {@link #removeConnection(WebSocket)} and {@link
   *                             #addConnection(WebSocket)}.<br> By default a {@link HashSet} will
   *                             be used.
   * @see #removeConnection(WebSocket) for more control over syncronized operation
   * @see <a href="https://github.com/TooTallNate/Java-WebSocket/wiki/Drafts" > more about
   * drafts</a>
   */
  public WebSocketServer(InetSocketAddress address, int decodercount, List<Draft> drafts,
      Collection<WebSocket> connectionscontainer) {
    if (address == null || decodercount < 1 || connectionscontainer == null) {
      throw new IllegalArgumentException(
          "address and connectionscontainer must not be null and you need at least 1 decoder");
    }

    if (drafts == null) {
      this.drafts = Collections.emptyList();
    } else {
      this.drafts = drafts;
    }

    this.address = address;
    this.connections = connectionscontainer;
    setTcpNoDelay(false);
    setReuseAddr(false);
    iqueue = new LinkedList<>();

    decoders = new ArrayList<>(decodercount);
    buffers = new LinkedBlockingQueue<>();
    for (int i = 0; i < decodercount; i++) {
      WebSocketWorker ex = new WebSocketWorker();
      decoders.add(ex);
    }
  }


  /**
   * Starts the server selectorthread that binds to the currently set port number and listeners for
   * WebSocket connection requests. Creates a fixed thread pool with the size {@link
   * WebSocketServer#AVAILABLE_PROCESSORS}<br> May only be called once.
   * <p>
   * Alternatively you can call {@link WebSocketServer#run()} directly.
   *
   * @throws IllegalStateException Starting an instance again
   */
  public void start() {
    if (selectorthread != null) {
      throw new IllegalStateException(getClass().getName() + " can only be started once.");
    }
    Thread t = new Thread(this);
    t.setDaemon(isDaemon());
    t.start();
  }

  public void stop(int timeout) throws InterruptedException {
    stop(timeout, "");
  }

  /**
   * Closes all connected clients sockets, then closes the underlying ServerSocketChannel,
   * effectively killing the server socket selectorthread, freeing the port the server was bound to
   * and stops all internal workerthreads.
   * <p>
   * If this method is called before the server is started it will never start.
   *
   * @param timeout Specifies how many milliseconds the overall close handshaking may take
   *                altogether before the connections are closed without proper close
   *                handshaking.
   * @param closeMessage Specifies message for remote client<br>
   * @throws InterruptedException Interrupt
   */
  public void stop(int timeout, String closeMessage) throws InterruptedException {
    if (!isclosed.compareAndSet(false,
        true)) { // this also makes sure that no further connections will be added to this.connections
      return;
    }

    List<WebSocket> socketsToClose;

    // copy the connections in a list (prevent callback deadlocks)
    synchronized (connections) {
      socketsToClose = new ArrayList<>(connections);
    }

    for (WebSocket ws : socketsToClose) {
      ws.close(CloseFrame.GOING_AWAY, closeMessage);
    }

    wsf.close();

    synchronized (this) {
      if (selectorthread != null && selector != null) {
        selector.wakeup();
        selectorthread.join(timeout);
      }
    }
  }

  public void stop() throws InterruptedException {
    stop(0);
  }

  /**
   * Returns  all currently connected clients. This collection does not allow any modification e.g.
   * removing a client.
   *
   * @return A unmodifiable collection of all currently connected clients
   * @since 1.3.8
   */
  public Collection<WebSocket> getConnections() {
    synchronized (connections) {
      return Collections.unmodifiableCollection(new ArrayList<>(connections));
    }
  }

  public InetSocketAddress getAddress() {
    return this.address;
  }

  /**
   * Gets the port number that this server listens on.
   *
   * @return The port number.
   */
  public int getPort() {
    int port = getAddress().getPort();
    if (port == 0 && server != null) {
      port = server.socket().getLocalPort();
    }
    return port;
  }

  @Override
  public void setDaemon(boolean daemon) {
    // pass it to the AbstractWebSocket too, to use it on the connectionLostChecker thread factory
    super.setDaemon(daemon);
    // we need to apply this to the decoders as well since they were created during the constructor
    for (WebSocketWorker w : decoders) {
      if (w.isAlive()) {
        throw new IllegalStateException("Cannot call setDaemon after server is already started!");
      } else {
        w.setDaemon(daemon);
      }
    }
  }

  /**
   * Get the list of active drafts
   *
   * @return the available drafts for this server
   */
  public List<Draft> getDraft() {
    return Collections.unmodifiableList(drafts);
  }

  /**
   * Set the requested maximum number of pending connections on the socket. The exact semantics are
   * implementation specific. The value provided should be greater than 0. If it is less than or
   * equal to 0, then an implementation specific default will be used. This option will be passed as
   * "backlog" parameter to {@link ServerSocket#bind(SocketAddress, int)}
   *
   * @since 1.5.0
   * @param numberOfConnections the new number of allowed pending connections
   */
  public void setMaxPendingConnections(int numberOfConnections) {
    maxPendingConnections = numberOfConnections;
  }

  /**
   * Returns the currently configured maximum number of pending connections.
   *
   * @see #setMaxPendingConnections(int)
   * @since 1.5.0
   * @return the maximum number of pending connections
   */
  public int getMaxPendingConnections() {
    return maxPendingConnections;
  }

  // Runnable IMPLEMENTATION /////////////////////////////////////////////////
  public void run() {
    if (!doEnsureSingleThread()) {
      return;
    }
    if (!doSetupSelectorAndServerThread()) {
      return;
    }
    try {
      int shutdownCount = 5;
      int selectTimeout = 0;
      while (!selectorthread.isInterrupted() && shutdownCount != 0) {
        SelectionKey key = null;
        try {
          if (isclosed.get()) {
            selectTimeout = 5;
          }
          int keyCount = selector.select(selectTimeout);
          if (keyCount == 0 && isclosed.get()) {
            shutdownCount--;
          }
          Set<SelectionKey> keys = selector.selectedKeys();
          Iterator<SelectionKey> i = keys.iterator();

          while (i.hasNext()) {
            key = i.next();

            if (!key.isValid()) {
              continue;
            }

            if (key.isAcceptable()) {
              doAccept(key, i);
              continue;
            }

            if (key.isReadable() && !doRead(key, i)) {
              continue;
            }

            if (key.isWritable()) {
              doWrite(key);
            }
          }
          doAdditionalRead();
        } catch (CancelledKeyException e) {
          // an other thread may cancel the key
        } catch (ClosedByInterruptException e) {
          return; // do the same stuff as when InterruptedException is thrown
        } catch (WrappedIOException ex) {
          handleIOException(key, ex.getConnection(), ex.getIOException());
        } catch (IOException ex) {
          handleIOException(key, null, ex);
        } catch (InterruptedException e) {
          // FIXME controlled shutdown (e.g. take care of buffermanagement)
          Thread.currentThread().interrupt();
        }
      }
    } catch (RuntimeException e) {
      // should hopefully never occur
      handleFatal(null, e);
    } finally {
      doServerShutdown();
    }
  }

  /**
   * Do an additional read
   *
   * @throws InterruptedException thrown by taking a buffer
   * @throws IOException          if an error happened during read
   */
  private void doAdditionalRead() throws InterruptedException, IOException {
    WebSocketImpl conn;
    while (!iqueue.isEmpty()) {
      conn = iqueue.remove(0);
      WrappedByteChannel c = ((WrappedByteChannel) conn.getChannel());
      ByteBuffer buf = takeBuffer();
      try {
        if (SocketChannelIOHelper.readMore(buf, conn, c)) {
          iqueue.add(conn);
        }
        if (buf.hasRemaining()) {
          conn.inQueue.put(buf);
          queue(conn);
        } else {
          pushBuffer(buf);
        }
      } catch (IOException e) {
        pushBuffer(buf);
        throw e;
      }
    }
  }

  /**
   * Execute a accept operation
   *
   * @param key the selectionkey to read off
   * @param i   the iterator for the selection keys
   * @throws InterruptedException thrown by taking a buffer
   * @throws IOException          if an error happened during accept
   */
  private void doAccept(SelectionKey key, Iterator<SelectionKey> i)
      throws IOException, InterruptedException {
    if (!onConnect(key)) {
      key.cancel();
      return;
    }

    SocketChannel channel = server.accept();
    if (channel == null) {
      return;
    }
    channel.configureBlocking(false);
    Socket socket = channel.socket();
    socket.setTcpNoDelay(isTcpNoDelay());
    socket.setKeepAlive(true);
    WebSocketImpl w = wsf.createWebSocket(this, drafts);
    w.setSelectionKey(channel.register(selector, SelectionKey.OP_READ, w));
    try {
      w.setChannel(wsf.wrapChannel(channel, w.getSelectionKey()));
      i.remove();
      allocateBuffers(w);
    } catch (IOException ex) {
      if (w.getSelectionKey() != null) {
        w.getSelectionKey().cancel();
      }

      handleIOException(w.getSelectionKey(), null, ex);
    }
  }

  /**
   * Execute a read operation
   *
   * @param key the selectionkey to read off
   * @param i   the iterator for the selection keys
   * @return true, if the read was successful, or false if there was an error
   * @throws InterruptedException thrown by taking a buffer
   * @throws IOException          if an error happened during read
   */
  private boolean doRead(SelectionKey key, Iterator<SelectionKey> i)
      throws InterruptedException, WrappedIOException {
    WebSocketImpl conn = (WebSocketImpl) key.attachment();
    ByteBuffer buf = takeBuffer();
    if (conn.getChannel() == null) {
      key.cancel();

      handleIOException(key, conn, new IOException());
      return false;
    }
    try {
      if (SocketChannelIOHelper.read(buf, conn, conn.getChannel())) {
        if (buf.hasRemaining()) {
          conn.inQueue.put(buf);
          queue(conn);
          i.remove();
          if (conn.getChannel() instanceof WrappedByteChannel && ((WrappedByteChannel) conn
              .getChannel()).isNeedRead()) {
            iqueue.add(conn);
          }
        } else {
          pushBuffer(buf);
        }
      } else {
        pushBuffer(buf);
      }
    } catch (IOException e) {
      pushBuffer(buf);
      throw new WrappedIOException(conn, e);
    }
    return true;
  }

  /**
   * Execute a write operation
   *
   * @param key the selectionkey to write on
   * @throws IOException if an error happened during batch
   */
  private void doWrite(SelectionKey key) throws WrappedIOException {
    WebSocketImpl conn = (WebSocketImpl) key.attachment();
    try {
      if (SocketChannelIOHelper.batch(conn, conn.getChannel()) && key.isValid()) {
        key.interestOps(SelectionKey.OP_READ);
      }
    } catch (IOException e) {
      throw new WrappedIOException(conn, e);
    }
  }

  /**
   * Setup the selector thread as well as basic server settings
   *
   * @return true, if everything was successful, false if some error happened
   */
  private boolean doSetupSelectorAndServerThread() {
    selectorthread.setName("WebSocketSelector-" + selectorthread.getId());
    try {
      server = ServerSocketChannel.open();
      server.configureBlocking(false);
      ServerSocket socket = server.socket();
      int receiveBufferSize = getReceiveBufferSize();
      if (receiveBufferSize > 0) {
        socket.setReceiveBufferSize(receiveBufferSize);
      }
      socket.setReuseAddress(isReuseAddr());
      socket.bind(address, getMaxPendingConnections());
      selector = Selector.open();
      server.register(selector, server.validOps());
      startConnectionLostTimer();
      for (WebSocketWorker ex : decoders) {
        ex.start();
      }
      onStart();
    } catch (IOException ex) {
      handleFatal(null, ex);
      return false;
    }
    return true;
  }

  /**
   * The websocket server can only be started once
   *
   * @return true, if the server can be started, false if already a thread is running
   */
  private boolean doEnsureSingleThread() {
    synchronized (this) {
      if (selectorthread != null) {
        throw new IllegalStateException(getClass().getName() + " can only be started once.");
      }
      selectorthread = Thread.currentThread();
      if (isclosed.get()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Clean up everything after a shutdown
   */
  private void doServerShutdown() {
    stopConnectionLostTimer();
    if (decoders != null) {
      for (WebSocketWorker w : decoders) {
        w.interrupt();
      }
    }
    if (selector != null) {
      try {
        selector.close();
      } catch (IOException e) {
        log.error("IOException during selector.close", e);
        onError(null, e);
      }
    }
    if (server != null) {
      try {
        server.close();
      } catch (IOException e) {
        log.error("IOException during server.close", e);
        onError(null, e);
      }
    }
  }

  protected void allocateBuffers(WebSocket c) throws InterruptedException {
    if (queuesize.get() >= 2 * decoders.size() + 1) {
      return;
    }
    queuesize.incrementAndGet();
    buffers.put(createBuffer());
  }

  protected void releaseBuffers(WebSocket c) throws InterruptedException {
    // queuesize.decrementAndGet();
    // takeBuffer();
  }

  public ByteBuffer createBuffer() {
    int receiveBufferSize = getReceiveBufferSize();
    return ByteBuffer.allocate(receiveBufferSize > 0 ? receiveBufferSize : DEFAULT_READ_BUFFER_SIZE);
  }

  protected void queue(WebSocketImpl ws) throws InterruptedException {
    if (ws.getWorkerThread() == null) {
      ws.setWorkerThread(decoders.get(queueinvokes % decoders.size()));
      queueinvokes++;
    }
    ws.getWorkerThread().put(ws);
  }

  private ByteBuffer takeBuffer() throws InterruptedException {
    return buffers.take();
  }

  private void pushBuffer(ByteBuffer buf) throws InterruptedException {
    if (buffers.size() > queuesize.intValue()) {
      return;
    }
    buffers.put(buf);
  }

  private void handleIOException(SelectionKey key, WebSocket conn, IOException ex) {
    // onWebsocketError( conn, ex );// conn may be null here
    if (key != null) {
      key.cancel();
    }
    if (conn != null) {
      conn.closeConnection(CloseFrame.ABNORMAL_CLOSE, ex.getMessage());
    } else if (key != null) {
      SelectableChannel channel = key.channel();
      if (channel != null && channel
          .isOpen()) { // this could be the case if the IOException ex is a SSLException
        try {
          channel.close();
        } catch (IOException e) {
          // there is nothing that must be done here
        }
        log.trace("Connection closed because of exception", ex);
      }
    }
  }

  private void handleFatal(WebSocket conn, Exception e) {
    log.error("Shutdown due to fatal error", e);
    onError(conn, e);

    String causeMessage = e.getCause() != null ? " caused by " + e.getCause().getClass().getName() : "";
    String errorMessage = "Got error on server side: " + e.getClass().getName() + causeMessage;
    try {
      stop(0, errorMessage);
    } catch (InterruptedException e1) {
      Thread.currentThread().interrupt();
      log.error("Interrupt during stop", e);
      onError(null, e1);
    }

    //Shutting down WebSocketWorkers, see #222
    if (decoders != null) {
      for (WebSocketWorker w : decoders) {
        w.interrupt();
      }
    }
    if (selectorthread != null) {
      selectorthread.interrupt();
    }
  }

  @Override
  public final void onWebsocketMessage(WebSocket conn, String message) {
    onMessage(conn, message);
  }


  @Override
  public final void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
    onMessage(conn, blob);
  }

  @Override
  public final void onWebsocketOpen(WebSocket conn, Handshakedata handshake) {
    if (addConnection(conn)) {
      onOpen(conn, (ClientHandshake) handshake);
    }
  }

  @Override
  public final void onWebsocketClose(WebSocket conn, int code, String reason, boolean remote) {
    selector.wakeup();
    try {
      if (removeConnection(conn)) {
        onClose(conn, code, reason, remote);
      }
    } finally {
      try {
        releaseBuffers(conn);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

  }

  /**
   * This method performs remove operations on the connection and therefore also gives control over
   * whether the operation shall be synchronized
   * <p>
   * {@link #WebSocketServer(InetSocketAddress, int, List, Collection)} allows to specify a
   * collection which will be used to store current connections in.<br> Depending on the type on the
   * connection, modifications of that collection may have to be synchronized.
   *
   * @param ws The Websocket connection which should be removed
   * @return Removing connection successful
   */
  protected boolean removeConnection(WebSocket ws) {
    boolean removed = false;
    synchronized (connections) {
      if (this.connections.contains(ws)) {
        removed = this.connections.remove(ws);
      } else {
        //Don't throw an assert error if the ws is not in the list. e.g. when the other endpoint did not send any handshake. see #512
        log.trace(
            "Removing connection which is not in the connections collection! Possible no handshake received! {}",
            ws);
      }
    }
    if (isclosed.get() && connections.isEmpty()) {
      selectorthread.interrupt();
    }
    return removed;
  }

  /**
   * @param ws the Websocket connection which should be added
   * @return Adding connection successful
   * @see #removeConnection(WebSocket)
   */
  protected boolean addConnection(WebSocket ws) {
    if (!isclosed.get()) {
      synchronized (connections) {
        return this.connections.add(ws);
      }
    } else {
      // This case will happen when a new connection gets ready while the server is already stopping.
      ws.close(CloseFrame.GOING_AWAY);
      return true;// for consistency sake we will make sure that both onOpen will be called
    }
  }

  @Override
  public final void onWebsocketError(WebSocket conn, Exception ex) {
    onError(conn, ex);
  }

  @Override
  public final void onWriteDemand(WebSocket w) {
    WebSocketImpl conn = (WebSocketImpl) w;
    try {
      conn.getSelectionKey().interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    } catch (CancelledKeyException e) {
      // the thread which cancels key is responsible for possible cleanup
      conn.outQueue.clear();
    }
    selector.wakeup();
  }

  @Override
  public void onWebsocketCloseInitiated(WebSocket conn, int code, String reason) {
    onCloseInitiated(conn, code, reason);
  }

  @Override
  public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
    onClosing(conn, code, reason, remote);

  }

  public void onCloseInitiated(WebSocket conn, int code, String reason) {
  }

  public void onClosing(WebSocket conn, int code, String reason, boolean remote) {

  }

  public final void setWebSocketFactory(WebSocketServerFactory wsf) {
    if (this.wsf != null) {
      this.wsf.close();
    }
    this.wsf = wsf;
  }

  public final WebSocketFactory getWebSocketFactory() {
    return wsf;
  }

  /**
   * Returns whether a new connection shall be accepted or not.<br> Therefore method is well suited
   * to implement some kind of connection limitation.<br>
   *
   * @param key the SelectionKey for the new connection
   * @return Can this new connection be accepted
   * @see #onOpen(WebSocket, ClientHandshake)
   * @see #onWebsocketHandshakeReceivedAsServer(WebSocket, Draft, ClientHandshake)
   **/
  protected boolean onConnect(SelectionKey key) {
    return true;
  }

  /**
   * Getter to return the socket used by this specific connection
   *
   * @param conn The specific connection
   * @return The socket used by this connection
   */
  private Socket getSocket(WebSocket conn) {
    WebSocketImpl impl = (WebSocketImpl) conn;
    return ((SocketChannel) impl.getSelectionKey().channel()).socket();
  }

  @Override
  public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
    return (InetSocketAddress) getSocket(conn).getLocalSocketAddress();
  }

  @Override
  public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
    return (InetSocketAddress) getSocket(conn).getRemoteSocketAddress();
  }

  /**
   * Called after an opening handshake has been performed and the given websocket is ready to be
   * written on.
   *
   * @param conn      The <code>WebSocket</code> instance this event is occurring on.
   * @param handshake The handshake of the websocket instance
   */
  public abstract void onOpen(WebSocket conn, ClientHandshake handshake);

  /**
   * Called after the websocket connection has been closed.
   *
   * @param conn   The <code>WebSocket</code> instance this event is occurring on.
   * @param code   The codes can be looked up here: {@link CloseFrame}
   * @param reason Additional information string
   * @param remote Returns whether or not the closing of the connection was initiated by the remote
   *               host.
   **/
  public abstract void onClose(WebSocket conn, int code, String reason, boolean remote);

  /**
   * Callback for string messages received from the remote host
   *
   * @param conn    The <code>WebSocket</code> instance this event is occurring on.
   * @param message The UTF-8 decoded message that was received.
   * @see #onMessage(WebSocket, ByteBuffer)
   **/
  public abstract void onMessage(WebSocket conn, String message);

  /**
   * Called when errors occurs. If an error causes the websocket connection to fail {@link
   * #onClose(WebSocket, int, String, boolean)} will be called additionally.<br> This method will be
   * called primarily because of IO or protocol errors.<br> If the given exception is an
   * RuntimeException that probably means that you encountered a bug.<br>
   *
   * @param conn Can be null if there error does not belong to one specific websocket. For example
   *             if the servers port could not be bound.
   * @param ex   The exception causing this error
   **/
  public abstract void onError(WebSocket conn, Exception ex);

  /**
   * Called when the server started up successfully.
   * <p>
   * If any error occurred, onError is called instead.
   */
  public abstract void onStart();

  /**
   * Callback for binary messages received from the remote host
   *
   * @param conn    The <code>WebSocket</code> instance this event is occurring on.
   * @param message The binary message that was received.
   * @see #onMessage(WebSocket, ByteBuffer)
   **/
  public void onMessage(WebSocket conn, ByteBuffer message) {
  }

  /**
   * Send a text to all connected endpoints
   *
   * @param text the text to send to the endpoints
   */
  public void broadcast(String text) {
    broadcast(text, connections);
  }

  /**
   * Send a byte array to all connected endpoints
   *
   * @param data the data to send to the endpoints
   */
  public void broadcast(byte[] data) {
    broadcast(data, connections);
  }

  /**
   * Send a ByteBuffer to all connected endpoints
   *
   * @param data the data to send to the endpoints
   */
  public void broadcast(ByteBuffer data) {
    broadcast(data, connections);
  }

  /**
   * Send a byte array to a specific collection of websocket connections
   *
   * @param data    the data to send to the endpoints
   * @param clients a collection of endpoints to whom the text has to be send
   */
  public void broadcast(byte[] data, Collection<WebSocket> clients) {
    if (data == null || clients == null) {
      throw new IllegalArgumentException();
    }
    broadcast(ByteBuffer.wrap(data), clients);
  }

  /**
   * Send a ByteBuffer to a specific collection of websocket connections
   *
   * @param data    the data to send to the endpoints
   * @param clients a collection of endpoints to whom the text has to be send
   */
  public void broadcast(ByteBuffer data, Collection<WebSocket> clients) {
    if (data == null || clients == null) {
      throw new IllegalArgumentException();
    }
    doBroadcast(data, clients);
  }

  /**
   * Send a text to a specific collection of websocket connections
   *
   * @param text    the text to send to the endpoints
   * @param clients a collection of endpoints to whom the text has to be send
   */
  public void broadcast(String text, Collection<WebSocket> clients) {
    if (text == null || clients == null) {
      throw new IllegalArgumentException();
    }
    doBroadcast(text, clients);
  }

  /**
   * Private method to cache all the frames to improve memory footprint and conversion time
   *
   * @param data    the data to broadcast
   * @param clients the clients to send the message to
   */
  private void doBroadcast(Object data, Collection<WebSocket> clients) {
    String strData = null;
    if (data instanceof String) {
      strData = (String) data;
    }
    ByteBuffer byteData = null;
    if (data instanceof ByteBuffer) {
      byteData = (ByteBuffer) data;
    }
    if (strData == null && byteData == null) {
      return;
    }
    Map<Draft, List<Framedata>> draftFrames = new HashMap<>();
    List<WebSocket> clientCopy;
    synchronized (clients) {
      clientCopy = new ArrayList<>(clients);
    }
    for (WebSocket client : clientCopy) {
      if (client != null) {
        Draft draft = client.getDraft();
        fillFrames(draft, draftFrames, strData, byteData);
        try {
          client.sendFrame(draftFrames.get(draft));
        } catch (WebsocketNotConnectedException e) {
          //Ignore this exception in this case
        }
      }
    }
  }

  /**
   * Fills the draftFrames with new data for the broadcast
   *
   * @param draft       The draft to use
   * @param draftFrames The list of frames per draft to fill
   * @param strData       the string data, can be null
   * @param byteData       the byte buffer data, can be null
   */
  private void fillFrames(Draft draft, Map<Draft, List<Framedata>> draftFrames, String strData,
      ByteBuffer byteData) {
    if (!draftFrames.containsKey(draft)) {
      List<Framedata> frames = null;
      if (strData != null) {
        frames = draft.createFrames(strData, false);
      }
      if (byteData != null) {
        frames = draft.createFrames(byteData, false);
      }
      if (frames != null) {
        draftFrames.put(draft, frames);
      }
    }
  }

  /**
   * This class is used to process incoming data
   */
  public class WebSocketWorker extends Thread {

    private BlockingQueue<WebSocketImpl> iqueue;

    public WebSocketWorker() {
      iqueue = new LinkedBlockingQueue<>();
      setName("WebSocketWorker-" + getId());
      setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          log.error("Uncaught exception in thread {}: {}", t.getName(), e);
        }
      });
    }

    public void put(WebSocketImpl ws) throws InterruptedException {
      iqueue.put(ws);
    }

    @Override
    public void run() {
      WebSocketImpl ws = null;
      try {
        while (true) {
          ByteBuffer buf;
          ws = iqueue.take();
          buf = ws.inQueue.poll();
          assert (buf != null);
          doDecode(ws, buf);
          ws = null;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (VirtualMachineError | ThreadDeath | LinkageError e) {
        log.error("Got fatal error in worker thread {}", getName());
        Exception exception = new Exception(e);
        handleFatal(ws, exception);
      } catch (Throwable e) {
        log.error("Uncaught exception in thread {}: {}", getName(), e);
        if (ws != null) {
          Exception exception = new Exception(e);
          onWebsocketError(ws, exception);
          ws.close();
        }
      }
    }

    /**
     * call ws.decode on the byteBuffer
     *
     * @param ws  the Websocket
     * @param buf the buffer to decode to
     * @throws InterruptedException thrown by pushBuffer
     */
    private void doDecode(WebSocketImpl ws, ByteBuffer buf) throws InterruptedException {
      try {
        ws.decode(buf);
      } catch (Exception e) {
        log.error("Error while reading from remote connection", e);
      } finally {
        pushBuffer(buf);
      }
    }
  }
}
