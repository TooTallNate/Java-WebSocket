import java.net.URI;

import org.java_websocket.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class EmptyClient extends WebSocketClient {

	public EmptyClient( URI serverUri , Draft draft ) {
		super( serverUri, draft );
	}

	public EmptyClient( URI serverURI ) {
		super( serverURI );
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
	}

	@Override
	public void onMessage( String message ) {
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
	}

	@Override
	public void onError( Exception ex ) {
	}

}
