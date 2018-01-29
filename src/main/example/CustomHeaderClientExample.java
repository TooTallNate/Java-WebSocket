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

import org.java_websocket.WebSocketImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class shows how to add additional http header like "Origin" or "Cookie".
 *
 * To see it working, start ServerRejectHandshakeExample and then start this example.
 */
public class CustomHeaderClientExample {

	public static void main( String[] args ) throws URISyntaxException, InterruptedException {
		WebSocketImpl.DEBUG = true;
		Map<String,String> httpHeaders = new HashMap<String, String>();
		httpHeaders.put( "Cookie", "test" );
		ExampleClient c = new ExampleClient( new URI( "ws://localhost:8887" ), httpHeaders);
		//We expect no successful connection
		c.connectBlocking();
		httpHeaders.put( "Cookie", "username=nemo" );
		c = new ExampleClient( new URI( "ws://localhost:8887" ) , httpHeaders);
		//Wer expect a successful connection
		c.connectBlocking();
		c.closeBlocking();
		httpHeaders.put( "Access-Control-Allow-Origin", "*" );
		c = new ExampleClient( new URI( "ws://localhost:8887" ) , httpHeaders);
		//We expect no successful connection
		c.connectBlocking();
		c.closeBlocking();
		httpHeaders.clear();
		httpHeaders.put( "Origin", "localhost:8887" );
		httpHeaders.put( "Cookie", "username=nemo" );
		c = new ExampleClient( new URI( "ws://localhost:8887" ) , httpHeaders);
		//We expect a successful connection
		c.connectBlocking();
		c.closeBlocking();
		httpHeaders.clear();
		httpHeaders.put( "Origin", "localhost" );
		httpHeaders.put( "cookie", "username=nemo" );
		c = new ExampleClient( new URI( "ws://localhost:8887" ) , httpHeaders);
		//We expect no successful connection
		c.connectBlocking();
	}
}
