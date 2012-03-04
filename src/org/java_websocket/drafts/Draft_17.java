package org.java_websocket.drafts;

import org.java_websocket.ClientHandshake;
import org.java_websocket.ClientHandshakeBuilder;
import org.java_websocket.Draft.HandshakeState;
import org.java_websocket.exeptions.InvalidHandshakeException;

public class Draft_17 extends Draft_10 {
	@Override
	public HandshakeState acceptHandshakeAsServer( ClientHandshake handshakedata ) throws InvalidHandshakeException {
		int v = readVersion( handshakedata );
		if( v == 13 )
			return HandshakeState.MATCHED;
		return HandshakeState.NOT_MATCHED;
	}

	@Override
	public ClientHandshakeBuilder postProcessHandshakeRequestAsClient( ClientHandshakeBuilder request ) {
		super.postProcessHandshakeRequestAsClient( request );
		request.put( "Sec-WebSocket-Version", "13" );// overwriting the previous
		return request;
	}

}
