/*
 * Copyright (c) 2010-2017 Nathan Rajlich
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

package org.java_websocket.extensions;

import org.java_websocket.framing.BinaryFrame;
import org.java_websocket.framing.TextFrame;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DefaultExtensionTest {
	@Test
	public void testDecodeFrame() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		BinaryFrame binaryFrame = new BinaryFrame();
		binaryFrame.setPayload( ByteBuffer.wrap( "test".getBytes() ) );
		defaultExtension.decodeFrame( binaryFrame );
		assertEquals( ByteBuffer.wrap( "test".getBytes() ), binaryFrame.getPayloadData() );
	}

	@Test
	public void testEncodeFrame() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		BinaryFrame binaryFrame = new BinaryFrame();
		binaryFrame.setPayload( ByteBuffer.wrap( "test".getBytes() ) );
		defaultExtension.encodeFrame( binaryFrame );
		assertEquals( ByteBuffer.wrap( "test".getBytes() ), binaryFrame.getPayloadData() );
	}

	@Test
	public void testAcceptProvidedExtensionAsServer() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		assertTrue( defaultExtension.acceptProvidedExtensionAsServer( "Test" ) );
		assertTrue( defaultExtension.acceptProvidedExtensionAsServer( "" ) );
		assertTrue( defaultExtension.acceptProvidedExtensionAsServer( "Test, ASDC, as, ad" ) );
		assertTrue( defaultExtension.acceptProvidedExtensionAsServer( "ASDC, as,ad" ) );
		assertTrue( defaultExtension.acceptProvidedExtensionAsServer( "permessage-deflate" ) );
	}

	@Test
	public void testAcceptProvidedExtensionAsClient() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		assertTrue( defaultExtension.acceptProvidedExtensionAsClient( "Test" ) );
		assertTrue( defaultExtension.acceptProvidedExtensionAsClient( "" ) );
		assertTrue( defaultExtension.acceptProvidedExtensionAsClient( "Test, ASDC, as, ad" ) );
		assertTrue( defaultExtension.acceptProvidedExtensionAsClient( "ASDC, as,ad" ) );
		assertTrue( defaultExtension.acceptProvidedExtensionAsClient( "permessage-deflate" ) );
	}

	@Test
	public void testIsFrameValid() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		TextFrame textFrame = new TextFrame();
		try {
			defaultExtension.isFrameValid( textFrame );
		} catch ( Exception e ) {
			fail( "This frame is valid" );
		}
		textFrame.setRSV1( true );
		try {
			defaultExtension.isFrameValid( textFrame );
			fail( "This frame is not valid" );
		} catch ( Exception e ) {
			//
		}
		textFrame.setRSV1( false );
		textFrame.setRSV2( true );
		try {
			defaultExtension.isFrameValid( textFrame );
			fail( "This frame is not valid" );
		} catch ( Exception e ) {
			//
		}
		textFrame.setRSV2( false );
		textFrame.setRSV3( true );
		try {
			defaultExtension.isFrameValid( textFrame );
			fail( "This frame is not valid" );
		} catch ( Exception e ) {
			//
		}
	}

	@Test
	public void testGetProvidedExtensionAsClient() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		assertEquals( "", defaultExtension.getProvidedExtensionAsClient() );
	}

	@Test
	public void testGetProvidedExtensionAsServer() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		assertEquals( "", defaultExtension.getProvidedExtensionAsServer() );
	}

	@Test
	public void testCopyInstance() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		IExtension extensionCopy = defaultExtension.copyInstance();
		assertEquals( defaultExtension, extensionCopy );
	}

	@Test
	public void testToString() throws Exception {
		DefaultExtension defaultExtension = new DefaultExtension();
		assertEquals( "DefaultExtension", defaultExtension.toString() );
	}

	@Test
	public void testHashCode() throws Exception {
		DefaultExtension defaultExtension0 = new DefaultExtension();
		DefaultExtension defaultExtension1 = new DefaultExtension();
		assertEquals( defaultExtension0.hashCode(), defaultExtension1.hashCode() );
	}

	@Test
	public void testEquals() throws Exception {
		DefaultExtension defaultExtension0 = new DefaultExtension();
		DefaultExtension defaultExtension1 = new DefaultExtension();
		assertEquals( defaultExtension0, defaultExtension1 );
	}

}