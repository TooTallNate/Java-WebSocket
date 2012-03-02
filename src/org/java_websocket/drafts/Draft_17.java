package org.java_websocket.drafts;

import org.java_websocket.Draft.HandshakeState;
import org.java_websocket.HandshakeBuilder;
import org.java_websocket.Handshakedata;
import org.java_websocket.exeptions.InvalidHandshakeException;

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
		request.put( "Sec-WebSocket-Version", "13" );// overwriting the previous
		return request;
	}

}
