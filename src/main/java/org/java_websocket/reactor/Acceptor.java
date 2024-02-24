package org.java_websocket.reactor;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * acceptor process new client connections and dispatch requests to the processor chain
 */
public class Acceptor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);
    private final ServerSocketChannel ssc; // socket channel monitored by mainReactor
    private final WebSocketServer wss;
    private final int cores = Runtime.getRuntime().availableProcessors(); // get the number of CPU cores
    private final Selector[] selectors = new Selector[cores]; // create several core selectors for subReactor
    private int selIdx = 0; // currently available subreactor indexes
    private TCPSubReactor[] r = new TCPSubReactor[cores]; // subReactor thread
    private Thread[] t = new Thread[cores]; // subReactor thread

    public Acceptor(ServerSocketChannel s, WebSocketServer server) throws IOException {
        this.ssc = s;
        this.wss = server;
        // Create multiple selectors and multiple subReactor threads
        for (int i = 0; i < cores; i++) {
            selectors[i] = Selector.open();
            r[i] = new TCPSubReactor(selectors[i], s, i);
            t[i] = new Thread(r[i]);
            t[i].start();
        }
    }

    @Override
    public void run() {
        try {
            SocketChannel sc = ssc.accept(); // receive client connection request
            if (sc != null) {
                log.trace(sc.socket().getRemoteSocketAddress().toString() + " is connected.");
                log.trace("selIdx is {}", selIdx);
                sc.configureBlocking(false); // set non blocking
                Socket socket = sc.socket();
                socket.setTcpNoDelay(this.wss.isTcpNoDelay());
                socket.setKeepAlive(true);
                r[selIdx].setRestart(true); // pause thread
                selectors[selIdx].wakeup(); // causes a blocked selector operation to return immediately
                WebSocketImpl w = this.wss.getWsf().createWebSocket(this.wss, this.wss.getDrafts());
                SelectionKey sk = sc.register(selectors[selIdx], SelectionKey.OP_READ, w);
                w.setSelectionKey(sk);
                try {
                    w.setChannel(this.wss.getWsf().wrapChannel(sc, w.getSelectionKey()));
                    allocateBuffers(w);
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                    if (w.getSelectionKey() != null)
                        w.getSelectionKey().cancel();
                    handleIOException(w.getSelectionKey(), null, ex);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                selectors[selIdx].wakeup(); // causes a blocked selector operation to return immediately
                r[selIdx].setRestart(false); // restart thread
                if (++selIdx == selectors.length)
                    selIdx = 0;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void allocateBuffers(WebSocket c) throws InterruptedException {
        synchronized (this.wss) {
            if (this.wss.getQueuesize().get() >= 2 * this.wss.getDecoders().size() + 1) {
                return;
            }
            this.wss.getQueuesize().incrementAndGet();
            this.wss.getBuffers().put(createBuffer());
        }
    }

    public ByteBuffer createBuffer() {
        return ByteBuffer.allocate(WebSocketImpl.RCVBUF);
    }

    private void handleIOException(SelectionKey key, WebSocket conn, IOException ex) {
        // onWebsocketError( conn, ex );// conn may be null here
        if (conn != null) {
            conn.closeConnection(CloseFrame.ABNORMAL_CLOSE, ex.getMessage());
        } else if (key != null) {
            SelectableChannel channel = key.channel();
            if (channel != null && channel.isOpen()) { // this could be the case if the IOException ex is a SSLException
                try {
                    channel.close();
                } catch (IOException e) {
                    // there is nothing that must be done here
                }
                log.trace("Connection closed because of exception", ex);
            }
        }
    }
}
