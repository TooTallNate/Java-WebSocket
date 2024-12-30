package org.java_websocket.extensions;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Deflater;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.framing.ContinuousFrame;
import org.java_websocket.framing.TextFrame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PerMessageDeflateExtensionTest {

  @Test
  public void testDecodeFrame() throws InvalidDataException {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
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
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
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
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension(Deflater.NO_COMPRESSION);
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
    assertArrayEquals(message, Arrays.copyOfRange(payloadArray, 5,payloadArray.length-5));
    assertTrue(frame.isRSV1());
    deflateExtension.decodeFrame(frame);
    assertArrayEquals(message, frame.getPayloadData().array());
  }

  @Test
  public void testDecodeFrameBestSpeedCompression() throws InvalidDataException {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension(Deflater.BEST_SPEED);
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
    assertArrayEquals(Arrays.copyOfRange(buffer,0, bytesCompressed), Arrays.copyOfRange(payloadArray,0,payloadArray.length));
    assertTrue(frame.isRSV1());
    deflateExtension.decodeFrame(frame);
    assertArrayEquals(message, frame.getPayloadData().array());
  }

  @Test
  public void testDecodeFrameBestCompression() throws InvalidDataException {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension(Deflater.BEST_COMPRESSION);
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
    assertArrayEquals(Arrays.copyOfRange(buffer,0, bytesCompressed), Arrays.copyOfRange(payloadArray,0,payloadArray.length));
    assertTrue(frame.isRSV1());
    deflateExtension.decodeFrame(frame);
    assertArrayEquals(message, frame.getPayloadData().array());
  }


  @Test
  public void testEncodeFrame() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
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
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
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
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    assertTrue(deflateExtension.acceptProvidedExtensionAsServer("permessage-deflate"));
    assertTrue(deflateExtension
        .acceptProvidedExtensionAsServer("some-other-extension, permessage-deflate"));
    assertFalse(deflateExtension.acceptProvidedExtensionAsServer("wrong-permessage-deflate"));
  }

  @Test
  public void testAcceptProvidedExtensionAsClient() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    assertTrue(deflateExtension.acceptProvidedExtensionAsClient("permessage-deflate"));
    assertTrue(deflateExtension
        .acceptProvidedExtensionAsClient("some-other-extension, permessage-deflate"));
    assertFalse(deflateExtension.acceptProvidedExtensionAsClient("wrong-permessage-deflate"));
  }

  @Test
  public void testIsFrameValid() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
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
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    assertEquals("permessage-deflate; server_no_context_takeover; client_no_context_takeover",
        deflateExtension.getProvidedExtensionAsClient());
  }

  @Test
  public void testGetProvidedExtensionAsServer() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    assertEquals("permessage-deflate; server_no_context_takeover",
        deflateExtension.getProvidedExtensionAsServer());
  }

  @Test
  public void testToString() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    assertEquals("PerMessageDeflateExtension", deflateExtension.toString());
  }

  @Test
  public void testIsServerNoContextTakeover() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    assertTrue(deflateExtension.isServerNoContextTakeover());
  }

  @Test
  public void testSetServerNoContextTakeover() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    deflateExtension.setServerNoContextTakeover(false);
    assertFalse(deflateExtension.isServerNoContextTakeover());
  }

  @Test
  public void testIsClientNoContextTakeover() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    assertFalse(deflateExtension.isClientNoContextTakeover());
  }

  @Test
  public void testSetClientNoContextTakeover() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    deflateExtension.setClientNoContextTakeover(true);
    assertTrue(deflateExtension.isClientNoContextTakeover());
  }

  @Test
  public void testCopyInstance() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    PerMessageDeflateExtension newDeflateExtension = (PerMessageDeflateExtension)deflateExtension.copyInstance();
    assertEquals("PerMessageDeflateExtension", newDeflateExtension.toString());
    // Also check the values
    assertEquals(deflateExtension.getThreshold(), newDeflateExtension.getThreshold());
    assertEquals(deflateExtension.isClientNoContextTakeover(), newDeflateExtension.isClientNoContextTakeover());
    assertEquals(deflateExtension.isServerNoContextTakeover(), newDeflateExtension.isServerNoContextTakeover());
    assertEquals(deflateExtension.getCompressionLevel(), newDeflateExtension.getCompressionLevel());


    deflateExtension = new PerMessageDeflateExtension(Deflater.BEST_COMPRESSION);
    deflateExtension.setThreshold(512);
    deflateExtension.setServerNoContextTakeover(false);
    deflateExtension.setClientNoContextTakeover(true);
    newDeflateExtension = (PerMessageDeflateExtension)deflateExtension.copyInstance();

    assertEquals(deflateExtension.getThreshold(), newDeflateExtension.getThreshold());
    assertEquals(deflateExtension.isClientNoContextTakeover(), newDeflateExtension.isClientNoContextTakeover());
    assertEquals(deflateExtension.isServerNoContextTakeover(), newDeflateExtension.isServerNoContextTakeover());
    assertEquals(deflateExtension.getCompressionLevel(), newDeflateExtension.getCompressionLevel());


    deflateExtension = new PerMessageDeflateExtension(Deflater.NO_COMPRESSION);
    deflateExtension.setThreshold(64);
    deflateExtension.setServerNoContextTakeover(true);
    deflateExtension.setClientNoContextTakeover(false);
    newDeflateExtension = (PerMessageDeflateExtension)deflateExtension.copyInstance();

    assertEquals(deflateExtension.getThreshold(), newDeflateExtension.getThreshold());
    assertEquals(deflateExtension.isClientNoContextTakeover(), newDeflateExtension.isClientNoContextTakeover());
    assertEquals(deflateExtension.isServerNoContextTakeover(), newDeflateExtension.isServerNoContextTakeover());
    assertEquals(deflateExtension.getCompressionLevel(), newDeflateExtension.getCompressionLevel());
  }

  @Test
  public void testDefaults() {
    PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
    assertFalse(deflateExtension.isClientNoContextTakeover());
    assertTrue(deflateExtension.isServerNoContextTakeover());
    assertEquals(1024, deflateExtension.getThreshold());
    assertEquals(Deflater.DEFAULT_COMPRESSION, deflateExtension.getCompressionLevel());
  }
}
