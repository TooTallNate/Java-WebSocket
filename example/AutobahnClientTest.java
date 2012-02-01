import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketClient;
import net.tootallnate.websocket.drafts.Draft_17;

public class AutobahnClientTest extends WebSocketClient {

	public AutobahnClientTest( Draft d , URI uri ) {
		super( uri, d );
	}
	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		System.out.println( "Testutility to profile/test this implementation using the Autobahn suit.\n" );
		System.out.println( "Type 'r <casenumber>' to run a testcase. Example: r 1" );
		System.out.println( "Type 'r <first casenumber> <last casenumber>' to run a testcase. Example: r 1 295" );
		System.out.println( "Type 'u' to update the test results." );
		System.out.println( "Type 'ex' to terminate the program." );
		System.out.println( "During sequences of cases the debugoutput will be turned of." );

		System.out.println( "You can now enter in your commands:" );

		try {
			BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );

			/*First of the thinks a programmer might want to change*/
			Draft d = new Draft_17();
			String clientname = "tootallnate/websocket";

			String protocol = "ws";
			String host = "localhost";
			int port = 9001;

			String serverlocation = protocol + "://" + host + ":" + port;
			String line = "";
			AutobahnClientTest e;
			URI uri = null;
			String perviousline = "";
			String nextline = null;
			Integer start = null;
			Integer end = null;

			while ( !line.contains( "ex" ) ) {
				try {
					if( nextline != null ) {
						line = nextline;
						nextline = null;
						WebSocket.DEBUG = false;
					} else {
						System.out.print( ">" );
						line = sysin.readLine();
						WebSocket.DEBUG = true;
					}
					if( line.equals( "l" ) ) {
						line = perviousline;
					}
					String[] spl = line.split( " " );
					if( line.startsWith( "r" ) ) {
						if( spl.length == 3 ) {
							start = new Integer( spl[ 1 ] );
							end = new Integer( spl[ 2 ] );
						}
						if( start != null && end != null ) {
							if( start > end ) {
								start = null;
								end = null;
							} else {
								nextline = "r " + start;
								start++;
								if( spl.length == 3 )
									continue;
							}
						}
						uri = URI.create( serverlocation + "/runCase?case=" + spl[ 1 ] + "&agent=" + clientname );

					} else if( line.startsWith( "u" ) ) {
						uri = URI.create( serverlocation + "/updateReports?agent=" + clientname );
					} else if( line.startsWith( "d" ) ) {
						try {
							d = (Draft) Class.forName( "Draft_" + spl[ 1 ] ).getConstructor().newInstance();
						} catch ( Exception ex ) {
							System.out.println( "Could not change draft" + ex );
						}
					}
					if( uri == null ) {
						System.out.println( "Do not understand the input." );
						continue;
					}
					System.out.println( "//////////////////////Exec: " + uri.getQuery() );
					e = new AutobahnClientTest( d, uri );
					Thread t = new Thread( e );
					t.start();
					try {
						t.join();

					} catch ( InterruptedException e1 ) {
						e1.printStackTrace();
					} finally {
						e.close();
					}
				} catch ( ArrayIndexOutOfBoundsException e1 ) {
					System.out.println( "Bad Input r 1, u 1, d 10, ex" );
				} catch ( IllegalArgumentException e2 ) {
					e2.printStackTrace();
				}

			}
		} catch ( ArrayIndexOutOfBoundsException e ) {
			System.out.println( "Missing server uri" );
		} catch ( IllegalArgumentException e ) {
			e.printStackTrace();
			System.out.println( "URI should look like ws://localhost:8887 or wss://echo.websocket.org" );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		System.exit( 0 );
	}

	@Override
	public void onMessage( String message ) {
		try {
			send( message );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}

	}

	@Override
	public void onMessage( WebSocket conn, byte[] blob ) {
		try {
			conn.send( blob );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError( Exception ex ) {
		System.out.println( "Error: " );
		ex.printStackTrace();
	}

	@Override
	public void onOpen() {
	}

	@Override
	public void onClose() {
	}

}
