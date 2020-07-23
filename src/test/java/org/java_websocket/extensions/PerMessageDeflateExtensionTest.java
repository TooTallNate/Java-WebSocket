package org.java_websocket.extensions;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.framing.ContinuousFrame;
import org.java_websocket.framing.TextFrame;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PerMessageDeflateExtensionTest {

    @Test
    public void testDecodeFrame() throws InvalidDataException {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        String str = "This is a highly compressable text"
                + "This is a highly compressable text"
                + "This is a highly compressable text"
                + "This is a highly compressable text"
                + "This is a highly compressable text";
        byte[] message = str.getBytes();
        TextFrame frame = new TextFrame();
        frame.setPayload(ByteBuffer.wrap(message));
        deflateExtension.encodeFrame(frame);
        deflateExtension.decodeFrame(frame);
        assertArrayEquals(message, frame.getPayloadData().array());
    }

    @Test
    public void testEncodeFrame() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        String str = "This is a highly compressable text"
                + "This is a highly compressable text"
                + "This is a highly compressable text"
                + "This is a highly compressable text"
                + "This is a highly compressable text";
        byte[] message = str.getBytes();
        TextFrame frame = new TextFrame();
        frame.setPayload(ByteBuffer.wrap(message));
        deflateExtension.encodeFrame(frame);
        assertTrue(message.length > frame.getPayloadData().array().length);
    }

    @Test
    public void testAcceptProvidedExtensionAsServer() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertTrue(deflateExtension.acceptProvidedExtensionAsServer("permessage-deflate"));
        assertTrue(deflateExtension.acceptProvidedExtensionAsServer("some-other-extension, permessage-deflate"));
        assertFalse(deflateExtension.acceptProvidedExtensionAsServer("wrong-permessage-deflate"));
    }

    @Test
    public void testAcceptProvidedExtensionAsClient() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertTrue(deflateExtension.acceptProvidedExtensionAsClient("permessage-deflate"));
        assertTrue(deflateExtension.acceptProvidedExtensionAsClient("some-other-extension, permessage-deflate"));
        assertFalse(deflateExtension.acceptProvidedExtensionAsClient("wrong-permessage-deflate"));
    }

    @Test
    public void testIsFrameValid() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        TextFrame frame = new TextFrame();
        try {
            deflateExtension.isFrameValid(frame);
            fail("Frame not valid. RSV1 must be set.");
        } catch (Exception e) {
            //
        }
        frame.setRSV1(true);
        try {
            deflateExtension.isFrameValid(frame);
        } catch (Exception e) {
            fail("Frame is valid.");
        }
        frame.setRSV2(true);
        try {
            deflateExtension.isFrameValid(frame);
            fail("Only RSV1 bit must be set.");
        } catch (Exception e) {
            //
        }
        ContinuousFrame contFrame = new ContinuousFrame();
        contFrame.setRSV1(true);
        try {
            deflateExtension.isFrameValid(contFrame);
            fail("RSV1 must only be set for first fragments.Continuous frames can't have RSV1 bit set.");
        } catch (Exception e) {
            //
        }
        contFrame.setRSV1(false);
        try {
            deflateExtension.isFrameValid(contFrame);
        } catch (Exception e) {
            fail("Continuous frame is valid.");
        }
    }

    @Test
    public void testGetProvidedExtensionAsClient() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertEquals( "permessage-deflate; server_no_context_takeover; client_no_context_takeover",
                deflateExtension.getProvidedExtensionAsClient() );
    }

    @Test
    public void testGetProvidedExtensionAsServer() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertEquals( "permessage-deflate; server_no_context_takeover",
                deflateExtension.getProvidedExtensionAsServer() );
    }

    @Test
    public void testToString() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertEquals( "PerMessageDeflateExtension", deflateExtension.toString() );
    }

    @Test
    public void testIsServerNoContextTakeover() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertTrue(deflateExtension.isServerNoContextTakeover());
    }

    @Test
    public void testSetServerNoContextTakeover() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        deflateExtension.setServerNoContextTakeover(false);
        assertFalse(deflateExtension.isServerNoContextTakeover());
    }

    @Test
    public void testIsClientNoContextTakeover() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertFalse(deflateExtension.isClientNoContextTakeover());
    }

    @Test
    public void testSetClientNoContextTakeover() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        deflateExtension.setClientNoContextTakeover(true);
        assertTrue(deflateExtension.isClientNoContextTakeover());
    }

    @Test
    public void testCopyInstance() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        IExtension newDeflateExtension = deflateExtension.copyInstance();
        assertEquals(deflateExtension.toString(), newDeflateExtension.toString());
    }

    @Test
    public void testGetInflater() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertEquals(deflateExtension.getInflater(), new Inflater(true));
    }

    @Test
    public void testSetInflater() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        deflateExtension.setInflater(new Inflater(false));
        assertEquals(deflateExtension.getInflater(), new Inflater(false));
    }

    @Test
    public void testGetDeflater() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        assertEquals(deflateExtension.getDeflater(), new Deflater(Deflater.DEFAULT_COMPRESSION, true));
    }

    @Test
    public void testSetDeflater() {
        PerMessageDeflateExtension deflateExtension = new PerMessageDeflateExtension();
        deflateExtension.setDeflater(new Deflater(Deflater.DEFAULT_COMPRESSION, false));
        assertEquals(deflateExtension.getDeflater(), new Deflater(Deflater.DEFAULT_COMPRESSION, false));
    }
}
