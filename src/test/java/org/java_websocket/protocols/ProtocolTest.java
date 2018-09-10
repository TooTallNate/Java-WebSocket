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

package org.java_websocket.protocols;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProtocolTest {

	@Test
	public void testConstructor() throws Exception {
		Protocol protocol0 = new Protocol( "" );
		try {
			Protocol protocol1 = new Protocol( null );
			fail( "IllegalArgumentException expected" );
		} catch ( IllegalArgumentException e ) {
			//Fine
		}
	}

	@Test
	public void testAcceptProvidedProtocol() throws Exception {
		Protocol protocol0 = new Protocol( "" );
		assertTrue( protocol0.acceptProvidedProtocol( "" ) );
		assertTrue( !protocol0.acceptProvidedProtocol( "chat" ) );
		assertTrue( !protocol0.acceptProvidedProtocol( "chat, test" ) );
		assertTrue( !protocol0.acceptProvidedProtocol( "chat, test," ) );
		Protocol protocol1 = new Protocol( "chat" );
		assertTrue( protocol1.acceptProvidedProtocol( "chat" ) );
		assertTrue( !protocol1.acceptProvidedProtocol( "test" ) );
		assertTrue( protocol1.acceptProvidedProtocol( "chat, test" ) );
		assertTrue( protocol1.acceptProvidedProtocol( "test, chat" ) );
		assertTrue( protocol1.acceptProvidedProtocol( "test,chat" ) );
		assertTrue( protocol1.acceptProvidedProtocol( "chat,test" ) );
		assertTrue( protocol1.acceptProvidedProtocol( "asdchattest,test, chat" ) );
	}

	@Test
	public void testGetProvidedProtocol() throws Exception {
		Protocol protocol0 = new Protocol( "" );
		assertEquals( protocol0.getProvidedProtocol(), "" );
		Protocol protocol1 = new Protocol( "protocol" );
		assertEquals( protocol1.getProvidedProtocol(), "protocol" );
	}

	@Test
	public void testCopyInstance() throws Exception {
		IProtocol protocol0 = new Protocol( "" );
		IProtocol protoocl1 = protocol0.copyInstance();
		assertEquals( protocol0, protoocl1 );
		IProtocol protocol2 = new Protocol( "protocol" );
		IProtocol protocol3 = protocol2.copyInstance();
		assertEquals( protocol2, protocol3 );
	}

	@Test
	public void testToString() throws Exception {
		Protocol protocol0 = new Protocol( "" );
		assertEquals( protocol0.getProvidedProtocol(), "" );
		Protocol protocol1 = new Protocol( "protocol" );
		assertEquals( protocol1.getProvidedProtocol(), "protocol" );
	}

	@Test
	public void testEquals() throws Exception {
		Protocol protocol0 = new Protocol( "" );
		Protocol protocol1 = new Protocol( "protocol" );
		Protocol protocol2 = new Protocol( "protocol" );
		assertTrue( !protocol0.equals( protocol1 ) );
		assertTrue( !protocol0.equals( protocol2 ) );
		assertTrue( protocol1.equals( protocol2 ) );
		assertTrue( !protocol1.equals( null ) );
		assertTrue( !protocol1.equals( new Object() ) );
	}

	@Test
	public void testHashCode() throws Exception {
		Protocol protocol0 = new Protocol( "" );
		Protocol protocol1 = new Protocol( "protocol" );
		Protocol protocol2 = new Protocol( "protocol" );
		assertNotEquals( protocol0, protocol1 );
		assertNotEquals( protocol0, protocol2 );
		assertEquals( protocol1, protocol2 );
	}

}