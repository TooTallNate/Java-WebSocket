package net.tootallnate.websocket.drafts;

import java.util.Random;

import com.sun.org.apache.regexp.internal.recompile;

import net.tootallnate.websocket.HandshakeBuilder;
import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.Draft.HandshakeState;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;
import sun.misc.BASE64Encoder;

public class Draft_17 extends Draft_10 {
	@Override
	public HandshakeState acceptHandshakeAsServer( Handshakedata handshakedata ) throws InvalidHandshakeException {
		int v = readVersion( handshakedata );
		if( v == 13 )
			return HandshakeState.MATCHED;
		return HandshakeState.NOT_MATCHED;
	}
	
	@Override
	public HandshakeBuilder postProcessHandshakeRequestAsClient( HandshakeBuilder request ) {
		 super.postProcessHandshakeRequestAsClient( request );
		 request.put ( "Sec-WebSocket-Version" , "13" );//overwriting the previous
		 return request;
	}
}
