package org.java_websocket.issues;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.SocketUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for #1460: a fatal Error (VirtualMachineError / ThreadDeath / LinkageError)
 * rethrown by WebSocketImpl#decodeFrames must reach onError and close the client connection
 * instead of silently killing the connect/read thread with no callback and no cleanup.
 */
public class Issue1460Test {

  /**
   * Draft that emulates a fatal Error during frame decoding, i.e. the trio
   * WebSocketImpl#decodeFrames rethrows.
   */
  private static class FatalErrorDraft extends Draft_6455 {
    @Override
    public List<Framedata> translateFrame(ByteBuffer buffer) {
      throw new LinkageError("simulated fatal error during frame processing");
    }

    @Override
    public Draft copyInstance() {
      ArrayList<IExtension> newExtensions = new ArrayList<>();
      for (IExtension knownExtension : getKnownExtensions()) {
        newExtensions.add(knownExtension.copyInstance());
      }
      ArrayList<org.java_websocket.protocols.IProtocol> newProtocols = new ArrayList<>();
      for (org.java_websocket.protocols.IProtocol knownProtocol : getKnownProtocols()) {
        newProtocols.add(knownProtocol.copyInstance());
      }
      return new FatalErrorDraft(newExtensions, newProtocols, getMaxFrameSize());
    }

    FatalErrorDraft(List<IExtension> inputExtensions,
        List<org.java_websocket.protocols.IProtocol> inputProtocols, int inputMaxFrameSize) {
      super(inputExtensions, inputProtocols, inputMaxFrameSize);
    }

    FatalErrorDraft() {
      super();
    }
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  public void testFatalErrorTriggersOnErrorAndClose() throws InterruptedException {
    int port = SocketUtil.getAvailablePort();
    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch errorLatch = new CountDownLatch(1);
    final CountDownLatch closeLatch = new CountDownLatch(1);
    final AtomicReference<Throwable> capturedError = new AtomicReference<>();
    final AtomicInteger capturedCode = new AtomicInteger(-1);

    WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {
      @Override
      public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("trigger");
      }

      @Override
      public void onClose(WebSocket conn, int code, String reason, boolean remote) {
      }

      @Override
      public void onMessage(WebSocket conn, String message) {
      }

      @Override
      public void onMessage(WebSocket conn, ByteBuffer message) {
      }

      @Override
      public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
      }

      @Override
      public void onStart() {
        startLatch.countDown();
      }
    };

    WebSocketClient client = new WebSocketClient(
        URI.create("ws://localhost:" + port), new FatalErrorDraft()) {
      @Override
      public void onOpen(ServerHandshake handshakedata) {
      }

      @Override
      public void onMessage(String message) {
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
        capturedCode.set(code);
        closeLatch.countDown();
      }

      @Override
      public void onError(Exception ex) {
        capturedError.set(ex);
        errorLatch.countDown();
      }
    };

    server.start();
    assertTrue(startLatch.await(5, TimeUnit.SECONDS), "server should start");
    client.connectBlocking();

    try {
      assertTrue(errorLatch.await(5, TimeUnit.SECONDS),
          "onError must be invoked for a fatal Error");
      assertTrue(closeLatch.await(5, TimeUnit.SECONDS),
          "onClose must be invoked for a fatal Error");
      assertTrue(capturedError.get().getCause() instanceof LinkageError,
          "onError must wrap the fatal Error");
      assertEquals(CloseFrame.UNEXPECTED_CONDITION, capturedCode.get());
    } finally {
      server.stop();
    }
  }
}
