/*
 * Copyright (c) 2010-2018 Nathan Rajlich
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.java_websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;


import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLEngineResult.Status;


// TODO: Add more logging?
/**
 * A ByteChannel that passes the data through an SSLEngine.
 */
public class SSLSocketChannel3 implements ByteChannel {
    private final ByteChannel wrappedChannel;
    private final SSLEngine engine;
    protected final Logger logger = LoggerFactory.getLogger(SSLSocketChannel3.class);


    private ByteBuffer inAppData; // cleartext decoded from SSL
    private final ByteBuffer outAppData; // cleartext data to send
    private ByteBuffer inNetData; // SSL data read from wrappedChannel
    private final ByteBuffer outNetData; // SSL data to send on wrappedChannel


    private boolean closed = false;
    private int timeoutMillis = 0;
    private Selector selector = null;


    public void setTimeout(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }


    public int getTimeout() {
        return timeoutMillis;
    }


    /**
     * Creates a new instance of SSLByteChannel
     *
     * @param wrappedChannel
     *            The byte channel on which this ssl channel is built. This channel contains
     *            encrypted data.
     * @param engine
     *            A SSLEngine instance that will remember SSL current context. Warning, such an
     *            instance CAN NOT be shared
     * @param logger
     *            Logger for logging.
     */
    public SSLSocketChannel3(ByteChannel wrappedChannel, SSLEngine engine, ExecutorService inputExecutor, SelectionKey key) {
        this.wrappedChannel = wrappedChannel;
        this.engine = engine;


        SSLSession session = engine.getSession();


        inAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        outAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        logger.trace("app buffer size=" + session.getApplicationBufferSize());


        inNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        outNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        logger.trace("app buffer size=" + session.getPacketBufferSize());
    }


    /**
     * Ends SSL operation and close the wrapped byte channel
     *
     * @throws java.io.IOException
     *             May be raised by close operation on wrapped byte channel
     */
    public void close() throws java.io.IOException {
        if (!closed) {
            try {
                try {
                    engine.closeOutbound();
                    handleHandshake(wrapAppData());
                    if (selector != null)
                        selector.close();
                } catch (IOException e) {
                    // do nothing here
                }
                wrappedChannel.close();
            } finally {
                closed = true;
            }
        }
    }


    /**
     * Is the channel open ?
     *
     * @return true if the channel is still open
     */
    public boolean isOpen() {
        return !closed;
    }


    /**
     * Fill the given buffer with some bytes and return the number of bytes added in the buffer.<br>
     * This method may return immediately with nothing added in the buffer. This method must be use
     * exactly in the same way of ByteChannel read operation, so be careful with buffer position,
     * limit, ... Check corresponding javadoc.
     *
     * @param clientBuffer
     *            The buffer that will received read bytes
     * @return The number of bytes read
     * @throws java.io.IOException
     *             May be raised by ByteChannel read operation
     */
    public int read(ByteBuffer clientBuffer) throws IOException {
        // first try to copy out anything left over from last time
        int bytesCopied = copyOutClientData(clientBuffer);
        if (bytesCopied > 0)
            return bytesCopied;


        fillBufferFromEngine();
        bytesCopied = copyOutClientData(clientBuffer);
        if (bytesCopied > 0)
            return bytesCopied;


        return -1;
    }


    private void fillBufferFromEngine() throws IOException {
        while (true) {
            SSLEngineResult ser = unwrapNetData();
            if (ser.bytesProduced() > 0)
                return;


            switch (ser.getStatus()) {
                case OK:
                    break;


                case CLOSED:
                    close();
                    return;


                case BUFFER_OVERFLOW: {
                    int appSize = engine.getSession().getApplicationBufferSize();
                    ByteBuffer b = ByteBuffer.allocate(appSize + inAppData.position());
                    inAppData.flip();
                    b.put(inAppData);
                    inAppData = b;
                    continue; // retry operation
                }


                case BUFFER_UNDERFLOW: {
                    int netSize = engine.getSession().getPacketBufferSize();
                    if (netSize > inNetData.capacity()) {
                        ByteBuffer b = ByteBuffer.allocate(netSize);
                        inNetData.flip();
                        b.put(inNetData);
                        inNetData = b;
                    }


                    int rc = timedRead(inNetData, timeoutMillis);
                    if (rc == 0 && timeoutMillis > 0) {
                        throw new IOException("Timeout waiting for read (" + timeoutMillis + " milliseconds)");
                    }
                    if (rc == -1)
                        break;
                    continue; // retry operation
                }
            }


            switch (ser.getHandshakeStatus()) {
                case NOT_HANDSHAKING:
                    return;


                default:
                    handleHandshake(ser);
                    break;
            }
        }
    }


