/*
 * Copyright (c) 2010-2020 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.java_websocket.drafts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.java_websocket.enums.Opcode;
import org.java_websocket.framing.Framedata;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Draft6455BufferTest {

  static Stream<Arguments> buffers() {
    List<Arguments> arguments = new ArrayList<>();
    for (boolean direct : new boolean[]{false, true}) {
      for (boolean readOnly : new boolean[]{false, true}) {
        for (boolean masked : new boolean[]{false, true}) {
          for (int length : new int[]{0, 4, 126, 65536}) {
            arguments.add(Arguments.of(direct, readOnly, masked, length));
          }
        }
      }
    }
    return arguments.stream();
  }

  private static byte[] payload(int length) {
    byte[] payload = new byte[length];
    for (int i = 0; i < length; i++) {
      payload[i] = (byte) i;
    }
    return payload;
  }

  private static byte[] wire(byte[] payload, boolean masked) {
    int lengthBytes = payload.length <= 125 ? 0 : payload.length <= 65535 ? 2 : 8;
    ByteBuffer frame = ByteBuffer.allocate(2 + lengthBytes + (masked ? 4 : 0) + payload.length);
    frame.put((byte) 0x82);
    int length = lengthBytes == 0 ? payload.length : lengthBytes == 2 ? 126 : 127;
    frame.put((byte) (length | (masked ? 0x80 : 0)));
    if (lengthBytes == 2) {
      frame.putShort((short) payload.length);
    } else if (lengthBytes == 8) {
      frame.putLong(payload.length);
    }
    byte[] key = {1, 2, 3, 4};
    if (masked) {
      frame.put(key);
    }
    for (int i = 0; i < payload.length; i++) {
      frame.put((byte) (payload[i] ^ (masked ? key[i % key.length] : 0)));
    }
    return frame.array();
  }

  private static ByteBuffer input(byte[] bytes, int offset, int length, boolean direct,
      boolean readOnly) {
    ByteBuffer parent = direct ? ByteBuffer.allocateDirect(length + 7)
        : ByteBuffer.allocate(length + 7);
    parent.position(4);
    parent.put(bytes, offset, length);
    parent.limit(parent.position());
    parent.position(3);
    ByteBuffer slice = parent.slice();
    slice.position(1);
    return readOnly ? slice.asReadOnlyBuffer() : slice;
  }

  private static void assertFrame(Framedata frame, byte[] expected) {
    assertEquals(Opcode.BINARY, frame.getOpcode());
    assertEquals(ByteBuffer.wrap(expected), frame.getPayloadData());
  }

  @ParameterizedTest
  @MethodSource("buffers")
  void decodeConsecutiveFrames(boolean direct, boolean readOnly, boolean masked, int length)
      throws Exception {
    byte[] expected = payload(length);
    byte[] first = wire(expected, masked);
    byte[] second = wire(new byte[]{42}, masked);
    ByteBuffer combined = ByteBuffer.allocate(first.length + second.length);
    combined.put(first).put(second);
    ByteBuffer input = input(combined.array(), 0, combined.capacity(), direct, readOnly);
    int limit = input.limit();

    List<Framedata> frames = new Draft_6455().translateFrame(input);

    assertEquals(2, frames.size());
    assertFrame(frames.get(0), expected);
    assertFrame(frames.get(1), new byte[]{42});
    assertFalse(input.hasRemaining());
    assertEquals(limit, input.limit());
  }

  @ParameterizedTest
  @MethodSource("buffers")
  void decodeSplitFrames(boolean direct, boolean readOnly, boolean masked, int length)
      throws Exception {
    byte[] expected = payload(length);
    byte[] wire = wire(expected, masked);
    Draft_6455 draft = new Draft_6455();
    List<Framedata> frames = new ArrayList<>();
    int offset = 0;
    for (int chunk : new int[]{1, 1, 1, 2, 5, 17, wire.length}) {
      int count = Math.min(chunk, wire.length - offset);
      ByteBuffer input = input(wire, offset, count, direct, readOnly);
      int limit = input.limit();
      frames.addAll(draft.translateFrame(input));
      assertFalse(input.hasRemaining());
      assertEquals(limit, input.limit());
      offset += count;
    }

    assertEquals(1, frames.size());
    assertFrame(frames.get(0), expected);
  }
}
