import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

public class ExampleClient extends WebSocketClient {

	public ExampleClient( URI serverUri , Draft draft ) {
		super( serverUri, draft );
	}

	public ExampleClient( URI serverURI ) {
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

	public static void main( String[] args ) throws URISyntaxException {
		ExampleClient c = new ExampleClient( new URI( "ws://localhost:8887" ), new Draft_10() );
		c.connect();
	}

}