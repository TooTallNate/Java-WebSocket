
import java.io.IOException;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class ChatServer extends WebSocketServer {
    
    public void onClientOpen(WebSocket conn) {
        try {
            this.sendToAll(conn + " entered the room!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + " entered the room!");
    }

    public void onClientClose(WebSocket conn) {
        try {
            this.sendToAll(conn + " has left the room!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + " has left the room!");
    }

    public void onClientMessage(WebSocket conn, String message) {
        try {
            this.sendToAll(conn + ": " + message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + ": " + message);
    }

    public static void main(String[] args) {
        ChatServer s = new ChatServer();
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());
    }
}
