package org.java_websocket.framing;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.java_websocket.exeptions.InvalidDataException;
import org.java_websocket.exeptions.InvalidFrameException;
import org.java_websocket.util.Charsetfunctions;

public class FramedataImpl1 implements FrameBuilder {
	protected static byte[] emptyarray = {};
	protected boolean fin;
	protected Opcode optcode;
	private ByteBuffer unmaskedpayload;
	protected boolean transferemasked;

	public FramedataImpl1() {
	}

	public FramedataImpl1( Opcode op ) {
		this.optcode = op;
		unmaskedpayload = ByteBuffer.wrap( emptyarray );
	}

	/**
	 * Helper constructor which helps to create "echo" frames.
	 * The new object will use the same underlying payload data.
	 **/
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
	public void append( Framedata nextframe ) throws InvalidFrameException {
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
			if( unmaskedpayload.hasRemaining() )
				unmaskedpayload.put( b );
			if( b.hasRemaining() ) {
				ByteBuffer tmp = ByteBuffer.allocate( b.remaining() + unmaskedpayload.capacity() );
				unmaskedpayload.flip();
				tmp.put( unmaskedpayload );
				tmp.put( b );
				unmaskedpayload = tmp;

			}
			unmaskedpayload.rewind();
			b.reset();
		}
		fin = nextframe.isFin();
	}

	@Override
	public String toString() {
		return "Framedata{ optcode:" + getOpcode() + ", fin:" + isFin() + ", payloadlength:" + unmaskedpayload.limit() + ", payload:" + Arrays.toString(Charsetfunctions.utf8Bytes( new String( unmaskedpayload.array() ) ) ) + "}";
	}

}
