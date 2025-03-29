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

package org.java_websocket.framing;

import java.nio.ByteBuffer;
import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.util.ByteBufferUtils;
import org.java_websocket.util.Charsetfunctions;
import org.java_websocket.util.ValidationUtils;

/**
 * Class to represent a close frame
 */
public class CloseFrame extends ControlFrame {


  /**
   * The close code used in this close frame
   */
  private int code;

  /**
   * The close message used in this close frame
   */
  private String reason;

  /**
   * Constructor for a close frame
   * <p>
   * Using opcode closing and fin = true
   */
  public CloseFrame() {
    super(Opcode.CLOSING);
    setReason("");
    setCode(CloseCodeConstants.NORMAL);
  }

  /**
   * Set the close code for this close frame
   *
   * @param code the close code
   */
  public void setCode(int code) {
    this.code = code;
    // CloseFrame.TLS_ERROR is not allowed to be transferred over the wire
    if (code == CloseCodeConstants.TLS_ERROR) {
      this.code = CloseCodeConstants.NOCODE;
      this.reason = "";
    }
    updatePayload();
  }

  /**
   * Set the close reason for this close frame
   *
   * @param reason the reason code
   */
  public void setReason(String reason) {
    if (reason == null) {
      reason = "";
    }
    this.reason = reason;
    updatePayload();
  }

  /**
   * Get the used close code
   *
   * @return the used close code
   */
  public int getCloseCode() {
    return code;
  }

  /**
   * Get the message that closeframe is containing
   *
   * @return the message in this frame
   */
  public String getMessage() {
    return reason;
  }

  @Override
  public String toString() {
    return super.toString() + "code: " + code;
  }

  @Override
  public void isValid() throws InvalidDataException {
    super.isValid();
    if (code == CloseCodeConstants.NO_UTF8 && reason.isEmpty()) {
      throw new InvalidDataException(CloseCodeConstants.NO_UTF8, "Received text is no valid utf8 string!");
    }
    if (code == CloseCodeConstants.NOCODE && 0 < reason.length()) {
      throw new InvalidDataException(CloseCodeConstants.PROTOCOL_ERROR,
          "A close frame must have a closecode if it has a reason");
    }
    //Intentional check for code != CloseFrame.TLS_ERROR just to make sure even if the code earlier changes
    if ((code > CloseCodeConstants.TLS_ERROR && code < 3000)) {
      throw new InvalidDataException(CloseCodeConstants.PROTOCOL_ERROR, "Trying to send an illegal close code!");
    }
    if (code == CloseCodeConstants.ABNORMAL_CLOSE || code == CloseCodeConstants.TLS_ERROR
        || code == CloseCodeConstants.NOCODE || code > 4999 || code < 1000 || code == 1004) {
      throw new InvalidFrameException("closecode must not be sent over the wire: " + code);
    }
  }

  @Override
  public void setPayload(ByteBuffer payload) {
    code = CloseCodeConstants.NOCODE;
    reason = "";
    payload.mark();

    if (payload.remaining() == 0) {
      code = CloseCodeConstants.NORMAL;
    } else if (payload.remaining() == 1) {
      code = CloseCodeConstants.PROTOCOL_ERROR;
    } else {
      if (payload.remaining() >= 2) {
        // Read close code
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.position(2);
        bb.putShort(payload.getShort());
        bb.position(0);
        code = bb.getInt();
      }

      payload.reset();
      try {
        int mark = payload.position();
        reason = ValidationUtils.decodeUtf8(
                payload,
                mark + 2,  // Skip 2-byte close code
                mark
        );
      } catch (InvalidDataException e) {
        code = CloseCodeConstants.NO_UTF8;
        reason = null;
      }
    }
  }



  /**
   * Update the payload to represent the close code and the reason
   */
  private void updatePayload() {
    byte[] by = Charsetfunctions.utf8Bytes(reason);
    ByteBuffer buf = ByteBuffer.allocate(4);
    buf.putInt(code);
    buf.position(2);
    ByteBuffer pay = ByteBuffer.allocate(2 + by.length);
    pay.put(buf);
    pay.put(by);
    pay.rewind();
    super.setPayload(pay);
  }

  @Override
  public ByteBuffer getPayloadData() {
    if (code == CloseCodeConstants.NOCODE) {
      return ByteBufferUtils.getEmptyByteBuffer();
    }
    return super.getPayloadData();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    CloseFrame that = (CloseFrame) o;

    if (code != that.code) {
      return false;
    }
    return reason != null ? reason.equals(that.reason) : that.reason == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + code;
    result = 31 * result + (reason != null ? reason.hashCode() : 0);
    return result;
  }
}
