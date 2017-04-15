package org.java_websocket;

import cucumber.annotation.After;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.Assert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AutobahnClientScenario {

	private class AutobahnServer extends WebSocketServer {

		public AutobahnServer(int port, Draft d) throws UnknownHostException {
			super(new InetSocketAddress(port), Collections.singletonList(d));
		}

		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			//throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			//throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void onMessage(WebSocket conn, String message) {
			//throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			//throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void onStart() {

		}

	}

	private class AutobahnClient extends WebSocketClient {

		private final CountDownLatch connectionOpenedLatch;
		private final Map<String, String> openHandShakeFields;
		private String message;

		public AutobahnClient(Draft draft, URI uri) {
			super(uri, draft);
			connectionOpenedLatch = new CountDownLatch(1);
			openHandShakeFields = new HashMap<String, String>();
		}

		@Override
		public void onOpen(ServerHandshake handshakedata) {
			Iterator<String> it = handshakedata.iterateHttpFields();
			while(it.hasNext()) {
				String key = it.next();
				System.out.printf("%s %s%n", key, handshakedata.getFieldValue(key)); // TODO Remove this
				openHandShakeFields.put(key, handshakedata.getFieldValue(key));
			}
			connectionOpenedLatch.countDown();
		}

		@Override
		public void onMessage(String message) {
			// TODO Test message receiving
		}

		@Override
		public void onClose(int code, String reason, boolean remote) {
			// TODO Check connection closing
		}

		@Override
		public void onError(Exception ex) {
			// TODO Check error handling
			ex.printStackTrace();
			connectionOpenedLatch.countDown();
		}

	}

	private static Draft getDraft(int number) {
		Exception exception;
		try {
			return (Draft) Class.forName("org.java_websocket.drafts.Draft_" + number).newInstance();
		} catch(InstantiationException e) {
			exception = e;
		} catch(IllegalAccessException e) {
			exception = e;
		} catch(ClassNotFoundException e) {
			exception = e;
		}
		throw new RuntimeException(exception);
	}

	private String protocol;
	private String host;
	private Integer port;
	private String query;
	private Draft draft;

	private AutobahnServer autobahnServer;

	@Given("^the Autobahn Server is running using Draft_(\\d+) on port (\\d+)$")
	public void startAutobahnServer(int draft, int port) throws UnknownHostException {
		autobahnServer = new AutobahnServer(port, getDraft(draft));
		autobahnServer.start();
	}

	@Given("^protocol is (.+)$")
	public void createProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Given("^the host is (.+)$")
	public void createHost(String host) {
		this.host = host;
	}

	@Given("^the port is (\\d+)$")
	public void createPort(int port) {
		this.port = port;
	}

	@Given("^the query string is (.+)$")
	public void createQuery(String query) {
		this.query = query;
	}

	@Given("^the draft is Draft_(\\d+)")
	public void createDraft(int draft) {
		this.draft = getDraft(draft);
	}

	private AutobahnClient autobahnClient;

	@When("^the client connects to the server$")
	public void connectToServer() {
		URI uri;
		try {
			uri = new URI(this.protocol, null, this.host, this.port, null, this.query, null);
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}

		System.out.println(uri);
		autobahnClient = new AutobahnClient(this.draft, uri);
		try {
			autobahnClient.connectBlocking();
			autobahnClient.connectionOpenedLatch.await();
		} catch(InterruptedException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Then("^the server response should contain (.+)$")
	public void checkMethod(String method) {
		// TODO Implement check
		//assertTrue(method.contains("GET"));
	}

	@Then("^the response's query should contain (.+)$")
	public void checkQuery(String query) {
		// TODO Implement check
		//assertTrue(query.contains(this.query));
	}

	@Then("^the response's http version should contain (.+)$")
	public void checkHttpVersion(String httpversion) {
		// TODO Implement check
		//assertTrue(.contains("HTTP/" + major + "." + minor));
	}

	@Then("^the response's handshake should contain (.+)$")
	public void checkHandshake(String handshake) {
		Assert.assertEquals(handshake, autobahnClient.openHandShakeFields.get("Connection"));
	}

	@Then("^the response's host should contain (.+)$")
	public void checkHost(String host) {
		// TODO Implement check
		//assertTrue(host.contains(this.host));
	}

	@Then("^the response's websocket key should contain (.+)$")
	public void checkWebSocketKey(String websocketKey) {
		// TODO Implement check
		//Assert.assertTrue(autobahnClient.openHandShakeFields.containsKey(websocketKey));
		//assertTrue(websocketKey.contains("Sec-WebSocket-Key:"));
	}

	@Then("^the response's websocket version should contain (.+)$")
	public void checkWebSocketVersion(String websocketVersion) {
		// TODO Implement check
		//assertTrue(websocketVersion.contains("Sec-WebSocket-Version:"));
	}

	@Then("^the response's upgraded protocol should contain (.+)$")
	public void checkUpgradedProtocol(String upgradedProtocol) {
		Assert.assertEquals(upgradedProtocol, autobahnClient.openHandShakeFields.get("Upgrade"));
	}

	@After
	public void cleanup() {
		try {
			autobahnClient.closeBlocking();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}

		try {
			autobahnServer.stop();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

}
