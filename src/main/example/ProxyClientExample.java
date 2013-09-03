import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

public class ProxyClientExample {
	public static void main( String[] args ) throws URISyntaxException {
		ExampleClient c = new ExampleClient( new URI( "ws://echo.websocket.org" ) );
		c.setProxy( new Proxy( Proxy.Type.HTTP, new InetSocketAddress( "proxyaddress", 80 ) ) );
		c.connect();
	}
}
