package net.tootallnate.websocket;

import net.tootallnate.websocket.exeptions.InvalidDataException;

public interface FrameBuilder extends Framedata {

	public abstract void setFin( boolean fin );

	public abstract void setOptcode( Opcode optcode );

	public abstract void setPayload( byte[] payload ) throws InvalidDataException;

	public abstract void setTransferemasked( boolean transferemasked );

}