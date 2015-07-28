package org.java_websocket;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import static org.junit.Assert.assertTrue;

public class AutobahnClientScenario {

    private class AutobahnServer extends WebSocketServer {

        public AutobahnServer(int port, Draft d) throws UnknownHostException {
            super(new InetSocketAddress(port), Collections.singletonList(d));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private class AutobahnClient extends WebSocketClient {

        public AutobahnClient(Draft draft, URI uri) {
            super(uri, draft);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void onMessage(String message) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void onError(Exception ex) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    private String protocol;
    private String host;
    private Integer port;
    private String query;
    private Draft draft;

    @Given("^the Autobahn Server is running using Draft_(\\d+) on port (\\d+)$")
    public void startAutobahnServer() throws UnknownHostException {
        new AutobahnServer(9003, new Draft_17()).start();
    }

    @Given("^protocol is ws://$")
    public void createProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Given("^the host is localhost:$")
    public void createHost(String host) {
        this.host = host;
    }

    @Given("^the port is (\\d*)$")
    public void createPort(Integer port) {
        this.port = port;
    }

    @Given("^the query string is case=(\\d+)&agent=tootallnate/websocket$")
    public void createQuery(String query) {
        this.query = query;
    }

    @Given("^the draft is Draft_17")
    public void createDraft(Draft_17 draft_17) {
        this.draft = draft_17;
    }

    @When("^the client connects to the server")
    public void connectToServer() {
        AutobahnClient autobahnClient = new AutobahnClient(this.draft, URI.create(this.protocol + this.host + this.port + this.query));
        Thread thread = new Thread(autobahnClient);
        thread.start();
    }

    @Then("^the server response should contain (\\w*)$")
    public void checkMethod(String method) {
        assertTrue(method.contains("GET"));
    }

    @Then("^the response should contain case=(\\d+)&agent=tootallnate/websocket$")
    public void checkQuery(String query) {
        assertTrue(query.contains(this.query));
    }

    @Then("^the response should contain HTTP/(\\d+).(\\d+)$")
    public void checkHttpVersion(String http_version) {
        assertTrue(http_version.contains("HTTP/1.1"));
    }

    @Then("^the response should contain Connection: Upgrade$")
    public void checkHandshake(String handshake) {
        assertTrue(handshake.contains("Connection: Upgrade"));
    }

    @Then("^the response should contain localhost:$")
    public void checkHost(String host) {
        assertTrue(host.contains(this.host));
    }

    @Then("^the response should contain Sec-WebSocket-Key:$")
    public void checkWebSocketKey(String websocketKey) {
        assertTrue(websocketKey.contains("Sec-WebSocket-Key:"));
    }

    @Then("^the response should contain Sec-WebSocket-Version:$")
    public void checkWebSocketVersion(String websocketVersion) {
        assertTrue(websocketVersion.contains("Sec-WebSocket-Version:"));
    }
    @Then("^the response should contain Upgrade: websocket$")
    public void checkUpgradedProtocol(String upgradedProtocol) {
        assertTrue(upgradedProtocol.contains("Upgrade: websocket"));
    }

}
