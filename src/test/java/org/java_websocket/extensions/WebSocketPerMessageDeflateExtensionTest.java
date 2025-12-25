package org.java_websocket.extensions;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Deflater;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.extensions.permessage_deflate.WebSocketPerMessageDeflateExtension;
import org.java_websocket.framing.ContinuousFrame;
import org.java_websocket.framing.TextFrame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketPerMessageDeflateExtensionTest {

  @Test
  public void testDecodeFrame() throws InvalidDataException {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    deflateExtension.setThreshold(0);
    String str = "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text";
    byte[] message = str.getBytes();
    TextFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message));
    deflateExtension.encodeFrame(frame);
    assertTrue(frame.isRSV1());
    deflateExtension.decodeFrame(frame);
    assertArrayEquals(message, frame.getPayloadData().array());
  }
  @Test
  public void testDecodeFrameIfRSVIsNotSet() throws InvalidDataException {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    String str = "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text";
    byte[] message = str.getBytes();
    TextFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message));
    deflateExtension.decodeFrame(frame);
    assertArrayEquals(message, frame.getPayloadData().array());
    assertFalse(frame.isRSV1());
  }

  @Test
  public void testDecodeFrameNoCompression() throws InvalidDataException {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension(Deflater.NO_COMPRESSION);
    deflateExtension.setThreshold(0);
    String str = "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text";
    byte[] message = str.getBytes();
    TextFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message));
    deflateExtension.encodeFrame(frame);
    byte[] payloadArray = frame.getPayloadData().array();
    assertArrayEquals(message, Arrays.copyOfRange(payloadArray, 5, payloadArray.length - 1));
    assertTrue(frame.isRSV1());
    deflateExtension.decodeFrame(frame);
    assertArrayEquals(message, frame.getPayloadData().array());
  }

  @Test
  public void testDecodeFrameBestSpeedCompression() throws InvalidDataException {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension(Deflater.BEST_SPEED);
    deflateExtension.setThreshold(0);
    String str = "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text";
    byte[] message = str.getBytes();
    TextFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message));

    Deflater localDeflater = new Deflater(Deflater.BEST_SPEED,true);
    localDeflater.setInput(ByteBuffer.wrap(message).array());
    byte[] buffer = new byte[1024];
    int bytesCompressed = localDeflater.deflate(buffer, 0, buffer.length, Deflater.SYNC_FLUSH);

    deflateExtension.encodeFrame(frame);
    byte[] payloadArray = frame.getPayloadData().array();
    assertArrayEquals(Arrays.copyOfRange(buffer, 0, bytesCompressed - 4), payloadArray);
    assertTrue(frame.isRSV1());
    deflateExtension.decodeFrame(frame);
    assertArrayEquals(message, frame.getPayloadData().array());
  }

  @Test
  public void testDecodeFrameBestCompression() throws InvalidDataException {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension(Deflater.BEST_COMPRESSION);
    deflateExtension.setThreshold(0);
    String str = "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text"
            + "This is a highly compressable text";
    byte[] message = str.getBytes();
    TextFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message));

    Deflater localDeflater = new Deflater(Deflater.BEST_COMPRESSION,true);
    localDeflater.setInput(ByteBuffer.wrap(message).array());
    byte[] buffer = new byte[1024];
    int bytesCompressed = localDeflater.deflate(buffer, 0, buffer.length, Deflater.SYNC_FLUSH);

    deflateExtension.encodeFrame(frame);
    byte[] payloadArray = frame.getPayloadData().array();
    assertArrayEquals(Arrays.copyOfRange(buffer, 0, bytesCompressed - 4), payloadArray);
    assertTrue(frame.isRSV1());
    deflateExtension.decodeFrame(frame);
    assertArrayEquals(message, frame.getPayloadData().array());
  }


  @Test
  public void testEncodeFrame() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    deflateExtension.setThreshold(0);
    String str = "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text"
        + "This is a highly compressable text";
    byte[] message = str.getBytes();
    TextFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message));
    deflateExtension.encodeFrame(frame);
    assertTrue(message.length > frame.getPayloadData().array().length);
  }
  @Test
  public void testEncodeFrameBelowThreshold() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    deflateExtension.setThreshold(11);
    String str = "Hello World";
    byte[] message = str.getBytes();
    TextFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message));
    deflateExtension.encodeFrame(frame);
    // Message length is equal to the threshold --> encode
    assertTrue(frame.isRSV1());
    str = "Hello Worl";
    message = str.getBytes();
    frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message));
    deflateExtension.encodeFrame(frame);
    // Message length is below to the threshold --> do NOT encode
    assertFalse(frame.isRSV1());
  }

  @Test
  public void testAcceptProvidedExtensionAsServer() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    assertTrue(deflateExtension.acceptProvidedExtensionAsServer("permessage-deflate"));
    assertTrue(deflateExtension
        .acceptProvidedExtensionAsServer("some-other-extension, permessage-deflate"));
    assertFalse(deflateExtension.acceptProvidedExtensionAsServer("wrong-permessage-deflate"));
  }

  @Test
  public void testAcceptProvidedExtensionAsClient() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    assertTrue(deflateExtension.acceptProvidedExtensionAsClient("permessage-deflate"));
    assertTrue(deflateExtension
        .acceptProvidedExtensionAsClient("some-other-extension, permessage-deflate"));
    assertFalse(deflateExtension.acceptProvidedExtensionAsClient("wrong-permessage-deflate"));
  }

  @Test
  public void testIsFrameValid() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    TextFrame frame = new TextFrame();
    try {
      deflateExtension.isFrameValid(frame);
    } catch (Exception e) {
      fail("RSV1 is optional and should therefore not fail");
    }
    frame.setRSV1(true);
    try {
      deflateExtension.isFrameValid(frame);
    } catch (Exception e) {
      fail("Frame is valid.");
    }
    frame.setRSV2(true);
    try {
      deflateExtension.isFrameValid(frame);
      fail("Only RSV1 bit must be set.");
    } catch (Exception e) {
      //
    }
    ContinuousFrame contFrame = new ContinuousFrame();
    contFrame.setRSV1(true);
    try {
      deflateExtension.isFrameValid(contFrame);
      fail("RSV1 must only be set for first fragments.Continuous frames can't have RSV1 bit set.");
    } catch (Exception e) {
      //
    }
    contFrame.setRSV1(false);
    try {
      deflateExtension.isFrameValid(contFrame);
    } catch (Exception e) {
      fail("Continuous frame is valid.");
    }
  }

  @Test
  public void testGetProvidedExtensionAsClient() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    assertEquals("permessage-deflate", deflateExtension.getProvidedExtensionAsClient());
    deflateExtension.setClientNoContextTakeover(true);
    assertEquals("permessage-deflate; client_no_context_takeover",
        deflateExtension.getProvidedExtensionAsClient());
    deflateExtension.setServerNoContextTakeover(true);
    assertEquals("permessage-deflate; client_no_context_takeover; server_no_context_takeover",
        deflateExtension.getProvidedExtensionAsClient());
    deflateExtension.setClientNoContextTakeover(false);
    assertEquals("permessage-deflate; server_no_context_takeover",
        deflateExtension.getProvidedExtensionAsClient());
  }

  @Test
  public void testGetProvidedExtensionAsServer() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    assertEquals("permessage-deflate", deflateExtension.getProvidedExtensionAsServer());
    deflateExtension.setClientNoContextTakeover(true);
    assertEquals("permessage-deflate; client_no_context_takeover",
        deflateExtension.getProvidedExtensionAsServer());
    deflateExtension.setServerNoContextTakeover(true);
    assertEquals("permessage-deflate; client_no_context_takeover; server_no_context_takeover",
        deflateExtension.getProvidedExtensionAsServer());
    deflateExtension.setClientNoContextTakeover(false);
    assertEquals("permessage-deflate; server_no_context_takeover",
        deflateExtension.getProvidedExtensionAsServer());
  }

  @Test
  public void testToString() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    assertEquals("WebSocket Per-Message Deflate", deflateExtension.toString());
  }

  @Test
  public void testIsServerNoContextTakeover() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    assertFalse(deflateExtension.isServerNoContextTakeover());
  }

  @Test
  public void testSetServerNoContextTakeover() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    deflateExtension.setServerNoContextTakeover(true);
    assertTrue(deflateExtension.isServerNoContextTakeover());
  }

  @Test
  public void testIsClientNoContextTakeover() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    assertFalse(deflateExtension.isClientNoContextTakeover());
  }

  @Test
  public void testSetClientNoContextTakeover() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    deflateExtension.setClientNoContextTakeover(true);
    assertTrue(deflateExtension.isClientNoContextTakeover());
  }

  @Test
  public void testCopyInstance() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    WebSocketPerMessageDeflateExtension newDeflateExtension = (WebSocketPerMessageDeflateExtension)deflateExtension.copyInstance();
    assertEquals("WebSocket Per-Message Deflate", newDeflateExtension.toString());
    // Also check the values
    assertEquals(deflateExtension.getThreshold(), newDeflateExtension.getThreshold());
    assertEquals(deflateExtension.isClientNoContextTakeover(), newDeflateExtension.isClientNoContextTakeover());
    assertEquals(deflateExtension.isServerNoContextTakeover(), newDeflateExtension.isServerNoContextTakeover());
    assertEquals(deflateExtension.getCompressionLevel(), newDeflateExtension.getCompressionLevel());


    deflateExtension = new WebSocketPerMessageDeflateExtension(Deflater.BEST_COMPRESSION);
    deflateExtension.setThreshold(512);
    deflateExtension.setServerNoContextTakeover(false);
    deflateExtension.setClientNoContextTakeover(true);
    newDeflateExtension = (WebSocketPerMessageDeflateExtension)deflateExtension.copyInstance();

    assertEquals(deflateExtension.getThreshold(), newDeflateExtension.getThreshold());
    assertEquals(deflateExtension.isClientNoContextTakeover(), newDeflateExtension.isClientNoContextTakeover());
    assertEquals(deflateExtension.isServerNoContextTakeover(), newDeflateExtension.isServerNoContextTakeover());
    assertEquals(deflateExtension.getCompressionLevel(), newDeflateExtension.getCompressionLevel());


    deflateExtension = new WebSocketPerMessageDeflateExtension(Deflater.NO_COMPRESSION);
    deflateExtension.setThreshold(64);
    deflateExtension.setServerNoContextTakeover(true);
    deflateExtension.setClientNoContextTakeover(false);
    newDeflateExtension = (WebSocketPerMessageDeflateExtension)deflateExtension.copyInstance();

    assertEquals(deflateExtension.getThreshold(), newDeflateExtension.getThreshold());
    assertEquals(deflateExtension.isClientNoContextTakeover(), newDeflateExtension.isClientNoContextTakeover());
    assertEquals(deflateExtension.isServerNoContextTakeover(), newDeflateExtension.isServerNoContextTakeover());
    assertEquals(deflateExtension.getCompressionLevel(), newDeflateExtension.getCompressionLevel());
  }

  @Test
  public void testDefaults() {
    WebSocketPerMessageDeflateExtension deflateExtension = new WebSocketPerMessageDeflateExtension();
    assertFalse(deflateExtension.isClientNoContextTakeover());
    assertFalse(deflateExtension.isServerNoContextTakeover());
    assertEquals(64, deflateExtension.getThreshold());
    assertEquals(Deflater.DEFAULT_COMPRESSION, deflateExtension.getCompressionLevel());
  }
}
