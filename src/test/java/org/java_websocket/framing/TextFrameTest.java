package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JUnit Test for the TextFrame class
 */
public class TextFrameTest {

    @Test
    public void testConstructor() {
        TextFrame frame = new TextFrame();
        assertEquals("Opcode must be equal", Framedata.Opcode.TEXT , frame.getOpcode());
        assertEquals("Fin must be set", true , frame.isFin());
        assertEquals("TransferedMask must not be set", false , frame.getTransfereMasked());
        assertEquals("Payload must be empty", 0 , frame.getPayloadData().capacity());
        assertEquals("RSV1 must be false", false , frame.isRSV1());
        assertEquals("RSV2 must be false", false , frame.isRSV2());
        assertEquals("RSV3 must be false", false , frame.isRSV3());
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
    }

    @Test
    public void testExtends() {
        TextFrame frame = new TextFrame();
        assertEquals("Frame must extend dataframe", true, frame instanceof DataFrame);
    }

    @Test
    public void testIsValid() {
        //Nothing specific to test
    }
}
