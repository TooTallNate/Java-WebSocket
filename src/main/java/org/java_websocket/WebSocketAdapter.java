package org.java_websocket;

import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class default implements all methods of the WebSocketListener that can be overridden optionally when advances functionalities is needed.<br>
 **/
public abstract class WebSocketAdapter implements WebSocketListener {

	/**
	 * Attribute which allows you to deactivate the Nagle's algorithm
	 */
	private boolean tcpNoDelay;

	/**
	 * Attribute for a timer allowing to check for lost connections
	 */
	private Timer connectionLostTimer;
	/**
	 * Attribute for a timertask allowing to check for lost connections
	 */
	private TimerTask connectionLostTimerTask;

	/**
	 * Attribute for the lost connection check interval
	 */
	private int connectionLostTimeout = 60;

	/**
	 * Get the interval checking for lost connections
	 * Default is 60 seconds
	 * @return the interval
	 */
	public int getConnectionLostTimeout() {
		return connectionLostTimeout;
	}

	/**
	 * Setter for the interval checking for lost connections
	 * A value >= 0 results in the check to be deactivated
	 *
	 * @param connectionLostTimeout the interval in seconds
	 */
	public void setConnectionLostTimeout( int connectionLostTimeout ) {
		this.connectionLostTimeout = connectionLostTimeout;
		if (this.connectionLostTimeout <= 0) {
			stopConnectionLostTimer();
		} else {
			startConnectionLostTimer();
		}
	}

	/**
	 * Stop the connection lost timer
	 */
	protected void stopConnectionLostTimer() {
		if (connectionLostTimer != null ||connectionLostTimerTask != null) {
			if( WebSocketImpl.DEBUG )
				System.out.println( "Connection lost timer stoped" );
			cancelConnectionLostTimer();
		}
	}
	/**
	 * Start the connection lost timer
	 */
	protected void startConnectionLostTimer() {
		if (this.connectionLostTimeout <= 0) {
			if (WebSocketImpl.DEBUG)
				System.out.println("Connection lost timer deactivated");
			return;
		}
		if (WebSocketImpl.DEBUG)
			System.out.println("Connection lost timer started");
		cancelConnectionLostTimer();
		connectionLostTimer = new Timer();
		connectionLostTimerTask = new TimerTask() {
			@Override
			public void run() {
				for (WebSocket conn: connections()) {
					conn.sendPing();
				}
			}
		};
		connectionLostTimer.scheduleAtFixedRate( connectionLostTimerTask,connectionLostTimeout * 1000, connectionLostTimeout * 1000 );
	}

	/**
	 * Getter to get all the currently available connections
	 * @return the currently available connections
	 */
	protected abstract Collection<WebSocket> connections();

	/**
	 * Cancel any running timer for the connection lost detection
	 */
	private void cancelConnectionLostTimer() {
		if( connectionLostTimer != null ) {
			connectionLostTimer.cancel();
			connectionLostTimer = null;
		}
		if( connectionLostTimerTask != null ) {
			connectionLostTimerTask.cancel();
			connectionLostTimerTask = null;
		}
	}

	/**
	 * Tests if TCP_NODELAY is enabled.
	 *
	 * @return a boolean indicating whether or not TCP_NODELAY is enabled for new connections.
	 */
	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	/**
	 * Setter for tcpNoDelay
	 * <p>
	 * Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm) for new connections
	 *
	 * @param tcpNoDelay true to enable TCP_NODELAY, false to disable.
	 */
	public void setTcpNoDelay( boolean tcpNoDelay ) {
		this.tcpNoDelay = tcpNoDelay;
	}


	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 *
	 * @see org.java_websocket.WebSocketListener#onWebsocketHandshakeReceivedAsServer(WebSocket, Draft, ClientHandshake)
	 */
	@Override
	public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer( WebSocket conn, Draft draft, ClientHandshake request ) throws InvalidDataException {
		return new HandshakeImpl1Server();
	}

	@Override
	public void onWebsocketHandshakeReceivedAsClient( WebSocket conn, ClientHandshake request, ServerHandshake response ) throws InvalidDataException {
	}

	/**
	 * This default implementation does not do anything which will cause the connections to always progress.
	 *
	 * @see org.java_websocket.WebSocketListener#onWebsocketHandshakeSentAsClient(WebSocket, ClientHandshake)
	 */
	@Override
	public void onWebsocketHandshakeSentAsClient( WebSocket conn, ClientHandshake request ) throws InvalidDataException {
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it
	 *
	 * @see org.java_websocket.WebSocketListener#onWebsocketMessageFragment(WebSocket, Framedata)
	 */
	@Override
	public void onWebsocketMessageFragment( WebSocket conn, Framedata frame ) {
	}

	/**
	 * This default implementation will send a pong in response to the received ping.
	 * The pong frame will have the same payload as the ping frame.
	 *
	 * @see org.java_websocket.WebSocketListener#onWebsocketPing(WebSocket, Framedata)
	 */
	@Override
	public void onWebsocketPing( WebSocket conn, Framedata f ) {
		FramedataImpl1 resp = new FramedataImpl1( f );
		resp.setOptcode( Opcode.PONG );
		conn.sendFrame( resp );
	}

	/**
	 * This default implementation does not do anything. Go ahead and overwrite it.
	 *
	 * @see org.java_websocket.WebSocketListener#onWebsocketPong(WebSocket, Framedata)
	 */
	@Override
	public void onWebsocketPong( WebSocket conn, Framedata f ) {
	}

	/**
	 * Gets the XML string that should be returned if a client requests a Flash
	 * security policy.
	 * <p>
	 * The default implementation allows access from all remote domains, but
	 * only on the port that this WebSocketServer is listening on.
	 * <p>
	 * This is specifically implemented for gitime's WebSocket client for Flash:
	 * http://github.com/gimite/web-socket-js
	 *
	 * @return An XML String that comforts to Flash's security policy. You MUST
	 * not include the null char at the end, it is appended automatically.
	 * @throws InvalidDataException thrown when some data that is required to generate the flash-policy like the websocket local port could not be obtained e.g because the websocket is not connected.
	 */
	@Override
	public String getFlashPolicy( WebSocket conn ) throws InvalidDataException {
		InetSocketAddress adr = conn.getLocalSocketAddress();
		if( null == adr ) {
			throw new InvalidHandshakeException( "socket not bound" );
		}

		return "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"" + adr.getPort() + "\" /></cross-domain-policy>\0";
	}


}
