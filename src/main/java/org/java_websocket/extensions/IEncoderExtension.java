package org.java_websocket.extensions;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;

/**
 * Interface which specifies all required methods to develop a websocket extension.
 *
 * @since 1.3.5
 */
public interface IEncoderExtension {

  /**
   * Decode a frame with a extension specific algorithm. The algorithm is subject to be implemented
   * by the specific extension. The resulting frame will be used in the application
   *
   * @param inputFrame the frame, which has do be decoded to be used in the application
   * @throws InvalidDataException Throw InvalidDataException if the received frame is not correctly
   *                              implemented by the other endpoint or there are other protocol
   *                              errors/decoding errors
   * @since 1.3.5
   */
  void decodeFrame(Framedata inputFrame) throws InvalidDataException;

  /**
   * Encode a frame with a extension specific algorithm. The algorithm is subject to be implemented
   * by the specific extension. The resulting frame will be send to the other endpoint.
   *
   * @param inputFrame the frame, which has do be encoded to be used on the other endpoint
   * @since 1.3.5
   */
  void encodeFrame(Framedata inputFrame);

}