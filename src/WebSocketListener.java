
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Implemented by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>.
 * The methods within are called by <tt>WebSocket</tt>.
 * @author Nathan Rajlich
 */
interface WebSocketListener {
	/**
   * Enum for WebSocket Draft
   */
  public static enum Draft {
    AUTO,
    DRAFT75,
    DRAFT76
  }
  
  /**
   * Called when the socket connection is first established, and the WebSocket
   * handshake has been recieved. This method should parse the
   * <var>handshake</var>, and return a boolean indicating whether or not the
   * connection is a valid WebSocket connection.
   * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
   * @param handshake The entire UTF-8 decoded handshake from the connection.
   * @return <var>true</var> if the handshake is valid, and <var>onOpen</var>
   *         should be immediately called afterwards. <var>false</var> if the
   *         handshake was invalid, and the connection should be terminated.
   * @throws NoSuchAlgorithmException 
   */
  public boolean onHandshakeRecieved(WebSocket conn, String handshake,byte[] key3) throws IOException, NoSuchAlgorithmException;
  
  /**
   * Called when an entire text frame has been recieved. Do whatever you want
   * here...
   * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
   * @param message The UTF-8 decoded message that was recieved.
   */
  public void onMessage(WebSocket conn, String message);
  
  /**
   * Called after <var>onHandshakeRecieved</var> returns <var>true</var>.
   * Indicates that a complete WebSocket connection has been established,
   * and we are ready to send/recieve data.
   * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
   */
  public void onOpen(WebSocket conn);
  
  /**
   * Called after <tt>WebSocket#close</tt> is explicity called, or when the
   * other end of the WebSocket connection is closed.
   * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
   */
  public void onClose(WebSocket conn);
  
  /**
   * Called to retrieve the Draft of this listener.
   */
  public Draft getDraft();  
}
