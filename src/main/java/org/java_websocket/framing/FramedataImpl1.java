package org.java_websocket.framing;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.util.Charsetfunctions;

public class FramedataImpl1 implements FrameBuilder {
	/**
	 * Attribute for just an empty array
	 */
	private static byte[] emptyarray = {};

	/**
	 * Indicates that this is the final fragment in a message.
	 */
	protected boolean fin;
	/**
	 * Defines the interpretation of the "Payload data".
	 */
	protected Opcode optcode;

	/**
	 * The unmasked "Payload data" which was sent in this frame
	 */
	private ByteBuffer unmaskedpayload;

	/**
	 * Defines whether the "Payload data" is masked.
	 */
	protected boolean transferemasked;

	/**
	 * Constructor for a FramedataImpl without any attributes set
	 */
	public FramedataImpl1() {
	}

	/**
	 * Constructor for a FramedataImpl without any attributes set apart from the opcode
	 * @param op the opcode to use
	 */
	public FramedataImpl1( Opcode op ) {
		this.optcode = op;
		unmaskedpayload = ByteBuffer.wrap( emptyarray );
	}

	/**
	 * Helper constructor which helps to create "echo" frames.
	 * The new object will use the same underlying payload data.
	 * @param f The Framedata to copy data from
	 */
	public FramedataImpl1( Framedata f ) {
		fin = f.isFin();
		optcode = f.getOpcode();
		unmaskedpayload = f.getPayloadData();
		transferemasked = f.getTransfereMasked();
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
	public void setFin( boolean fin ) {
		this.fin = fin;
	}

	@Override
	public void setOptcode( Opcode optcode ) {
		this.optcode = optcode;
	}

	@Override
	public void setPayload( ByteBuffer payload ) throws InvalidDataException {
		unmaskedpayload = payload;
	}

	@Override
	public void setTransferemasked( boolean transferemasked ) {
		this.transferemasked = transferemasked;
	}

	@Override
	public void append( Framedata nextframe )  {
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
		return "Framedata{ optcode:" + getOpcode() + ", fin:" + isFin() + ", payloadlength:[pos:" + unmaskedpayload.position() + ", len:" + unmaskedpayload.remaining() + "], payload:" + Arrays.toString( Charsetfunctions.utf8Bytes( new String( unmaskedpayload.array() ) ) ) + "}";
	}

}
