package org.java_websocket;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.java_websocket.util.ByteBufferUtilsTest.class,
        org.java_websocket.framing.AllFramingTests.class
})
/**
 * Start all tests
 */
public class AllTests {
}
