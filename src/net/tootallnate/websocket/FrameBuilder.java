package net.tootallnate.websocket;

public interface FrameBuilder extends Framedata {

	public abstract void setFin( boolean fin );

	public abstract void setOptcode( Opcode optcode );

	public abstract void setPayload( byte[] payload );

	public abstract void setTransferemasked( boolean transferemasked );

}