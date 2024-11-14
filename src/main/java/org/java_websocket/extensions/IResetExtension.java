package org.java_websocket.extensions;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;

/**
 * Interface which specifies all required methods to develop a websocket extension.
 *
 * @since 1.3.5
 */
public interface IResetExtension {

  /**
   * Cleaning up internal stats when the draft gets reset.
   *
   * @since 1.3.5
   */
  void reset();
}