package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JUnit Test for the CloseFrame class
 */
public class CloseFrameTest {

    @Test
    public void testConstructor() {
        CloseFrame frame = new CloseFrame();
        assertEquals("Opcode must be equal", Framedata.Opcode.CLOSING, frame.getOpcode());
        assertEquals("Fin must be set", true, frame.isFin());
        assertEquals("TransferedMask must not be set", false, frame.getTransfereMasked());
        assertEquals("Payload must be 2 (close code)", 2, frame.getPayloadData().capacity());
        assertEquals("RSV1 must be false", false, frame.isRSV1());
        assertEquals("RSV2 must be false", false, frame.isRSV2());
        assertEquals("RSV3 must be false", false, frame.isRSV3());
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
    }

    @Test
    public void testExtends() {
        CloseFrame frame = new CloseFrame();
        assertEquals("Frame must extend dataframe", true, frame instanceof ControlFrame);
    }

    @Test
    public void testIsValid() {
        CloseFrame frame = new CloseFrame();
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
        frame.setRSV3(false);
        frame.setCode(CloseFrame.NORMAL);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.GOING_AWAY);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.PROTOCOL_ERROR);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.REFUSE);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.NOCODE);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //fine
        }
        frame.setCode(CloseFrame.ABNORMAL_CLOSE);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //fine
        }
        frame.setCode(CloseFrame.NO_UTF8);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.POLICY_VALIDATION);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.TOOBIG);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.EXTENSION);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.UNEXPECTED_CONDITION);
        try {
            frame.isValid();
        } catch (InvalidDataException e) {
            fail("InvalidDataException should not be thrown");
        }
        frame.setCode(CloseFrame.TLS_ERROR);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //fine
        }
        frame.setCode(CloseFrame.NEVER_CONNECTED);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //fine
        }
        frame.setCode(CloseFrame.BUGGYCLOSE);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //fine
        }
        frame.setCode(CloseFrame.FLASHPOLICY);
        try {
            frame.isValid();
            fail("InvalidDataException should be thrown");
        } catch (InvalidDataException e) {
            //fine
        }
    }
}
