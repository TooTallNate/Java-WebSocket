
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 
 * @author Nathan Rajlich
 */
public abstract class WebSocketServer implements Runnable, WebSocketListener {

    /**
     * Holds the list of active WebSocket connections. "Active" means able to
     * be written to, or read from.
     */
    private final CopyOnWriteArraySet<WebSocket> connections;
    /**
     * The port number that this WebSocket server should listen on. Default is
     * 80 (HTTP).
     */
    private final int port;
    /**
     * The Thread that is watching the socket for activity.
     */
    private Thread serverThread;
    /**
     * The socket channel for this WebSocket server.
     */
    private ServerSocketChannel server;
    private Selector selector;

    public WebSocketServer() {
        this(80);
    }

    public WebSocketServer(int port) {
        this.port = port;
        this.connections = new CopyOnWriteArraySet<WebSocket>();
    }

    public void start() {
        serverThread = new Thread(this);
        serverThread.start();
    }

    public void stop() throws IOException {
        this.server.close();
    }

    public void sendToAll(String text) throws IOException {
        for (WebSocket c : this.connections) {
            c.sendFrame(text);
        }
    }

    public int getPort() {
        return port;
    }

    
    // Runnable IMPLEMENTATION /////////////////////////////////////////////////
    public void run() {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new java.net.InetSocketAddress(port));

            selector = Selector.open();
            server.register(selector, server.validOps());

            while(true) {
                // Waiting for events
                //System.out.println("waiting for events...");
                selector.select();
                // Get keys
                Set keys = selector.selectedKeys();
                Iterator i = keys.iterator();

                // For each keys...
                while(i.hasNext()) {
                    SelectionKey key = (SelectionKey) i.next();

                    // Remove the current key
                    i.remove();

                    // if isAccetable = true
                    // then a client required a connection
                    if (key.isAcceptable()) {
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        WebSocket c = new WebSocket(client, this);
                        client.register(selector, SelectionKey.OP_READ, c);
                    }

                    // if isReadable = true
                    // then the server is ready to read
                    if (key.isReadable()) {
                        WebSocket conn = (WebSocket)key.attachment();
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
     * Called by a {@link WebSocket} instance when a client connection has
     * finished sending a handshake. This method verifies that the handshake is
     * a valid WebSocket cliend request. Then sends a WebSocket server handshake
     * if it is valid, or closes the connection if it is not.
     * @param conn The {@link WebSocket} instance who's handshake has been recieved.
     * @param handshake The entire UTF-8 decoded handshake from the connection.
     * @return True if the client sent a valid WebSocket handshake and this server
     *         successfully sent a WebSocket server handshake, false otherwise.
     * @throws IOException When socket related I/O errors occur.
     */
    public boolean onHandshakeRecieved(WebSocket conn, String handshake) throws IOException {
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
        Properties p = new Properties();
        for (int i=1; i<requestLines.length; i++) {
            line = requestLines[i];
            int firstColon = line.indexOf(":");
            p.setProperty(line.substring(0, firstColon).trim(), line.substring(firstColon+1).trim());
        }
        String prop = p.getProperty("Upgrade");
        if (prop == null || !prop.equals("WebSocket")) {
            isWebSocketRequest = false;
        }
        prop = p.getProperty("Connection");
        if (prop == null || !prop.equals("Upgrade")) {
            isWebSocketRequest = false;
        }

        // If we've determined that this is a valid WebSocket request, send a
        // valid WebSocket server handshake, then return true to keep connection alive.
        if (isWebSocketRequest) {
            String responseHandshake = "HTTP/1.1 101 Web Socket Protocol Handshake\r\n" +
                                       "Upgrade: WebSocket\r\n" +
                                       "Connection: Upgrade\r\n";
            responseHandshake += "WebSocket-Origin: " + p.getProperty("Origin") + "\r\n";
            responseHandshake += "WebSocket-Location: ws://" + p.getProperty("Host") + path + "\r\n";
            if (p.containsKey("WebSocket-Protocol")) {
                responseHandshake += "WebSocket-Protocol: " + p.getProperty("WebSocket-Protocol") + "\r\n";
            }
            responseHandshake += "\r\n"; // Signifies end of handshake
            conn.socketChannel().write(ByteBuffer.wrap(responseHandshake.getBytes(WebSocket.UTF8_CHARSET)));
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
        this.connections.add(conn);
        onClientOpen(conn);
    }
    
    public void onClose(WebSocket conn) {
        this.connections.remove(conn);
        onClientClose(conn);
    }

    // ABTRACT METHODS /////////////////////////////////////////////////////////
    public abstract void onClientOpen(WebSocket conn);
    public abstract void onClientClose(WebSocket conn);
    public abstract void onClientMessage(WebSocket conn, String message);
}
