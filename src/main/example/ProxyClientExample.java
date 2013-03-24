import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;

public class ProxyClientExample extends ExampleClient {

	public ProxyClientExample( URI serverURI , InetSocketAddress proxy ) {
		super( serverURI );
		setProxy( proxy );
	}

	@Override
	public ByteChannel createProxyChannel( ByteChannel towrap ) {
		/*
		 * You can create custom proxy handshake here. 
		 * For more infos see: WebSocketClient.DefaultClientProxyChannel and http://tools.ietf.org/html/rfc6455#section-4.1
		 */
		return super.createProxyChannel( towrap );
	}

	public static void main( String[] args ) throws URISyntaxException {
		ProxyClientExample c = new ProxyClientExample( new URI( "ws://echo.websocket.org" ), new InetSocketAddress( "proxyaddress", 80 ) );// don't forget to change "proxyaddress"
		c.connect();
	}
}
