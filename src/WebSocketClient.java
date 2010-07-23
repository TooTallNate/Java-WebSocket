
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;

/**
 * The <tt>WebSocketClient</tt> is an abstract class that expects a valid
 * "ws://" URI to connect to. When connected, an instance recieves important
 * events related to the life of the connection. A subclass must implement
 * <var>onOpen</var>, <var>onClose</var>, and <var>onMessage</var> to be
 * useful. An instance can send messages to it's connected server via the
 * <var>send</var> method.
 * @author Nathan Rajlich
 */
public abstract class WebSocketClient implements Runnable, WebSocketListener {
    // INSTANCE PROPERTIES /////////////////////////////////////////////////////
    /**
     * The URI this client is supposed to connect to.
     */
    private URI uri;
    /**
     * The WebSocket instance this client object wraps.
     */
    private WebSocket conn;

    // CONSTRUCTOR /////////////////////////////////////////////////////////////
    /**
     * Nullary constructor. You must call <var>setURI</var> before calling
     * <var>connect</var>, otherwise an exception will be thrown.
     */
    public WebSocketClient() {}

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The client does not attampt to connect automatically. You
     * must call <var>connect</var> first to initiate the socket connection.
     * @param serverUri
     */
    public WebSocketClient(URI serverUri) {
        setURI(serverUri);
    }

    // PUBLIC INSTANCE METHODS /////////////////////////////////////////////////
    /**
     * Sets this WebSocketClient to connect to the specified URI.
     *
     * TODO: Throw an exception if this is called while the socket thread is
     * running.
     *
     * @param uri
     */
    public void setURI(URI uri) {
        this.uri = uri;
    }

    /**
     * Gets the URI that this WebSocketClient is connected to (or should attempt
     * to connect to).
     * @return The URI for this WebSocketClient.
     */
    public URI getURI() {
        return uri;
    }

    /**
     * Starts a background thread that attempts and maintains a WebSocket
     * connection to the URI specified in the constructor or via <var>setURI</var>.
     * <var>setURI</var>.
     */
    public void connect() {
        if (this.uri == null) throw new NullPointerException("WebSocketClient must have a URI to connect to. See WebSocketClient#setURI");

        (new Thread(this)).start();
    }

    /**
     * Calls <var>close</var> on the underlying SocketChannel, which in turn
     * closes the socket connection, and ends the client socket thread.
     * @throws IOException When socket related I/O errors occur.
     */
    public void close() throws IOException {
        conn.close();
    }

    /**
     * Sends <var>text</var> to the connected WebSocket server.
     * @param text The String to send across the socket to the WebSocket server.
     * @throws IOException When socket related I/O errors occur.
     */
    public void send(String text) throws IOException {
        conn.send(text);
    }

    // Runnable IMPLEMENTATION /////////////////////////////////////////////////
    public void run() {
        try {
            int port = uri.getPort();
            if (port == -1) {
                port = 80;
            }

            // The WebSocket constructor expects a SocketChannel that is
            // non-blocking, and has a Selector attached to it.
            SocketChannel client = SocketChannel.open();
            client.configureBlocking(false);
            client.connect(new InetSocketAddress(uri.getHost(), port));

            Selector selector = Selector.open();

            this.conn = new WebSocket(client, this);
            client.register(selector, client.validOps());

            // Continuous loop that is only supposed to end when close is called
            while (selector.select(500) > 0) {

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator i = keys.iterator();

                while (i.hasNext()) {
                    SelectionKey key = (SelectionKey)i.next();
                    i.remove();

                    // When 'conn' has connected to the host
                    if (key.isConnectable()) {
                        // Ensure connection is finished
                        if (client.isConnectionPending())
                            client.finishConnect();

                        // Now send WebSocket client-side handshake
                        String path = "/" + uri.getPath();
                        String host = uri.getHost() + (port != 80 ? ":" + port : "");
                        String origin = null; // I don't know what to put here!?
                        String request = "GET "+path+" HTTP/1.1\r\n" +
                            "Upgrade: WebSocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Host: "+host+"\r\n" +
                            "Origin: "+origin+"\r\n" +
                            //extraHeaders.toString() +
                            "\r\n";
                        conn.socketChannel().write(ByteBuffer.wrap(request.getBytes(WebSocket.UTF8_CHARSET)));
                    }

                    // When 'conn' has recieved some data
                    if (key.isReadable()) {
                        conn.handleRead();
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    public boolean onHandshakeRecieved(WebSocket conn, String handshake,byte[] key3) throws IOException {
        // TODO: Do some parsing of the returned handshake, and close connection
        // (return false) if we recieved anything unexpected.
        return true;
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
    public void onClose(WebSocket conn) {
        onClose();
    }


    // ABTRACT METHODS /////////////////////////////////////////////////////////
    public abstract void onMessage(String message);
    public abstract void onOpen();
    public abstract void onClose();
}
