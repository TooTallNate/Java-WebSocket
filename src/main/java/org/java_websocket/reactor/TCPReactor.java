package org.java_websocket.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.server.WebSocketServer.WebSocketWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCPReactor is the mainReactor, process new client connections
 */
public class TCPReactor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(TCPReactor.class);

    private ServerSocketChannel ssc;
    private Selector selector;

    public TCPReactor(InetSocketAddress address, ServerSocketChannel server, WebSocketServer wss,
                      Selector s) {
        try {
            this.ssc = server;
            this.selector = s;
            Acceptor acceptor = new Acceptor(server, wss);
            SelectionKey sk = server.register(selector, SelectionKey.OP_ACCEPT);
            sk.attach(acceptor);
            wss.startConnectionLostTimer();
            for (WebSocketWorker ex : wss.getDecoders()) {
                ex.start();
            }
            wss.onStart();
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        } catch (ClosedChannelException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            log.trace("mainReactor waiting for new event on port: " + ssc.socket().getLocalPort()
                    + "...");
            try {
                if (selector.select() == 0) {// if no event is ready, do not proceed
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                while (it.hasNext()) {
                    dispatch((SelectionKey) (it.next()));
                    it.remove();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (ClosedSelectorException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable r = (Runnable) (key.attachment());
        if (r != null)
            r.run();
    }

}
