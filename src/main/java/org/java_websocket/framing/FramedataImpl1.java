package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.util.ByteBufferUtils;
import org.java_websocket.util.Charsetfunctions;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class FramedataImpl1 implements Framedata {

	/**
	 * Indicates that this is the final fragment in a message.
	 */
	private boolean fin;
	/**
	 * Defines the interpretation of the "Payload data".
	 */
	private Opcode optcode;

	/**
	 * The unmasked "Payload data" which was sent in this frame
	 */
	private ByteBuffer unmaskedpayload;

	/**
	 * Defines whether the "Payload data" is masked.
	 */
	private boolean transferemasked;

	/**
	 * Indicates that the rsv1 bit is set or not
	 */
	private boolean rsv1;

	/**
	 * Indicates that the rsv2 bit is set or not
	 */
	private boolean rsv2;

	/**
	 * Indicates that the rsv3 bit is set or not
	 */
	private boolean rsv3;

	/**
	 * Check if the frame is valid due to specification
	 *
	 * @throws InvalidDataException
	 */
	public abstract void isValid() throws InvalidDataException;

	/**
	 * Constructor for a FramedataImpl without any attributes set apart from the opcode
	 *
	 * @param op the opcode to use
	 */
	public FramedataImpl1( Opcode op ) {
		optcode = op;
		unmaskedpayload = ByteBufferUtils.getEmptyByteBuffer();
		fin = true;
		transferemasked = false;
		rsv1 = false;
		rsv2 = false;
		rsv3 = false;
	}

	@Override
	public boolean isRSV1() {
		return rsv1;
	}

	@Override
	public boolean isRSV2() {
		return rsv2;
	}

	@Override
	public boolean isRSV3() {
		return rsv3;
	}

	@Override
	public boolean isFin() {
		return fin;
	}

	@Override
	public Opcode getOpcode() {
		return optcode;
	}

	@Override
	public boolean getTransfereMasked() {
		return transferemasked;
	}

	@Override
	public ByteBuffer getPayloadData() {
		return unmaskedpayload;
	}

	@Override
	public void append( Framedata nextframe ) {
		ByteBuffer b = nextframe.getPayloadData();
		if( unmaskedpayload == null ) {
			unmaskedpayload = ByteBuffer.allocate( b.remaining() );
			b.mark();
			unmaskedpayload.put( b );
			b.reset();
		} else {
			b.mark();
			unmaskedpayload.position( unmaskedpayload.limit() );
			unmaskedpayload.limit( unmaskedpayload.capacity() );

			if( b.remaining() > unmaskedpayload.remaining() ) {
				ByteBuffer tmp = ByteBuffer.allocate( b.remaining() + unmaskedpayload.capacity() );
				unmaskedpayload.flip();
				tmp.put( unmaskedpayload );
				tmp.put( b );
				unmaskedpayload = tmp;

			} else {
				unmaskedpayload.put( b );
			}
			unmaskedpayload.rewind();
			b.reset();
		}
		fin = nextframe.isFin();
	}

	@Override
	public String toString() {
		return "Framedata{ optcode:" + getOpcode() + ", fin:" + isFin() + ", rsv1:" + isRSV1() + ", rsv2:" + isRSV2() + ", rsv3:" + isRSV3() + ", payloadlength:[pos:" + unmaskedpayload.position() + ", len:" + unmaskedpayload.remaining() + "], payload:" + Arrays.toString( Charsetfunctions.utf8Bytes( new String( unmaskedpayload.array() ) ) ) + "}";
	}

	public void setPayload( ByteBuffer payload ) {
		this.unmaskedpayload = payload;
	}

	public void setFin( boolean fin ) {
		this.fin = fin;
	}

	public void setTransferemasked( boolean transferemasked ) {
		this.transferemasked = transferemasked;
	}

	public static FramedataImpl1 get( Opcode optcode ) {
		switch(optcode) {
			case PING:
				return new PingFrame();
			case PONG:
				return new PongFrame();
			case TEXT:
				return new TextFrame();
			case BINARY:
				return new BinaryFrame();
			case CLOSING:
				return new CloseFrame();
			case CONTINUOUS:
				return new ContinuousFrame();
			default:
				throw new IllegalArgumentException( "Supplied opcode is invalid" );
		}
	}
}
