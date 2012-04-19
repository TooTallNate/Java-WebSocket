package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;

public interface CloseFrame extends Framedata {
		/**
		 * indicates a normal closure, meaning whatever purpose the
		 * connection was established for has been fulfilled.
		 */
		public static final int NORMAL = 1000;
		/**
		 * 1001 indicates that an endpoint is "going away", such as a server
		 * going down, or a browser having navigated away from a page.
		 */
		public static final int GOING_AWAY = 1001;
		/**
		 * 1002 indicates that an endpoint is terminating the connection due
		 * to a protocol error.
		 */
		public static final int PROTOCOL_ERROR = 1002;
		/**
		 * 1003 indicates that an endpoint is terminating the connection
		 * because it has received a type of data it cannot accept (e.g. an
		 * endpoint that understands only text data MAY send this if it
		 * receives a binary message).
		 */
		public static final int REFUSE = 1003;
		/*1004: Reserved. The specific meaning might be defined in the future.*/
		/**
		 * 1005 is a reserved value and MUST NOT be set as a status code in a
		 * Close control frame by an endpoint. It is designated for use in
		 * applications expecting a status code to indicate that no status
		 * code was actually present.
		 */
		public static final int NOCODE = 1005;
		/**
		 * 1006 is a reserved value and MUST NOT be set as a status code in a
		 * Close control frame by an endpoint. It is designated for use in
		 * applications expecting a status code to indicate that the
		 * connection was closed abnormally, e.g. without sending or
		 * receiving a Close control frame.
		 */
		public static final int ABNOROMAL_CLOSE = 1006;
		/**
		 * 1007 indicates that an endpoint is terminating the connection
		 * because it has received data within a message that was not
		 * consistent with the type of the message (e.g., non-UTF-8 [RFC3629]
		 * data within a text message).
		 */
		public static final int NO_UTF8 = 1007;
		/**
		 * 1008 indicates that an endpoint is terminating the connection
		 * because it has received a message that violates its policy. This
		 * is a generic status code that can be returned when there is no
		 * other more suitable status code (e.g. 1003 or 1009), or if there
		 * is a need to hide specific details about the policy.
		 */
		public static final int POLICY_VALIDATION = 1008;
		/**
		 * 1009 indicates that an endpoint is terminating the connection
		 * because it has received a message which is too big for it to
		 * process.
		 */
		public static final int TOOBIG = 1009;
		/**
		 * 1010 indicates that an endpoint (client) is terminating the
		 * connection because it has expected the server to negotiate one or
		 * more extension, but the server didn't return them in the response
		 * message of the WebSocket handshake. The list of extensions which
		 * are needed SHOULD appear in the /reason/ part of the Close frame.
		 * Note that this status code is not used by the server, because it
		 * can fail the WebSocket handshake instead.
		 */
	public static final int EXTENSION = 1010;

	/** The connection had never been established */
	public static final int NEVERCONNECTED = -1;
	public static final int BUGGYCLOSE = -2;
	public static final int FLASHPOLICY = -3;

	public int getCloseCode() throws InvalidFrameException;
	public String getMessage() throws InvalidDataException;
}
