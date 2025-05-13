package org.java_websocket;

import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SocketChannelIOHelperTest {

    private WebSocketImpl socket;
    private RecorderChannel channel;

    @BeforeEach
    void setUp() {
        socket = new WebSocketImpl(getListener(), null, 10);
    }

    @Test
    void bulkWriteSingleMessageFully() throws IOException {
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5, 6}));
        channel = new RecorderChannel(10);

        boolean finished = SocketChannelIOHelper.bulkWrite(socket, channel);

        assertTrue(finished);
        assertEquals(1, channel.buffers.size());
        assertEquals(4, channel.capacity);
        assertFalse(socket.bulkReadMode);
    }

    @Test
    void bulkWriteSingleMessagePartially() throws IOException {
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5, 6}));
        channel = new RecorderChannel(5);

        boolean finished = SocketChannelIOHelper.bulkWrite(socket, channel);

        assertFalse(finished);
        assertEquals(1, channel.buffers.size());
        assertEquals(0, channel.capacity);
        assertTrue(socket.bulkReadMode);
    }

    @Test
    void bulkWriteMultipleMessagesInBufferFully() throws IOException {
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {4, 5, 6}));
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {7, 8, 9}));
        channel = new RecorderChannel(10);

        boolean finished = SocketChannelIOHelper.bulkWrite(socket, channel);

        assertTrue(finished);
        assertEquals(1, channel.buffers.size());
        assertEquals(1, channel.capacity);
        assertFalse(socket.bulkReadMode);
    }

    @Test
    void bulkWriteMultipleMessagesInBufferPartially() throws IOException {
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {4, 5, 6}));
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {7, 8, 9}));
        channel = new RecorderChannel(8);

        boolean finished = SocketChannelIOHelper.bulkWrite(socket, channel);

        assertFalse(finished);
        assertEquals(1, channel.buffers.size());
        assertEquals(0, channel.capacity);
        assertTrue(socket.bulkReadMode);
    }

    @Test
    void bulkWriteMultipleMessagesExceedingBuffer() throws IOException {
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {4, 5, 6}));
        socket.outQueue.add(ByteBuffer.wrap(new byte[] {7, 8, 9}));
        ByteBuffer lastBuffer = ByteBuffer.wrap(new byte[] {10, 11, 12});
        socket.outQueue.add(lastBuffer);
        channel = new RecorderChannel(10);

        boolean finished = SocketChannelIOHelper.bulkWrite(socket, channel);

        assertFalse(finished);
        assertEquals(1, channel.buffers.size());
        assertEquals(1, channel.capacity);
        assertFalse(socket.bulkReadMode);
        assertEquals(1, socket.outQueue.size());
        assertSame(lastBuffer, socket.outQueue.poll());
    }

    @Test
    void bulkWriteSingleMessageExceedingBuffer() throws IOException {
        final ByteBuffer buffer1 = ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    }

    @Test
    void bulkWriteFirstMessageExceedingBuffer() throws IOException {
        final ByteBuffer buffer1 = ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        final ByteBuffer buffer2 = ByteBuffer.wrap(new byte[] {13, 14, 15});
    }

    @Test
    void bulkWriteSecondMessageExceedingBuffer() throws IOException {
    }

    @Test
    void bulkWriteRemainingDataCanBeSent() throws IOException {
    }

    @Test
    void bulkWriteRemainingDataCannotBeSent() throws IOException {
    }

    class RecorderChannel implements WritableByteChannel {

        final List<ByteBuffer> buffers = new ArrayList<>();
        int capacity;

        RecorderChannel(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public int write(ByteBuffer buffer) {
            buffers.add(buffer);
            if (capacity >= buffer.remaining()) {
                int written = buffer.remaining();
                buffer.position(buffer.position() + written);
                capacity -= written;
                return written;
            } else {
                int written = capacity;
                buffer.position(buffer.position() + written);
                capacity = 0;
                return written;
            }
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void close() throws IOException {
        }
    }


    private static WebSocketListener getListener() {
        return new WebSocketListener() {
            @Override
            public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
                return null;
            }

            @Override
            public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException {

            }

            @Override
            public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {

            }

            @Override
            public void onWebsocketMessage(WebSocket conn, String message) {

            }

            @Override
            public void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {

            }

            @Override
            public void onWebsocketOpen(WebSocket conn, Handshakedata d) {

            }

            @Override
            public void onWebsocketClose(WebSocket ws, int code, String reason, boolean remote) {

            }

            @Override
            public void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote) {

            }

            @Override
            public void onWebsocketCloseInitiated(WebSocket ws, int code, String reason) {

            }

            @Override
            public void onWebsocketError(WebSocket conn, Exception ex) {

            }

            @Override
            public void onWebsocketPing(WebSocket conn, Framedata f) {

            }

            @Override
            public PingFrame onPreparePing(WebSocket conn) {
                return null;
            }

            @Override
            public void onWebsocketPong(WebSocket conn, Framedata f) {

            }

            @Override
            public void onWriteDemand(WebSocket conn) {

            }

            @Override
            public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
                return null;
            }

            @Override
            public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
                return null;
            }
        };
    }
}