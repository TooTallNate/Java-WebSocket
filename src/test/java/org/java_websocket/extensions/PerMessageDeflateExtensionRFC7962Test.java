/*
 * TooTallNate - Java-WebSocket
 *
 * MIT License
 *
 * Copyright (C) 2025 Robert Schlabbach <robert.schlabbach@ubitricity.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.java_websocket.extensions;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.framing.*;
import org.java_websocket.handshake.HandshakeImpl1Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RFC 7692 WebSocket Per-Message Deflate Extension Tests */
public class PerMessageDeflateExtensionRFC7962Test {
  // RFC 7692 Section 7.2.3.1 A Message Compressed Using One Compressed Deflate Block
  private static final String RFC_7962_TEST_MESSAGE_TEXT = "Hello";
  private static final byte[] RFC_7962_TEST_MESSAGE_COMPRESSED =
      new byte[] {
        (byte) 0xc1, 0x07, (byte) 0xf2, 0x48, (byte) 0xcd, (byte) 0xc9, (byte) 0xc9, 0x07, 0x00
      };
  private static final byte[] RFC_7962_TEST_MESSAGE_FRAGMENTS =
      new byte[] {
        // first frame:
        0x41,
        0x03,
        (byte) 0xf2,
        0x48,
        (byte) 0xcd,
        // second frame:
        (byte) 0x80,
        0x04,
        (byte) 0xc9,
        (byte) 0xc9,
        0x07,
        0x00
      };
  // RFC 7692 Section 7.2.3.2 Sharing LZ77 Sliding Window
  private static final byte[] RFC_7962_TEST_PAYLOAD_COMPRESSED =
      new byte[] {(byte) 0xf2, 0x48, (byte) 0xcd, (byte) 0xc9, (byte) 0xc9, 0x07, 0x00};
  private static final byte[] RFC_7962_TEST_PAYLOAD_COMPRESSED_AGAIN =
      new byte[] {(byte) 0xf2, 0x00, 0x11, 0x00, 0x00};
  // RFC 7692 Section 7.2.3.3 DEFLATE Block with No Compression
  private static final byte[] RFC_7962_TEST_MESSAGE_NO_COMPRESSION =
      new byte[] {
        (byte) 0xc1,
        0x0b,
        0x00,
        0x05,
        0x00,
        (byte) 0xfa,
        (byte) 0xff,
        0x48,
        0x65,
        0x6c,
        0x6c,
        0x6f,
        0x00
      };
  // RFC 7692 Section 7.2.3.4 DEFLATE Block with "BFINAL" Set to 1
  private static final byte[] RFC_7962_TEST_PAYLOAD_COMPRESSED_BFINAL =
      new byte[] {(byte) 0xf3, 0x48, (byte) 0xcd, (byte) 0xc9, (byte) 0xc9, 0x07, 0x00, 0x00};
  // RFC 7692 Section 7.2.3.5 Two DEFLATE Blocks in One Message
  private static final byte[] RFC_7962_TEST_PAYLOAD_TWO_DEFLATE_BLOCKS =
      new byte[] {
        (byte) 0xf2,
        0x48,
        0x05,
        0x00,
        0x00,
        0x00,
        (byte) 0xff,
        (byte) 0xff,
        (byte) 0xca,
        (byte) 0xc9,
        (byte) 0xc9,
        0x07,
        0x00
      };
  // RFC 7692 Section 7.2.3.6 Compressed Empty Fragment
  private static final byte[] RFC_7962_TEST_PAYLOAD_COMPRESSED_EMPTY_FRAGMENT = new byte[] {0x00};

  private PerMessageDeflateExtension extension;
  private Draft_6455 draft;

  @BeforeEach
  public void setUp() throws Exception {
    extension = new PerMessageDeflateExtension();
    extension.setThreshold(0);
    setupDraft();
  }

  private void setupDraft() throws InvalidHandshakeException {
    draft = new Draft_6455(extension);
    HandshakeImpl1Client handshake = new HandshakeImpl1Client();
    handshake.setResourceDescriptor("/");
    handshake.put("Host", "localhost");
    handshake.put("Connection", "Upgrade");
    handshake.put("Upgrade", "websocket");
    handshake.put("Sec-WebSocket-Version", "13");
    handshake.put("Sec-WebSocket-Extensions", extension.getProvidedExtensionAsClient());
    draft.acceptHandshakeAsServer(handshake);
  }

  @Test
  public void testRFC7962Section7231MessageCompression() {
    Framedata frame = buildMessageFrame(RFC_7962_TEST_MESSAGE_TEXT);
    byte[] frameBytes = draft.createBinaryFrame(frame).array();
    assertArrayEquals(RFC_7962_TEST_MESSAGE_COMPRESSED, frameBytes);
  }

  @Test
  public void testRFC7962Section7231FragmentsDecompression() throws InvalidDataException {
    List<Framedata> frames = draft.translateFrame(ByteBuffer.wrap(RFC_7962_TEST_MESSAGE_FRAGMENTS));
    assertEquals(2, frames.size());
    assertInstanceOf(DataFrame.class, frames.get(0));
    assertFalse(frames.get(0) instanceof ContinuousFrame);
    assertFalse(frames.get(0).isFin());
    assertFalse(frames.get(0).isRSV1());
    assertFalse(frames.get(0).isRSV2());
    assertFalse(frames.get(0).isRSV3());
    assertInstanceOf(ContinuousFrame.class, frames.get(1));
    assertTrue(frames.get(1).isFin());
    assertFalse(frames.get(1).isRSV1());
    assertFalse(frames.get(1).isRSV2());
    assertFalse(frames.get(1).isRSV3());
    assertEquals(RFC_7962_TEST_MESSAGE_TEXT, framesPayloadToString(frames));
  }