    private int timedRead(ByteBuffer buf, int timeoutMillis) throws IOException {
        if (timeoutMillis <= 0)
            return wrappedChannel.read(buf);


        SelectableChannel ch = (SelectableChannel)wrappedChannel;


        synchronized (ch) {
            SelectionKey key = null;


            if (selector == null) {
                selector = Selector.open();
            }


            try {
                selector.selectNow(); // Needed to clear old key state
                ch.configureBlocking(false);
                key = ch.register(selector, SelectionKey.OP_READ);


                selector.select(timeoutMillis);


                return wrappedChannel.read(buf);
            } finally {
                if (key != null)
                    key.cancel();
                ch.configureBlocking(true);
            }
        }
    }


    /**
     * Write remaining bytes of the given byte buffer. This method may return immediately with
     * nothing written. This method must be use exactly in the same way of ByteChannel write
     * operation, so be careful with buffer position, limit, ... Check corresponding javadoc.
     *
     * @param clientBuffer
     *            buffer with remaining bytes to write
     * @return The number of bytes written
     * @throws java.io.IOException
     *             May be raised by ByteChannel write operation
     */
    public int write(ByteBuffer clientBuffer) throws IOException {
        int bytesWritten = 0;


        while (clientBuffer.remaining() > 0) {
            bytesWritten += pushToEngine(clientBuffer);
        }


        return bytesWritten;
    }


    private int pushToEngine(ByteBuffer clientBuffer) throws IOException {
        int bytesWritten = 0;


        while (clientBuffer.remaining() > 0) {
            bytesWritten += copyInClientData(clientBuffer);
            logger.trace("bytesWritten="+bytesWritten);


            while (outAppData.position() > 0) {
                SSLEngineResult ser = wrapAppData();
                logger.trace("ser.getStatus()="+ser.getStatus());
                logger.trace("ser.getHandshakeStatus()="+ser.getHandshakeStatus());
                logger.trace("app bytes after wrap()="+outAppData.position());


                switch (ser.getStatus()) {
                    case OK:
                        break;


                    case CLOSED:
                        pushNetData();
                        close();
                        return bytesWritten;


                    case BUFFER_OVERFLOW:
                        continue;


                    case BUFFER_UNDERFLOW:
                        return bytesWritten; // TODO: handshake needed here?
                }


                switch (ser.getHandshakeStatus()) {
                    case NOT_HANDSHAKING:
                        break;


                    default:
                        handleHandshake(ser);
                        break;
                }
            }
        }


        return bytesWritten;
    }


    private void handleHandshake(SSLEngineResult initialSer) throws IOException {
        SSLEngineResult ser = initialSer;


        while (ser.getStatus() != Status.CLOSED) {
            switch (ser.getHandshakeStatus()) {
                case NEED_TASK:
                    Runnable task;


                    while ((task = engine.getDelegatedTask()) != null) {
                        task.run();
                    }


                    pushNetData();
                    ser = wrapAppData();
                    break;


                case NEED_WRAP:
                    pushNetData();
                    ser = wrapAppData();
                    break;


                case NEED_UNWRAP:
                    pushNetData();
                    if (inNetData.position() == 0) {
                        int n = wrappedChannel.read(inNetData);
                        if (n<0) throw new EOFException("SSL wrapped byte channel");
                    }
                    ser = unwrapNetData();
                    break;


                case FINISHED:
                case NOT_HANDSHAKING:
                    return;
            }
        }
    }


    private SSLEngineResult unwrapNetData() throws SSLException {
        SSLEngineResult ser;
        inNetData.flip();
        ser = engine.unwrap(inNetData, inAppData);
        inNetData.compact();
        return ser;
    }


    private SSLEngineResult wrapAppData() throws IOException {
        outAppData.flip();


        SSLEngineResult ser = engine.wrap(outAppData, outNetData);


        outAppData.compact();


        pushNetData();


        return ser;
    }


    private void pushNetData() throws IOException {
        outNetData.flip();


        while (outNetData.remaining() > 0) {
            wrappedChannel.write(outNetData);
        }


        outNetData.compact();
    }


    // ------------------------------------------------------------


    private int copyInClientData(ByteBuffer clientBuffer) {
        if (clientBuffer.remaining() == 0) {
            return 0;
        }


        int posBefore;


        posBefore = clientBuffer.position();


        if (clientBuffer.remaining() <= outAppData.remaining()) {
            outAppData.put(clientBuffer);
        } else {
            while (clientBuffer.hasRemaining() && outAppData.hasRemaining()) {
                outAppData.put(clientBuffer.get());
            }
        }


        return clientBuffer.position() - posBefore;
    }


    private int copyOutClientData(ByteBuffer clientBuffer) {
        inAppData.flip();
        int posBefore = inAppData.position();


        if (inAppData.remaining() <= clientBuffer.remaining()) {
            clientBuffer.put(inAppData);
        } else {
            while (clientBuffer.hasRemaining()) {
                clientBuffer.put(inAppData.get());
            }
        }


        int posAfter = inAppData.position();
        inAppData.compact();


        return posAfter - posBefore;
    }
}