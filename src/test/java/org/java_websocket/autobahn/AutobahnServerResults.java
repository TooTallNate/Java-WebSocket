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

package org.java_websocket.autobahn;

import org.json.JSONObject;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AutobahnServerResults {

	static JSONObject jsonObject = null;

	@BeforeClass
	public static void getJSONObject() throws FileNotFoundException {
		File file = new File( "reports/servers/index.json" );
		//File file = new File( "C:\\Python27\\Scripts\\reports\\servers\\index.json" );
		if( file.exists() ) {
			String content = new Scanner( file ).useDelimiter("\\Z").next();
			jsonObject = new JSONObject( content );
			jsonObject = jsonObject.getJSONObject( "TooTallNate Java-WebSocket" );
		}
		Assume.assumeTrue( jsonObject != null );
	}

	@Test
	public void test1_1_1() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.1" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_1_2() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.2" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_1_3() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.3" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_1_4() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.4" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_1_5() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.5" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_1_6() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.6" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 30);
	}
	@Test
	public void test1_1_7() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.7" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 20);
	}
	@Test
	public void test1_1_8() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.8" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 30);
	}

	@Test
	public void test1_2_1() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.1" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_2_2() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.2" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_2_3() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.3" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_2_4() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.4" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_2_5() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.5" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 10);
	}
	@Test
	public void test1_2_6() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.6" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 70);
	}
	@Test
	public void test1_2_8() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.8" );
		assertEquals("OK", testResult.get( "behavior" ));
		assertEquals("OK", testResult.get( "behaviorClose" ));
		assertTrue(testResult.getInt( "duration" ) < 60);
	}
}