  @Test
  public void testRFC7962Section7232CompressionWithNoContextTakeover()
      throws InvalidHandshakeException {
    extension.setServerNoContextTakeover(true);
    setupDraft();
    Framedata frame1 = buildMessageFrame(RFC_7962_TEST_MESSAGE_TEXT);
    extension.encodeFrame(frame1);
    assertArrayEquals(RFC_7962_TEST_PAYLOAD_COMPRESSED, getPayload(frame1));
    Framedata frame2 = buildMessageFrame(RFC_7962_TEST_MESSAGE_TEXT);
    extension.encodeFrame(frame2);
    assertArrayEquals(RFC_7962_TEST_PAYLOAD_COMPRESSED, getPayload(frame2));
  }

  @Test
  public void testRFC7962Section7232CompressionWithContextTakeover() {
    Framedata frame1 = buildMessageFrame(RFC_7962_TEST_MESSAGE_TEXT);
    extension.encodeFrame(frame1);
    assertArrayEquals(RFC_7962_TEST_PAYLOAD_COMPRESSED, getPayload(frame1));
    Framedata frame2 = buildMessageFrame(RFC_7962_TEST_MESSAGE_TEXT);
    extension.encodeFrame(frame2);
    assertArrayEquals(RFC_7962_TEST_PAYLOAD_COMPRESSED_AGAIN, getPayload(frame2));
  }

  @Test
  public void testRFC7962Section7233DeflateBlockWithNoCompression() throws InvalidDataException {
    List<Framedata> frames =
        draft.translateFrame(ByteBuffer.wrap(RFC_7962_TEST_MESSAGE_NO_COMPRESSION));
    assertEquals(1, frames.size());
    assertInstanceOf(DataFrame.class, frames.get(0));
    assertFalse(frames.get(0) instanceof ContinuousFrame);
    assertTrue(frames.get(0).isFin());
    assertFalse(frames.get(0).isRSV1());
    assertFalse(frames.get(0).isRSV2());
    assertFalse(frames.get(0).isRSV3());
    assertEquals(RFC_7962_TEST_MESSAGE_TEXT, framesPayloadToString(frames));
  }

  @Test
  public void testRFC7962Section7234DeflateBlockWithBFINAL() throws InvalidDataException {
    Framedata frame = buildCompressedFrame(RFC_7962_TEST_PAYLOAD_COMPRESSED_BFINAL);
    extension.decodeFrame(frame);
    assertTrue(frame.isFin());
    assertFalse(frame.isRSV1());
    assertFalse(frame.isRSV2());
    assertFalse(frame.isRSV3());
    assertEquals(RFC_7962_TEST_MESSAGE_TEXT, framePayloadToString(frame));
  }

  @Test
  public void testRFC7962Section7235TwoDeflateBlocksInOneMessage() throws InvalidDataException {
    Framedata frame = buildCompressedFrame(RFC_7962_TEST_PAYLOAD_TWO_DEFLATE_BLOCKS);
    extension.decodeFrame(frame);
    assertTrue(frame.isFin());
    assertFalse(frame.isRSV1());
    assertFalse(frame.isRSV2());
    assertFalse(frame.isRSV3());
    assertEquals(RFC_7962_TEST_MESSAGE_TEXT, framePayloadToString(frame));
  }

  @Test
  public void testRFC7962Section7236GeneratingAnEmptyFragment() throws InvalidDataException {
    DataFrame frame1 = buildMessageFrame(RFC_7962_TEST_MESSAGE_TEXT);
    frame1.setFin(false);
    DataFrame frame2 = new ContinuousFrame();
    frame2.setFin(true);
    extension.encodeFrame(frame1);
    extension.encodeFrame(frame2);
    assertArrayEquals(RFC_7962_TEST_PAYLOAD_COMPRESSED_EMPTY_FRAGMENT, getPayload(frame2));
    extension.decodeFrame(frame1);
    extension.decodeFrame(frame2);
    List<Framedata> frames = new ArrayList<>(2);
    frames.add(frame1);
    frames.add(frame2);
    assertEquals(RFC_7962_TEST_MESSAGE_TEXT, framesPayloadToString(frames));
  }

  private DataFrame buildMessageFrame(String message) {
    TextFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(message.getBytes()));
    frame.setFin(true);
    return frame;
  }

  private DataFrame buildCompressedFrame(byte[] payload) {
    DataFrame frame = new TextFrame();
    frame.setPayload(ByteBuffer.wrap(payload));
    frame.setRSV1(true);
    frame.setFin(true);
    return frame;
  }

  private String framePayloadToString(Framedata frame) {
    return framesPayloadToString(Collections.singletonList(frame));
  }

  private String framesPayloadToString(List<Framedata> frames) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      for (Framedata frame : frames) {
        output.write(getPayload(frame));
      }
      return output.toString();
    } catch (IOException e) {
      return null;
    }
  }

  private byte[] getPayload(Framedata frame) {
    ByteBuffer buffer = frame.getPayloadData();
    byte[] payload = new byte[buffer.remaining()];
    System.arraycopy(
        buffer.array(), buffer.arrayOffset() + buffer.position(), payload, 0, buffer.remaining());
    return payload;
  }
}
