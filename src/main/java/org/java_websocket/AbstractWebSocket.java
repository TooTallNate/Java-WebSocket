/*
 * Copyright (c) 2010-2017 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.java_websocket;

import org.java_websocket.framing.CloseFrame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Base class for additional implementations for the server as well as the client
 */
public abstract class AbstractWebSocket extends WebSocketAdapter {

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
     * A value lower or equal 0 results in the check to be deactivated
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
                System.out.println( "Connection lost timer stopped" );
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

			/**
			 * Keep the connections in a separate list to not cause deadlocks
			 */
			private ArrayList<WebSocket> connections = new ArrayList<WebSocket>(  );
            @Override
            public void run() {
                connections.clear();
                connections.addAll( connections() );
				long current = (System.currentTimeMillis()-(connectionLostTimeout * 1500));
				WebSocketImpl webSocketImpl;
				for( WebSocket conn : connections ) {
					if (conn instanceof WebSocketImpl) {
						webSocketImpl = (WebSocketImpl)conn;
						if( webSocketImpl.getLastPong() < current ) {
							if (WebSocketImpl.DEBUG)
								System.out.println("Closing connection due to no pong received: " + conn.toString());
							webSocketImpl.closeConnection( CloseFrame.ABNORMAL_CLOSE , false );
						} else {
							webSocketImpl.sendPing();
						}
					}
				}
				connections.clear();
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

}
