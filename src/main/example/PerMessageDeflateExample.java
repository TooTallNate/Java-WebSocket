import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * This class only serves the purpose of showing how to enable PerMessageDeflateExtension for both
 * server and client sockets.<br> Extensions are required to be registered in
 *
 * @see Draft objects and both
 * @see WebSocketClient and
 * @see WebSocketServer accept a
 * @see Draft object in their constructors. This example shows how to achieve it for both server and
 * client sockets. Once the connection has been established, PerMessageDeflateExtension will be
 * enabled and any messages (binary or text) will be compressed/decompressed automatically.<br>
 * Since no additional code is required when sending or receiving messages, this example skips those
 * parts.
 */
public class PerMessageDeflateExample {

  private static final Draft perMessageDeflateDraft = new Draft_6455(
      new PerMessageDeflateExtension());
  private static final int PORT = 8887;

  private static class DeflateClient extends WebSocketClient {

    public DeflateClient() throws URISyntaxException {
      super(new URI("ws://localhost:" + PORT), perMessageDeflateDraft);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
    }

    @Override
    public void onError(Exception ex) {
    }
  }

  private static class DeflateServer extends WebSocketServer {

    public DeflateServer() {
      super(new InetSocketAddress(PORT), Collections.singletonList(perMessageDeflateDraft));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
    }

    @Override
    public void onStart() {
    }
  }
}
