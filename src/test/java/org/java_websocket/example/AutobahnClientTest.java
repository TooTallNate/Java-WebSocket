/*
 * Copyright (c) 2010-2020 Nathan Rajlich
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

package org.java_websocket.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ServerHandshake;

public class AutobahnClientTest extends WebSocketClient {

  public AutobahnClientTest(Draft d, URI uri) {
    super(uri, d);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out
        .println("Testutility to profile/test this implementation using the Autobahn suit.\n");
    System.out.println("Type 'r <casenumber>' to run a testcase. Example: r 1");
    System.out.println(
        "Type 'r <first casenumber> <last casenumber>' to run a testcase. Example: r 1 295");
    System.out.println("Type 'u' to update the test results.");
    System.out.println("Type 'ex' to terminate the program.");
    System.out.println("During sequences of cases the debugoutput will be turned of.");

    System.out.println("You can now enter in your commands:");

    try {
      BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

      /*First of the thinks a programmer might want to change*/
      Draft d = new Draft_6455();
      String clientname = "tootallnate/websocket";

      String protocol = "ws";
      String host = "localhost";
      int port = 9003;

      String serverlocation = protocol + "://" + host + ":" + port;
      String line = "";
      AutobahnClientTest e;
      URI uri = null;
      String perviousline = "";
      String nextline = null;
      Integer start = null;
      Integer end = null;

      while (!line.contains("ex")) {
        try {
          if (nextline != null) {
            line = nextline;
            nextline = null;
          } else {
            System.out.print(">");
            line = sysin.readLine();
          }
          if (line.equals("l")) {
            line = perviousline;
          }
          String[] spl = line.split(" ");
          if (line.startsWith("r")) {
            if (spl.length == 3) {
              start = new Integer(spl[1]);
              end = new Integer(spl[2]);
            }
            if (start != null && end != null) {
              if (start > end) {
                start = null;
                end = null;
              } else {
                nextline = "r " + start;
                start++;
                if (spl.length == 3) {
                  continue;
                }
              }
            }
            uri = URI.create(serverlocation + "/runCase?case=" + spl[1] + "&agent=" + clientname);

          } else if (line.startsWith("u")) {
            uri = URI.create(serverlocation + "/updateReports?agent=" + clientname);
          } else if (line.startsWith("d")) {
            try {
              d = (Draft) Class.forName("Draft_" + spl[1]).getConstructor().newInstance();
            } catch (Exception ex) {
              System.out.println("Could not change draft" + ex);
            }
          }
          if (uri == null) {
            System.out.println("Do not understand the input.");
            continue;
          }
          System.out.println("//////////////////////Exec: " + uri.getQuery());
          e = new AutobahnClientTest(d, uri);
          Thread t = new Thread(e);
          t.start();
          try {
            t.join();

          } catch (InterruptedException e1) {
            e1.printStackTrace();
          } finally {
            e.close();
          }
        } catch (ArrayIndexOutOfBoundsException e1) {
          System.out.println("Bad Input r 1, u 1, d 10, ex");
        } catch (IllegalArgumentException e2) {
          e2.printStackTrace();
        }

      }
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Missing server uri");
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      System.out.println("URI should look like ws://localhost:8887 or wss://echo.websocket.org");
    } catch (IOException e) {
      e.printStackTrace(); // for System.in reader
    }
    System.exit(0);
  }

  @Override
  public void onMessage(String message) {
    send(message);
  }

  @Override
  public void onMessage(ByteBuffer blob) {
    getConnection().send(blob);
  }

  @Override
  public void onError(Exception ex) {
    System.out.println("Error: ");
    ex.printStackTrace();
  }

  @Override
  public void onOpen(ServerHandshake handshake) {
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    System.out.println("Closed: " + code + " " + reason);
  }

}
