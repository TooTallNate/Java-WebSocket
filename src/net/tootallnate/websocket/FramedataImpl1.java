package net.tootallnate.websocket;

import java.nio.ByteBuffer;

public class FramedataImpl1 implements FrameBuilder {
	private boolean fin;
	private Opcode optcode;
	private ByteBuffer unmaskedpayload;
	private boolean transferemasked;
	
	public FramedataImpl1() {
	}

	@Override
	public boolean isFin( ) {
		return fin;
	}

	@Override
	public Opcode getOpcode( ) {
		return optcode;
	}
	
	@Override
	public boolean getTransfereMasked( ) {
		return transferemasked;
	}

	@Override
	public byte[] getPayloadData( ) {
		return unmaskedpayload.array ();
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
	public void setPayload( byte[] payload ) {
		unmaskedpayload=ByteBuffer.wrap ( payload );
	}
	

	@Override
	public void setTransferemasked( boolean transferemasked ) {
		this.transferemasked = transferemasked;
	}


	@Override
	public void append( Framedata nextframe ) {
		if( unmaskedpayload == null ){
			unmaskedpayload = ByteBuffer.wrap ( nextframe.getPayloadData () );
		}
		else{
			ByteBuffer tmp = ByteBuffer.allocate ( nextframe.getPayloadData ().length + unmaskedpayload.capacity () );
			tmp.put ( unmaskedpayload.array () );
			tmp.put ( nextframe.getPayloadData () );
		}
	}
	
	

}
