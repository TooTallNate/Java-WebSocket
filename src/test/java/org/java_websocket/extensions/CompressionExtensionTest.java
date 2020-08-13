package org.java_websocket.extensions;

import static org.junit.Assert.fail;

import org.java_websocket.framing.PingFrame;
import org.java_websocket.framing.TextFrame;
import org.junit.Test;

public class CompressionExtensionTest {


  @Test
  public void testIsFrameValid() {
    CustomCompressionExtension customCompressionExtension = new CustomCompressionExtension();
    TextFrame textFrame = new TextFrame();
    try {
      customCompressionExtension.isFrameValid(textFrame);
    } catch (Exception e) {
      fail("This frame is valid");
    }
    textFrame.setRSV1(true);
    try {
      customCompressionExtension.isFrameValid(textFrame);
    } catch (Exception e) {
      fail("This frame is valid");
    }
    textFrame.setRSV1(false);
    textFrame.setRSV2(true);
    try {
      customCompressionExtension.isFrameValid(textFrame);
      fail("This frame is not valid");
    } catch (Exception e) {
      //
    }
    textFrame.setRSV2(false);
    textFrame.setRSV3(true);
    try {
      customCompressionExtension.isFrameValid(textFrame);
      fail("This frame is not valid");
    } catch (Exception e) {
      //
    }
    PingFrame pingFrame = new PingFrame();
    try {
      customCompressionExtension.isFrameValid(pingFrame);
    } catch (Exception e) {
      fail("This frame is valid");
    }
    pingFrame.setRSV1(true);
    try {
      customCompressionExtension.isFrameValid(pingFrame);
      fail("This frame is not valid");
    } catch (Exception e) {
      //
    }
    pingFrame.setRSV1(false);
    pingFrame.setRSV2(true);
    try {
      customCompressionExtension.isFrameValid(pingFrame);
      fail("This frame is not valid");
    } catch (Exception e) {
      //
    }
    pingFrame.setRSV2(false);
    pingFrame.setRSV3(true);
    try {
      customCompressionExtension.isFrameValid(pingFrame);
      fail("This frame is not valid");
    } catch (Exception e) {
      //
    }
  }

  private static class CustomCompressionExtension extends CompressionExtension {

  }
}
