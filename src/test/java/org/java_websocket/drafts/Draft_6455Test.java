/*
 * Copyright (c) 2010-2018 Nathan Rajlich
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

import org.java_websocket.enums.CloseHandshakeType;
import org.java_websocket.enums.HandshakeState;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.BinaryFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.TextFrame;
import org.java_websocket.handshake.HandshakeImpl1Client;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.java_websocket.util.Charsetfunctions;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		try {
			Draft_6455 draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>emptyList(), -1 );
			fail( "IllegalArgumentException expected" );
		} catch ( IllegalArgumentException e ) {
			//Fine
		}
		try {
			Draft_6455 draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>emptyList(), 0 );
			fail( "IllegalArgumentException expected" );
		} catch ( IllegalArgumentException e ) {
			//Fine
		}
		Draft_6455 draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>emptyList() );
		assertEquals( 1, draft_6455.getKnownExtensions().size() );
		assertEquals( 0, draft_6455.getKnownProtocols().size() );
	}

	@Test
	public void testGetExtension() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertNotNull( draft_6455.getExtension() );
		assert ( draft_6455.getExtension() instanceof DefaultExtension );
	}

	@Test
	public void testGetKnownExtensions() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertEquals( 1, draft_6455.getKnownExtensions().size() );
		draft_6455 = new Draft_6455( new DefaultExtension() );
		assertEquals( 1, draft_6455.getKnownExtensions().size() );
		draft_6455 = new Draft_6455( new TestExtension() );
		assertEquals( 2, draft_6455.getKnownExtensions().size() );
	}

	@Test
	public void testGetProtocol() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertNull( draft_6455.getProtocol() );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertNull( draft_6455.getProtocol() );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		assertNull( draft_6455.getProtocol() );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertNotNull( draft_6455.getProtocol() );
	}

	@Test
	public void testGetKnownProtocols() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertEquals( 1, draft_6455.getKnownProtocols().size() );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>emptyList() );
		assertEquals( 0, draft_6455.getKnownProtocols().size() );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		assertEquals( 1, draft_6455.getKnownProtocols().size() );
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "chat" ) );
		protocols.add( new Protocol( "test" ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), protocols );
		assertEquals( 2, draft_6455.getKnownProtocols().size() );
	}

	@Test
	public void testCopyInstance() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455( Collections.<IExtension>singletonList( new TestExtension() ), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		Draft_6455 draftCopy = ( Draft_6455 ) draft_6455.copyInstance();
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertNotEquals( draft_6455, draftCopy );
		assertEquals( draft_6455.getKnownProtocols(), draftCopy.getKnownProtocols() );
		assertEquals( draft_6455.getKnownExtensions(), draftCopy.getKnownExtensions() );
		assertNotEquals( draft_6455.getProtocol(), draftCopy.getProtocol() );
		assertNotEquals( draft_6455.getExtension(), draftCopy.getExtension() );
	}

	@Test
	public void testReset() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455( Collections.<IExtension>singletonList( new TestExtension() ), 100 );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		List<IExtension> extensionList = new ArrayList<IExtension>( draft_6455.getKnownExtensions() );
		List<IProtocol> protocolList = new ArrayList<IProtocol>( draft_6455.getKnownProtocols() );
		draft_6455.reset();
		//Protocol and extension should be reset
		assertEquals( new DefaultExtension(), draft_6455.getExtension() );
		assertNull( draft_6455.getProtocol() );
		assertEquals( extensionList, draft_6455.getKnownExtensions() );
		assertEquals( protocolList, draft_6455.getKnownProtocols() );
	}

	@Test
	public void testGetCloseHandshakeType() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertEquals( CloseHandshakeType.TWOWAY, draft_6455.getCloseHandshakeType() );
	}

	@Test
	public void testToString() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertEquals( "Draft_6455 extension: DefaultExtension max frame size: 2147483647", draft_6455.toString() );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertEquals( "Draft_6455 extension: DefaultExtension max frame size: 2147483647", draft_6455.toString() );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		assertEquals( "Draft_6455 extension: DefaultExtension max frame size: 2147483647", draft_6455.toString() );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertEquals( "Draft_6455 extension: DefaultExtension protocol: chat max frame size: 2147483647", draft_6455.toString() );
		draft_6455 = new Draft_6455( Collections.<IExtension>singletonList( new TestExtension() ), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		assertEquals( "Draft_6455 extension: DefaultExtension max frame size: 2147483647", draft_6455.toString() );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertEquals( "Draft_6455 extension: TestExtension protocol: chat max frame size: 2147483647", draft_6455.toString() );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) ,10);
		assertEquals( "Draft_6455 extension: DefaultExtension max frame size: 10", draft_6455.toString() );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		assertEquals( "Draft_6455 extension: DefaultExtension protocol: chat max frame size: 10", draft_6455.toString() );
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

	@Test
	public void acceptHandshakeAsServer() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedata ) );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataProtocol ) );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataExtension ) );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension ) );
		draft_6455 = new Draft_6455( new TestExtension() );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedata ) );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataProtocol ) );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataExtension ) );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsServer( handshakedata ) );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataProtocol ) );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataExtension ) );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension ) );
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "chat" ) );
		protocols.add( new Protocol( "" ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), protocols );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedata ) );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataProtocol ) );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataExtension ) );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension ) );
	}

	@Test
	public void acceptHandshakeAsClient() throws Exception {
		HandshakeImpl1Server response = new HandshakeImpl1Server();
		HandshakeImpl1Client request = new HandshakeImpl1Client();
		Draft_6455 draft_6455 = new Draft_6455();
		response.put( "Upgrade", "websocket" );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		response.put( "Connection", "upgrade" );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		response.put( "Sec-WebSocket-Version", "13" );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		request.put( "Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==" );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		response.put( "Sec-WebSocket-Accept", "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=" );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		response.put( "Sec-WebSocket-Protocol", "chat" );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol( "chat" ) ) );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "" ) );
		protocols.add( new Protocol( "chat" ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), protocols );
		assertEquals( HandshakeState.MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		draft_6455 = new Draft_6455();
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
		protocols.clear();
		protocols.add( new Protocol( "chat3" ) );
		protocols.add( new Protocol( "3chat" ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), protocols );
		assertEquals( HandshakeState.NOT_MATCHED, draft_6455.acceptHandshakeAsClient( request, response ) );
	}

	@Test
	public void postProcessHandshakeRequestAsClient() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		HandshakeImpl1Client request = new HandshakeImpl1Client();
		draft_6455.postProcessHandshakeRequestAsClient( request );
		assertEquals( "websocket", request.getFieldValue( "Upgrade" ) );
		assertEquals( "Upgrade", request.getFieldValue( "Connection" ) );
		assertEquals( "13", request.getFieldValue( "Sec-WebSocket-Version" ) );
		assertTrue( request.hasFieldValue( "Sec-WebSocket-Key" ) );
		assertTrue( !request.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		assertTrue( !request.hasFieldValue( "Sec-WebSocket-Protocol" ) );
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "chat" ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), protocols );
		request = new HandshakeImpl1Client();
		draft_6455.postProcessHandshakeRequestAsClient( request );
		assertTrue( !request.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		assertEquals( "chat", request.getFieldValue( "Sec-WebSocket-Protocol" ) );
		protocols.add( new Protocol( "chat2" ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), protocols );
		request = new HandshakeImpl1Client();
		draft_6455.postProcessHandshakeRequestAsClient( request );
		assertTrue( !request.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		assertEquals( "chat, chat2", request.getFieldValue( "Sec-WebSocket-Protocol" ) );
		protocols.clear();
		protocols.add( new Protocol( "" ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), protocols );
		request = new HandshakeImpl1Client();
		draft_6455.postProcessHandshakeRequestAsClient( request );
		assertTrue( !request.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		assertTrue( !request.hasFieldValue( "Sec-WebSocket-Protocol" ) );
	}

	@Test
	public void postProcessHandshakeResponseAsServer() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		HandshakeImpl1Server response = new HandshakeImpl1Server();
		HandshakeImpl1Client request = new HandshakeImpl1Client();
		request.put( "Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==" );
		request.put( "Connection", "upgrade" );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertTrue( response.hasFieldValue( "Date" ) );
		assertTrue( response.hasFieldValue( "Sec-WebSocket-Accept" ) );
		assertEquals( "Web Socket Protocol Handshake", response.getHttpStatusMessage() );
		assertEquals( "TooTallNate Java-WebSocket", response.getFieldValue( "Server" ) );
		assertEquals( "upgrade", response.getFieldValue( "Connection" ) );
		assertEquals( "websocket", response.getFieldValue( "Upgrade" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Protocol" ) );
		response = new HandshakeImpl1Server();
		draft_6455.acceptHandshakeAsServer( handshakedata );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		response = new HandshakeImpl1Server();
		draft_6455.acceptHandshakeAsServer( handshakedataProtocol );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		response = new HandshakeImpl1Server();
		draft_6455.acceptHandshakeAsServer( handshakedataExtension );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		response = new HandshakeImpl1Server();
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		response = new HandshakeImpl1Server();
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList( new Protocol("chat") ) );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocol );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertEquals( "chat", response.getFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		response = new HandshakeImpl1Server();
		draft_6455.reset();
		draft_6455.acceptHandshakeAsServer( handshakedataExtension );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		response = new HandshakeImpl1Server();
		draft_6455.reset();
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertEquals( "chat", response.getFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
		protocols.add( new Protocol( "test" ) );
		protocols.add( new Protocol( "chat" ) );
		draft_6455 = new Draft_6455( Collections.<IExtension>emptyList(), protocols );
		draft_6455.acceptHandshakeAsServer( handshakedataProtocol );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertEquals( "test", response.getFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		response = new HandshakeImpl1Server();
		draft_6455.reset();
		draft_6455.acceptHandshakeAsServer( handshakedataExtension );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
		response = new HandshakeImpl1Server();
		draft_6455.reset();
		draft_6455.acceptHandshakeAsServer( handshakedataProtocolExtension );
		draft_6455.postProcessHandshakeResponseAsServer(request, response);
		assertEquals( "test", response.getFieldValue( "Sec-WebSocket-Protocol" ) );
		assertTrue( !response.hasFieldValue( "Sec-WebSocket-Extensions" ) );
	}


	@Test
	public void createFramesBinary() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		BinaryFrame curframe = new BinaryFrame();
		ByteBuffer test0 = ByteBuffer.wrap( "Test0".getBytes() );
		curframe.setPayload( test0 );
		curframe.setTransferemasked( false );
		List<Framedata> createdFrame = draft_6455.createFrames( test0, false );
		assertEquals( 1, createdFrame.size() );
		assertEquals( curframe, createdFrame.get( 0 ) );
		curframe = new BinaryFrame();
		ByteBuffer test1 = ByteBuffer.wrap( "Test1".getBytes() );
		curframe.setPayload( test1 );
		curframe.setTransferemasked( true );
		createdFrame = draft_6455.createFrames( test1, true );
		assertEquals( 1, createdFrame.size() );
		assertEquals( curframe, createdFrame.get( 0 ) );
	}

	@Test
	public void createFramesText() throws Exception {
		Draft_6455 draft_6455 = new Draft_6455();
		TextFrame curframe = new TextFrame();
		curframe.setPayload( ByteBuffer.wrap( Charsetfunctions.utf8Bytes( "Test0" ) ) );
		curframe.setTransferemasked( false );
		List<Framedata> createdFrame = draft_6455.createFrames( "Test0", false );
		assertEquals( 1, createdFrame.size() );
		assertEquals( curframe, createdFrame.get( 0 ) );
		curframe = new TextFrame();
		curframe.setPayload( ByteBuffer.wrap( Charsetfunctions.utf8Bytes( "Test0" ) ) );
		curframe.setTransferemasked( true );
		createdFrame = draft_6455.createFrames( "Test0", true );
		assertEquals( 1, createdFrame.size() );
		assertEquals( curframe, createdFrame.get( 0 ) );
	}


	private class TestExtension extends DefaultExtension {
		@Override
		public int hashCode() {
			return getClass().hashCode();
		}

		@Override
		public IExtension copyInstance() {
			return new TestExtension();
		}

		@Override
		public boolean equals( Object o ) {
			if( this == o ) return true;
			if( o == null ) return false;
			return getClass() == o.getClass();
		}
	}
}