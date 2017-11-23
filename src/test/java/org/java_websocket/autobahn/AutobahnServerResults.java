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
		//File file = new File( "reports/servers/index.json" );
		File file = new File( "C:\\Python27\\Scripts\\reports\\servers\\index.json" );
		if( file.exists() ) {
			String content = new Scanner( file ).useDelimiter( "\\Z" ).next();
			jsonObject = new JSONObject( content );
			jsonObject = jsonObject.getJSONObject( "TooTallNate Java-WebSocket" );
		}
		Assume.assumeTrue( jsonObject != null );
	}

	@Test
	public void test1_1_1() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_1_2() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_1_3() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_1_4() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_1_5() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_1_6() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 30 );
	}

	@Test
	public void test1_1_7() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 20 );
	}

	@Test
	public void test1_1_8() {
		JSONObject testResult = jsonObject.getJSONObject( "1.1.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 30 );
	}

	@Test
	public void test1_2_1() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_2_2() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_2_3() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_2_4() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 20 );
	}

	@Test
	public void test1_2_5() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test1_2_6() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 70 );
	}
	@Test
	public void test1_2_7() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 70 );
	}

	@Test
	public void test1_2_8() {
		JSONObject testResult = jsonObject.getJSONObject( "1.2.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 60 );
	}

	@Test
	public void test2_1() {
		JSONObject testResult = jsonObject.getJSONObject( "2.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test2_2() {
		JSONObject testResult = jsonObject.getJSONObject( "2.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test2_3() {
		JSONObject testResult = jsonObject.getJSONObject( "2.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test2_4() {
		JSONObject testResult = jsonObject.getJSONObject( "2.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test2_5() {
		JSONObject testResult = jsonObject.getJSONObject( "2.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test2_6() {
		JSONObject testResult = jsonObject.getJSONObject( "2.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 30 );
	}

	@Test
	public void test2_7() {
		JSONObject testResult = jsonObject.getJSONObject( "2.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test2_8() {
		JSONObject testResult = jsonObject.getJSONObject( "2.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test2_9() {
		JSONObject testResult = jsonObject.getJSONObject( "2.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test2_10() {
		JSONObject testResult = jsonObject.getJSONObject( "2.10" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 60 );
	}

	@Test
	public void test2_11() {
		JSONObject testResult = jsonObject.getJSONObject( "2.11" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 50 );
	}

	@Test
	public void test3_1() {
		JSONObject testResult = jsonObject.getJSONObject( "3.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test3_2() {
		JSONObject testResult = jsonObject.getJSONObject( "3.2" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test3_3() {
		JSONObject testResult = jsonObject.getJSONObject( "3.3" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test3_4() {
		JSONObject testResult = jsonObject.getJSONObject( "3.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 20 );
	}

	@Test
	public void test3_5() {
		JSONObject testResult = jsonObject.getJSONObject( "3.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test3_6() {
		JSONObject testResult = jsonObject.getJSONObject( "3.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test3_7() {
		JSONObject testResult = jsonObject.getJSONObject( "3.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_1_1() {
		JSONObject testResult = jsonObject.getJSONObject( "4.1.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_1_2() {
		JSONObject testResult = jsonObject.getJSONObject( "4.1.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_1_3() {
		JSONObject testResult = jsonObject.getJSONObject( "4.1.3" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_1_4() {
		JSONObject testResult = jsonObject.getJSONObject( "4.1.4" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_1_5() {
		JSONObject testResult = jsonObject.getJSONObject( "4.1.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_2_1() {
		JSONObject testResult = jsonObject.getJSONObject( "4.2.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_2_2() {
		JSONObject testResult = jsonObject.getJSONObject( "4.2.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_2_3() {
		JSONObject testResult = jsonObject.getJSONObject( "4.2.3" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_2_4() {
		JSONObject testResult = jsonObject.getJSONObject( "4.2.4" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test4_2_5() {
		JSONObject testResult = jsonObject.getJSONObject( "4.2.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 15 );
	}

	@Test
	public void test5_1() {
		JSONObject testResult = jsonObject.getJSONObject( "5.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_2() {
		JSONObject testResult = jsonObject.getJSONObject( "5.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_3() {
		JSONObject testResult = jsonObject.getJSONObject( "5.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_4() {
		JSONObject testResult = jsonObject.getJSONObject( "5.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_5() {
		JSONObject testResult = jsonObject.getJSONObject( "5.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 20 );
	}

	@Test
	public void test5_6() {
		JSONObject testResult = jsonObject.getJSONObject( "5.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 60 );
	}

	@Test
	public void test5_7() {
		JSONObject testResult = jsonObject.getJSONObject( "5.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 60 );
	}

	@Test
	public void test5_8() {
		JSONObject testResult = jsonObject.getJSONObject( "5.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 20 );
	}

	@Test
	public void test5_9() {
		JSONObject testResult = jsonObject.getJSONObject( "5.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_10() {
		JSONObject testResult = jsonObject.getJSONObject( "5.10" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_11() {
		JSONObject testResult = jsonObject.getJSONObject( "5.11" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 20 );
	}

	@Test
	public void test5_12() {
		JSONObject testResult = jsonObject.getJSONObject( "5.12" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_13() {
		JSONObject testResult = jsonObject.getJSONObject( "5.13" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_14() {
		JSONObject testResult = jsonObject.getJSONObject( "5.14" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_15() {
		JSONObject testResult = jsonObject.getJSONObject( "5.15" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_16() {
		JSONObject testResult = jsonObject.getJSONObject( "5.16" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_17() {
		JSONObject testResult = jsonObject.getJSONObject( "5.17" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_18() {
		JSONObject testResult = jsonObject.getJSONObject( "5.18" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test5_19() {
		JSONObject testResult = jsonObject.getJSONObject( "5.19" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 1100 );
	}

	@Test
	public void test5_20() {
		JSONObject testResult = jsonObject.getJSONObject( "5.20" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 1100 );
	}

	@Test
	public void test6_1_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.1.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_1_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.1.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_1_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.1.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_2_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.2.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_2_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.2.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_2_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.2.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_2_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.2.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_3_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.3.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_3_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.3.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_4_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.4.1" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 2100 );
	}

	@Test
	public void test6_4_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.4.2" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 2100 );
	}

	@Test
	public void test6_4_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.4.3" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 2100 );
	}

	@Test
	public void test6_4_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.4.4" );
		assertEquals( "NON-STRICT", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 2100 );
	}

	@Test
	public void test6_5_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.5.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_5_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.5.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_5_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.5.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_5_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.5.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_5_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.5.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_6() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_7() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_8() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_9() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_10() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.10" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_6_11() {
		JSONObject testResult = jsonObject.getJSONObject( "6.6.11" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_7_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.7.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_7_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.7.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_7_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.7.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_7_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.7.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_8_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.8.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_8_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.8.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_9_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.9.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_9_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.9.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_9_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.9.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_9_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.9.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_10_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.10.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_10_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.10.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_10_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.10.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_11_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.11.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_11_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.11.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_11_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.11.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_11_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.11.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_11_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.11.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_12_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.12.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_12_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.12.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_12_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.12.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_12_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.12.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_12_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.12.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_12_6() {
		JSONObject testResult = jsonObject.getJSONObject( "6.12.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_12_7() {
		JSONObject testResult = jsonObject.getJSONObject( "6.12.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_12_8() {
		JSONObject testResult = jsonObject.getJSONObject( "6.12.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_13_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.13.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_13_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.13.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_13_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.13.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_13_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.13.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_13_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.13.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_6() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_7() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_8() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_9() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_14_10() {
		JSONObject testResult = jsonObject.getJSONObject( "6.14.10" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_15_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.15.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_16_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.16.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_16_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.16.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_16_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.16.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_17_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.17.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_17_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.17.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_17_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.17.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_17_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.17.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_17_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.17.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_18_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.18.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_18_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.18.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_18_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.18.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_18_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.18.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_18_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.18.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_19_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.19.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_19_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.19.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_19_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.19.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_19_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.19.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_19_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.19.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_20_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.20.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_20_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.20.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_20_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.20.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_20_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.20.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_20_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.20.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_20_6() {
		JSONObject testResult = jsonObject.getJSONObject( "6.20.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_20_7() {
		JSONObject testResult = jsonObject.getJSONObject( "6.20.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_21_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.21.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_21_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.21.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_21_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.21.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_21_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.21.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_21_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.21.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_21_6() {
		JSONObject testResult = jsonObject.getJSONObject( "6.21.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_21_7() {
		JSONObject testResult = jsonObject.getJSONObject( "6.21.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_21_8() {
		JSONObject testResult = jsonObject.getJSONObject( "6.21.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_6() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_7() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_8() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_9() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_10() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.10" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_11() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.11" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_12() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.12" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_13() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.13" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_14() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.14" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_15() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.15" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_16() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.16" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_17() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.17" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_18() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.18" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_19() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.19" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_20() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.20" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_21() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.21" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_22() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.22" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_23() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.23" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_24() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.24" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_25() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.25" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_26() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.26" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_27() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.27" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_28() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.28" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_29() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.29" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_30() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.30" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_31() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.31" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_32() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.32" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_33() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.33" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_22_34() {
		JSONObject testResult = jsonObject.getJSONObject( "6.22.34" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_23_1() {
		JSONObject testResult = jsonObject.getJSONObject( "6.23.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_23_2() {
		JSONObject testResult = jsonObject.getJSONObject( "6.23.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_23_3() {
		JSONObject testResult = jsonObject.getJSONObject( "6.23.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_23_4() {
		JSONObject testResult = jsonObject.getJSONObject( "6.23.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_23_5() {
		JSONObject testResult = jsonObject.getJSONObject( "6.23.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_23_6() {
		JSONObject testResult = jsonObject.getJSONObject( "6.23.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test6_23_7() {
		JSONObject testResult = jsonObject.getJSONObject( "6.23.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_1_1() {
		JSONObject testResult = jsonObject.getJSONObject( "7.1.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_1_2() {
		JSONObject testResult = jsonObject.getJSONObject( "7.1.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_1_3() {
		JSONObject testResult = jsonObject.getJSONObject( "7.1.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_1_4() {
		JSONObject testResult = jsonObject.getJSONObject( "7.1.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_1_5() {
		JSONObject testResult = jsonObject.getJSONObject( "7.1.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_1_6() {
		JSONObject testResult = jsonObject.getJSONObject( "7.1.6" );
		assertEquals( "INFORMATIONAL", testResult.get( "behavior" ) );
		assertEquals( "INFORMATIONAL", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 50 );
	}

	@Test
	public void test7_3_1() {
		JSONObject testResult = jsonObject.getJSONObject( "7.3.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_3_2() {
		JSONObject testResult = jsonObject.getJSONObject( "7.3.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_3_3() {
		JSONObject testResult = jsonObject.getJSONObject( "7.3.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_3_4() {
		JSONObject testResult = jsonObject.getJSONObject( "7.3.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_3_5() {
		JSONObject testResult = jsonObject.getJSONObject( "7.3.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_3_6() {
		JSONObject testResult = jsonObject.getJSONObject( "7.3.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_5_1() {
		JSONObject testResult = jsonObject.getJSONObject( "7.5.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_1() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_2() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_3() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_4() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_5() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_6() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_7() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_8() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_9() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_10() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.10" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_11() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.11" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_12() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.12" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_7_13() {
		JSONObject testResult = jsonObject.getJSONObject( "7.7.13" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_1() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_2() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_3() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_4() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_5() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_6() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_7() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_8() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_9() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_10() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.10" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_9_11() {
		JSONObject testResult = jsonObject.getJSONObject( "7.9.11" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_13_1() {
		JSONObject testResult = jsonObject.getJSONObject( "7.13.1" );
		assertEquals( "INFORMATIONAL", testResult.get( "behavior" ) );
		assertEquals( "INFORMATIONAL", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test7_13_2() {
		JSONObject testResult = jsonObject.getJSONObject( "7.13.2" );
		assertEquals( "INFORMATIONAL", testResult.get( "behavior" ) );
		assertEquals( "INFORMATIONAL", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test9_1_1() {
		JSONObject testResult = jsonObject.getJSONObject( "9.1.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test9_1_2() {
		JSONObject testResult = jsonObject.getJSONObject( "9.1.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 20 );
	}

	@Test
	public void test9_1_3() {
		JSONObject testResult = jsonObject.getJSONObject( "9.1.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 70 );
	}

	@Test
	public void test9_1_4() {
		JSONObject testResult = jsonObject.getJSONObject( "9.1.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 375 );
	}

	@Test
	public void test9_1_5() {
		JSONObject testResult = jsonObject.getJSONObject( "9.1.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 750 );
	}

	@Test
	public void test9_1_6() {
		JSONObject testResult = jsonObject.getJSONObject( "9.1.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 1000 );
	}

	@Test
	public void test9_2_1() {
		JSONObject testResult = jsonObject.getJSONObject( "9.2.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}

	@Test
	public void test9_2_2() {
		JSONObject testResult = jsonObject.getJSONObject( "9.2.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 20 );
	}

	@Test
	public void test9_2_3() {
		JSONObject testResult = jsonObject.getJSONObject( "9.2.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 70 );
	}

	@Test
	public void test9_2_4() {
		JSONObject testResult = jsonObject.getJSONObject( "9.2.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 250 );
	}

	@Test
	public void test9_2_5() {
		JSONObject testResult = jsonObject.getJSONObject( "9.2.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 350 );
	}

	@Test
	public void test9_2_6() {
		JSONObject testResult = jsonObject.getJSONObject( "9.2.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 800 );
	}

	@Test
	public void test9_3_1() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 2000 );
	}

	@Test
	public void test9_3_2() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 600 );
	}

	@Test
	public void test9_3_3() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 300 );
	}

	@Test
	public void test9_3_4() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 250 );
	}

	@Test
	public void test9_3_5() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 200 );
	}

	@Test
	public void test9_3_6() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 175 );
	}
	@Test
	public void test9_3_7() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 175 );
	}


	@Test
	public void test9_3_8() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 160 );
	}

	@Test
	public void test9_3_9() {
		JSONObject testResult = jsonObject.getJSONObject( "9.3.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 160 );
	}

	@Test
	public void test9_4_1() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 2300 );
	}

	@Test
	public void test9_4_2() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 700 );
	}

	@Test
	public void test9_4_3() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 350 );
	}

	@Test
	public void test9_4_4() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 175 );
	}

	@Test
	public void test9_4_5() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 150 );
	}

	@Test
	public void test9_4_6() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 100 );
	}

	@Test
	public void test9_4_7() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.7" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 125 );
	}

	@Test
	public void test9_4_8() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.8" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 125 );
	}

	@Test
	public void test9_4_9() {
		JSONObject testResult = jsonObject.getJSONObject( "9.4.9" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 125 );
	}

	@Test
	public void test9_5_1() {
		JSONObject testResult = jsonObject.getJSONObject( "9.5.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 3200 );
	}

	@Test
	public void test9_5_2() {
		JSONObject testResult = jsonObject.getJSONObject( "9.5.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 1300 );
	}

	@Test
	public void test9_5_3() {
		JSONObject testResult = jsonObject.getJSONObject( "9.5.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 700 );
	}

	@Test
	public void test9_5_4() {
		JSONObject testResult = jsonObject.getJSONObject( "9.5.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 450 );
	}

	@Test
	public void test9_5_5() {
		JSONObject testResult = jsonObject.getJSONObject( "9.5.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 250 );
	}

	@Test
	public void test9_5_6() {
		JSONObject testResult = jsonObject.getJSONObject( "9.5.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 150 );
	}

	@Test
	public void test9_6_1() {
		JSONObject testResult = jsonObject.getJSONObject( "9.6.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 3000 );
	}

	@Test
	public void test9_6_2() {
		JSONObject testResult = jsonObject.getJSONObject( "9.6.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 1500 );
	}

	@Test
	public void test9_6_3() {
		JSONObject testResult = jsonObject.getJSONObject( "9.6.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 750 );
	}

	@Test
	public void test9_6_4() {
		JSONObject testResult = jsonObject.getJSONObject( "9.6.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 450 );
	}

	@Test
	public void test9_6_5() {
		JSONObject testResult = jsonObject.getJSONObject( "9.6.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 250 );
	}

	@Test
	public void test9_6_6() {
		JSONObject testResult = jsonObject.getJSONObject( "9.6.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 200 );
	}

	@Test
	public void test9_7_1() {
		JSONObject testResult = jsonObject.getJSONObject( "9.7.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 500 );
	}

	@Test
	public void test9_7_2() {
		JSONObject testResult = jsonObject.getJSONObject( "9.7.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 400 );
	}

	@Test
	public void test9_7_3() {
		JSONObject testResult = jsonObject.getJSONObject( "9.7.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 400 );
	}

	@Test
	public void test9_7_4() {
		JSONObject testResult = jsonObject.getJSONObject( "9.7.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 400 );
	}

	@Test
	public void test9_7_5() {
		JSONObject testResult = jsonObject.getJSONObject( "9.7.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 550 );
	}

	@Test
	public void test9_7_6() {
		JSONObject testResult = jsonObject.getJSONObject( "9.7.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 850 );
	}

	@Test
	public void test9_8_1() {
		JSONObject testResult = jsonObject.getJSONObject( "9.8.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 300 );
	}

	@Test
	public void test9_8_2() {
		JSONObject testResult = jsonObject.getJSONObject( "9.8.2" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 320 );
	}

	@Test
	public void test9_8_3() {
		JSONObject testResult = jsonObject.getJSONObject( "9.8.3" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 400 );
	}

	@Test
	public void test9_8_4() {
		JSONObject testResult = jsonObject.getJSONObject( "9.8.4" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 400 );
	}

	@Test
	public void test9_8_5() {
		JSONObject testResult = jsonObject.getJSONObject( "9.8.5" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 470 );
	}

	@Test
	public void test9_8_6() {
		JSONObject testResult = jsonObject.getJSONObject( "9.8.6" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 770 );
	}

	@Test
	public void test10_1_1() {
		JSONObject testResult = jsonObject.getJSONObject( "10.1.1" );
		assertEquals( "OK", testResult.get( "behavior" ) );
		assertEquals( "OK", testResult.get( "behaviorClose" ) );
		assertTrue( testResult.getInt( "duration" ) < 10 );
	}
}
