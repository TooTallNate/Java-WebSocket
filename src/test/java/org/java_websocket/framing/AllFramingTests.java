package org.java_websocket.framing;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.java_websocket.framing.BinaryFrameTest.class,
        org.java_websocket.framing.PingFrameTest.class,
        org.java_websocket.framing.PongFrameTest.class,
        org.java_websocket.framing.CloseFrameTest.class,
        org.java_websocket.framing.TextFrameTest.class,
        org.java_websocket.framing.ContinuousFrameTest.class,
        org.java_websocket.framing.FramedataImpl1Test.class
})
/**
 * Start all tests for frames
 */
public class AllFramingTests {
}
