package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JUnit Test for the PingFrame class
 */
public class PingFrameTest {

    @Test
    public void testConstructor() {
        PingFrame frame = new PingFrame();
        assertEquals("Opcode must be equal", Framedata.Opcode.PING , frame.getOpcode());
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
        PingFrame frame = new PingFrame();
        assertEquals("Frame must extend dataframe", true, frame instanceof ControlFrame);
    }

    @Test
    public void testIsValid() {
        PingFrame frame = new PingFrame();
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setFin(false);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
           //Fine
        }
        frame.setFin(true);
        frame.setRSV1(true);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //Fine
        }
        frame.setRSV1(false);
        frame.setRSV2(true);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //Fine
        }
        frame.setRSV2(false);
        frame.setRSV3(true);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //Fine
        }
    }
}
