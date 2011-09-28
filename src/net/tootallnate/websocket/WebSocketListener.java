package net.tootallnate.websocket;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Implemented by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>.
 * The methods within are called by <tt>WebSocket</tt>.
 * @author Nathan Rajlich
 */
interface WebSocketListener {
  
  /**
   * Called when the socket connection is first established, and the WebSocket
   * handshake has been recieved.
   */
  public HandshakeBuilder onHandshakeRecievedAsServer( WebSocket conn , Draft draft , Handshakedata request ) throws IOException;
  public boolean onHandshakeRecievedAsClient( WebSocket conn , Handshakedata request ,  Handshakedata response ) throws IOException;
  
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
   * Triggered on any IOException error. This method should be overridden for custom 
   * implementation of error handling (e.g. when network is not available). 
   * @param ex
   */
  public void onError( Throwable ex );
}
