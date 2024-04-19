package org.java_websocket.reactor;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCPSubReactor is the subReactor, read/write network data and perform business processing, and throw it to the worker thread pool
 */
public class TCPSubReactor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(TCPSubReactor.class);
    private final Selector selector;
    private volatile boolean restart = false;
    int num;

    public TCPSubReactor(Selector selector, ServerSocketChannel ssc, int num) {
        this.selector = selector;
        this.num = num;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) { // continue running until the thread is interrupted
            log.trace("waiting for restart");
            while (!Thread.interrupted() && !restart) { // runs continuously until the thread is interrupted and designated to restart
                try {
                    if (selector.select() == 0) {// ff no event is ready, do not proceed
                        continue;
                    }
                    Set<SelectionKey> selectedKeys = selector.selectedKeys(); // get the key collection of all ready events
                    Iterator<SelectionKey> it = selectedKeys.iterator();
                    while (it.hasNext()) {
                        dispatch((SelectionKey) (it.next())); // schedule according to the key of the event
                        it.remove();
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                } catch (ClosedSelectorException e) {
                    log.error(e.getMessage(), e);
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                }
            }
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable r = (Runnable) (key.attachment()); // open a new thread according to the object bound by the key of the event
        if (r != null)
            r.run();
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }
}
