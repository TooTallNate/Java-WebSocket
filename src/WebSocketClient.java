
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public abstract class WebSocketClient implements Runnable, WebSocketListener {
    private final URI uri;
    private WebSocket conn;
    private Thread thread;
    private Selector selector;

    public WebSocketClient(URI serverUri) {
        uri = serverUri;
    }

    public URI getURI() {
        return uri;
    }

    public void connect() {
        thread = new Thread(this);
        thread.start();
    }

    public void close() throws IOException {
        conn.close();
    }

    public void send(String text) throws IOException {
        conn.sendFrame(text);
    }

    // Runnable IMPLEMENTATION /////////////////////////////////////////////////
    public void run() {
        try {
            SocketChannel client = SocketChannel.open();
            client.configureBlocking(false);
            int port = uri.getPort();
            if (port == -1) {
                port = 80;
            }
            client.connect(new InetSocketAddress(uri.getHost(), port));

            selector = Selector.open();

            this.conn = new WebSocket(client, this);
            client.register(selector, client.validOps(), conn);

            // Waiting for the connection
            while (selector.select(500)> 0) {

                Set keys = selector.selectedKeys();
                Iterator i = keys.iterator();

                while (i.hasNext()) {
                    SelectionKey key = (SelectionKey)i.next();
                    i.remove();

                    // When 'conn'
                    if (key.isConnectable()) {
                        // Ensure connection is finished
                        if (client.isConnectionPending())
                            client.finishConnect();

                        // Now send WebSocket client-side handshake
                        String path = "/";
                        String host = uri.getHost() + (uri.getPort() != 80 ? ":" + uri.getPort() : "");
                        String origin = "null";
                        String request = "GET "+path+" HTTP/1.1\r\n" +
                            "Upgrade: WebSocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Host: "+host+"\r\n" +
                            "Origin: "+origin+"\r\n" +
                            //extraHeaders.toString() +
                            "\r\n";
                        conn.socketChannel().write(ByteBuffer.wrap(request.getBytes(WebSocket.UTF8_CHARSET)));
                    }

                    if (key.isReadable()) {
                        conn.handleRead();
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
     */
    public boolean onHandshakeRecieved(WebSocket conn, String handshake) throws IOException {
        // TODO: Do some parsing of the returned handshake, and close connection
        // (return false) if we recieved anything unexpected.
        return true;
    }

    public void onMessage(WebSocket conn, String message) {
        onMessage(message);
    }

    public void onOpen(WebSocket conn) {
        onOpen();
    }
    
    public void onClose(WebSocket conn) {
        onClose();
    }


    // ABTRACT METHODS /////////////////////////////////////////////////////////
    public abstract void onMessage(String message);
    public abstract void onOpen();
    public abstract void onClose();
}
