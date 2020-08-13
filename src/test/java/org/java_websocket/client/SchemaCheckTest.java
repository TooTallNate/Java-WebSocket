package org.java_websocket.client;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchemaCheckTest {

  @Test
  public void testSchemaCheck() throws URISyntaxException {
    final String[] invalidCase = {
        "http://localhost:80",
        "http://localhost:81",
        "http://localhost",
        "https://localhost:443",
        "https://localhost:444",
        "https://localhost",
        "any://localhost",
        "any://localhost:82",
    };
    final Exception[] exs = new Exception[invalidCase.length];
    for (int i = 0; i < invalidCase.length; i++) {
      final int finalI = i;
      new WebSocketClient(new URI(invalidCase[finalI])) {
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
          exs[finalI] = ex;
        }
      }.run();
    }
    for (Exception exception : exs) {
      assertTrue(exception instanceof IllegalArgumentException);
    }
    final String[] validCase = {
        "ws://localhost",
        "ws://localhost:80",
        "ws://localhost:81",
        "wss://localhost",
        "wss://localhost:443",
        "wss://localhost:444"
    };
    for (String s : validCase) {
      new WebSocketClient(new URI(s)) {
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
          assertFalse(ex instanceof IllegalArgumentException);
        }
      }.run();
    }
  }
}
