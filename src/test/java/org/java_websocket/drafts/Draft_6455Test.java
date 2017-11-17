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

package org.java_websocket.drafts;

import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.handshake.HandshakeImpl1Client;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class Draft_6455Test {

	HandshakeImpl1Client handshakedataProtocolExtension;
	HandshakeImpl1Client handshakedataProtocol;
	HandshakeImpl1Client handshakedataExtension;
	HandshakeImpl1Client handshakedata;

	public Draft_6455Test() {
		handshakedataProtocolExtension = new HandshakeImpl1Client();
		handshakedataProtocolExtension.put( "Upgrade", "websocket" );
		handshakedataProtocolExtension.put( "Connection", "Upgrade" );
		handshakedataProtocolExtension.put( "Sec-WebSocket-Version", "13" );
		handshakedataProtocolExtension.put( "Sec-WebSocket-Extension", "permessage-deflate" );
		handshakedataProtocolExtension.put( "Sec-WebSocket-Protocol", "chat, test" );
		handshakedataProtocol = new HandshakeImpl1Client();
		handshakedataProtocol.put( "Upgrade", "websocket" );
		handshakedataProtocol.put( "Connection", "Upgrade" );
		handshakedataProtocol.put( "Sec-WebSocket-Version", "13" );
		handshakedataProtocol.put( "Sec-WebSocket-Protocol", "chat, test" );
		handshakedataExtension = new HandshakeImpl1Client();
		handshakedataExtension.put( "Upgrade", "websocket" );
		handshakedataExtension.put( "Connection", "Upgrade" );
		handshakedataExtension.put( "Sec-WebSocket-Version", "13" );
		handshakedataExtension.put( "Sec-WebSocket-Extension", "permessage-deflate" );
		handshakedata = new HandshakeImpl1Client();
		handshakedata.put( "Upgrade", "websocket" );
		handshakedata.put( "Connection", "Upgrade" );
		handshakedata.put( "Sec-WebSocket-Version", "13" );
	}

	@Test
	public void testConstructor() throws Exception {
		try {
			Draft_6455 draft_6455 = new Draft_6455( null, null );
			fail( "IllegalArgumentException expected" );
		} catch ( IllegalArgumentException e ) {
			//Fine
		}
		try {
			Draft_6455 draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), null );
			fail( "IllegalArgumentException expected" );
		} catch ( IllegalArgumentException e ) {
			//Fine
		}
		try {
			Draft_6455 draft_6455 = new Draft_6455( null, Collections.<IProtocol>emptyList() );
			fail( "IllegalArgumentException expected" );
		} catch ( IllegalArgumentException e ) {
			//Fine
		}
		Draft_6455 draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>emptyList() );
		assertEquals( draft_6455.getKnownExtensions().size(), 1 );
		assertEquals( draft_6455.getKnownProtocols().size(), 0 );
	}

	@Test
	public void testGetExtension() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertNotNull( draft_6455.getExtension() );
		assert ( draft_6455.getExtension() instanceof DefaultExtension );
	}

	@Test
	public void testGetKnownExtensions() throws Exception {
		//Wird automatisch default hinzugefügt?
		//TODO
	}

	@Test
	public void testGetProtocol() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertNull( draft_6455.getProtocol() );
		//TODO
	}

	@Test
	public void testGetKnownProtocols() throws Exception {
	}

	@Test
	public void testCopyInstance() throws Exception {
	}

	@Test
	public void testReset() throws Exception {
	}

	@Test
	public void testGetCloseHandshakeType() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertEquals( draft_6455.getCloseHandshakeType(), Draft.CloseHandshakeType.TWOWAY );
	}

	@Test
	public void testToString() throws Exception {
	}

	@Test
	public void testEquals() throws Exception {
		Draft draft0 = new Draft_6455();
		Draft draft1 = draft0.copyInstance();
		assertEquals( draft0, draft1 );
		Draft draft2 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		Draft draft3 = draft2.copyInstance();
		assertEquals( draft2, draft3 );
		assertEquals( draft0, draft2 );
		//unequal for draft2 due to a provided protocol
		draft2.acceptHandshakeAsServer( handshakedataProtocolExtension );
		draft1.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertNotEquals( draft2, draft3 );
		assertNotEquals( draft0, draft2 );
		assertEquals( draft0, draft1 );
		draft2 = draft2.copyInstance();
		draft1 = draft1.copyInstance();
		//unequal for draft draft2 due to a provided protocol
		draft2.acceptHandshakeAsServer( handshakedataProtocol );
		draft1.acceptHandshakeAsServer( handshakedataProtocol );
		assertNotEquals( draft2, draft3 );
		assertNotEquals( draft0, draft2 );
		assertEquals( draft0, draft1 );
		draft2 = draft2.copyInstance();
		draft1 = draft1.copyInstance();
		//unequal for draft draft0 due to a provided protocol (no protocol)
		draft2.acceptHandshakeAsServer( handshakedataExtension );
		draft1.acceptHandshakeAsServer( handshakedataExtension );
		assertEquals( draft2, draft3 );
		assertEquals( draft0, draft2 );
		assertNotEquals( draft0, draft1 );
		draft2 = draft2.copyInstance();
		draft1 = draft1.copyInstance();
		//unequal for draft draft0 due to a provided protocol (no protocol)
		draft2.acceptHandshakeAsServer( handshakedata );
		draft1.acceptHandshakeAsServer( handshakedata );
		assertEquals( draft2, draft3 );
		assertEquals( draft0, draft2 );
		assertNotEquals( draft0, draft1 );
	}

	@Test
	public void testHashCode() throws Exception {
		Draft draft0 = new Draft_6455();
		Draft draft1 = draft0.copyInstance();
		Draft draft2 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		Draft draft3 = draft2.copyInstance();
		assertEquals( draft2.hashCode(), draft3.hashCode() );
		assertEquals( draft0.hashCode(), draft2.hashCode() );
		assertEquals( draft0.hashCode(), draft1.hashCode() );
		//Hashcode changes for draft2 due to a provided protocol
		draft2.acceptHandshakeAsServer( handshakedataProtocolExtension );
		draft1.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertNotEquals( draft2.hashCode(), draft3.hashCode() );
		assertNotEquals( draft0.hashCode(), draft2.hashCode() );
		assertEquals( draft0.hashCode(), draft1.hashCode() );
		draft2 = draft2.copyInstance();
		draft1 = draft1.copyInstance();
		//Hashcode changes for draft draft2 due to a provided protocol
		draft2.acceptHandshakeAsServer( handshakedataProtocol );
		draft1.acceptHandshakeAsServer( handshakedataProtocol );
		assertNotEquals( draft2.hashCode(), draft3.hashCode() );
		assertNotEquals( draft0.hashCode(), draft2.hashCode() );
		assertEquals( draft0.hashCode(), draft1.hashCode() );
		draft2 = draft2.copyInstance();
		draft1 = draft1.copyInstance();
		//Hashcode changes for draft draft0 due to a provided protocol (no protocol)
		draft2.acceptHandshakeAsServer( handshakedataExtension );
		draft1.acceptHandshakeAsServer( handshakedataExtension );
		assertEquals( draft2.hashCode(), draft3.hashCode() );
		assertEquals( draft0.hashCode(), draft2.hashCode() );
		// THIS IS A DIFFERENCE BETWEEN equals and hashcode since the hashcode of an empty string = 0
		assertEquals( draft0.hashCode(), draft1.hashCode() );
		draft2 = draft2.copyInstance();
		draft1 = draft1.copyInstance();
		//Hashcode changes for draft draft0 due to a provided protocol (no protocol)
		draft2.acceptHandshakeAsServer( handshakedata );
		draft1.acceptHandshakeAsServer( handshakedata );
		assertEquals( draft2.hashCode(), draft3.hashCode() );
		assertEquals( draft0.hashCode(), draft2.hashCode() );
		// THIS IS A DIFFERENCE BETWEEN equals and hashcode since the hashcode of an empty string = 0
		assertEquals( draft0.hashCode(), draft1.hashCode() );
	}

}