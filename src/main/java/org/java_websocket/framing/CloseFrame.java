package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.util.ByteBufferUtils;
import org.java_websocket.util.Charsetfunctions;

import java.nio.ByteBuffer;

public class CloseFrame extends ControlFrame {

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
	public static final int ABNORMAL_CLOSE = 1006;
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
	/**
	 * 1011 indicates that a server is terminating the connection because
	 * it encountered an unexpected condition that prevented it from
	 * fulfilling the request.
	 **/
	public static final int UNEXPECTED_CONDITION = 1011;
	/**
	 * 1015 is a reserved value and MUST NOT be set as a status code in a
	 * Close control frame by an endpoint. It is designated for use in
	 * applications expecting a status code to indicate that the
	 * connection was closed due to a failure to perform a TLS handshake
	 * (e.g., the server certificate can't be verified).
	 **/
	public static final int TLS_ERROR = 1015;

	/**
	 * The connection had never been established
	 */
	public static final int NEVER_CONNECTED = -1;
	public static final int BUGGYCLOSE = -2;
	public static final int FLASHPOLICY = -3;


	/**
	 * The close code used in this close frame
	 */
	private int code;

	/**
	 * The close message used in this close frame
	 */
	private String reason;

	/**
	 * Constructor for a close frame
	 * <p>
	 * Using opcode closing and fin = true
	 */
	public CloseFrame() {
		super( Opcode.CLOSING );
	}

	/**
	 * Constructor for a close frame
	 * <p>
	 * Using opcode closing and fin = true
	 *
	 * @param code The close code causing this close frame
	 */
	public CloseFrame( int code ) throws InvalidDataException {
		super( Opcode.CLOSING );
		setCodeAndMessage( code, "" );
	}

	/**
	 * Constructor for a close frame
	 * <p>
	 * Using opcode closing and fin = true
	 *
	 * @param code The close code causing this close frame
	 * @param m    The close message explaining this close frame a bit more
	 */
	public CloseFrame( int code, String m ) throws InvalidDataException {
		super( Opcode.CLOSING );
		setCodeAndMessage( code, m );
	}

	private void setCodeAndMessage( int code, String m ) throws InvalidDataException {
		if( m == null ) {
			m = "";
		}
		// CloseFrame.TLS_ERROR is not allowed to be transfered over the wire
		if( code == CloseFrame.TLS_ERROR ) {
			code = CloseFrame.NOCODE;
			m = "";
		}
		if( code == CloseFrame.NOCODE ) {
			if( 0 < m.length() ) {
				throw new InvalidDataException( PROTOCOL_ERROR, "A close frame must have a closecode if it has a reason" );
			}
			return;// empty payload
		}
		//Intentional check for code != CloseFrame.TLS_ERROR just to make sure even if the code earlier changes
		if( ( code > CloseFrame.UNEXPECTED_CONDITION && code < 3000 && code != CloseFrame.TLS_ERROR ) ) {
			throw new InvalidDataException( PROTOCOL_ERROR, "Trying to send an illegal close code!" );
		}

		byte[] by = Charsetfunctions.utf8Bytes( m );
		ByteBuffer buf = ByteBuffer.allocate( 4 );
		buf.putInt( code );
		buf.position( 2 );
		ByteBuffer pay = ByteBuffer.allocate( 2 + by.length );
		pay.put( buf );
		pay.put( by );
		pay.rewind();
		setPayload( pay );
	}

	private void initCloseCode() throws InvalidFrameException {
		code = CloseFrame.NOCODE;
		ByteBuffer payload = super.getPayloadData();
		payload.mark();
		if( payload.remaining() >= 2 ) {
			ByteBuffer bb = ByteBuffer.allocate( 4 );
			bb.position( 2 );
			bb.putShort( payload.getShort() );
			bb.position( 0 );
			code = bb.getInt();

			if( code == CloseFrame.ABNORMAL_CLOSE || code == CloseFrame.TLS_ERROR || code == CloseFrame.NOCODE || code > 4999 || code < 1000 || code == 1004 ) {
				throw new InvalidFrameException( "closecode must not be sent over the wire: " + code );
			}
		}
		payload.reset();
	}

	public int getCloseCode() {
		return code;
	}

	private void initMessage() throws InvalidDataException {
		if( code == CloseFrame.NOCODE ) {
			reason = Charsetfunctions.stringUtf8( super.getPayloadData() );
		} else {
			ByteBuffer b = super.getPayloadData();
			int mark = b.position();// because stringUtf8 also creates a mark
			try {
				b.position( b.position() + 2 );
				reason = Charsetfunctions.stringUtf8( b );
			} catch ( IllegalArgumentException e ) {
				throw new InvalidFrameException( e );
			} finally {
				b.position( mark );
			}
		}
	}

	public String getMessage() {
		return reason;
	}

	@Override
	public String toString() {
		return super.toString() + "code: " + code;
	}

	@Override
	public void isValid() throws InvalidDataException {
		super.isValid();
		initCloseCode();
		initMessage();
	}

	@Override
	public ByteBuffer getPayloadData() {
		if( code == NOCODE )
			return ByteBufferUtils.getEmptyByteBuffer();
		return super.getPayloadData();
	}

}
