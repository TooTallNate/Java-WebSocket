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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Simple example to reconnect blocking and non-blocking.
 */
public class ReconnectClientExample {

  public static void main(String[] args) throws URISyntaxException, InterruptedException {
    ExampleClient c = new ExampleClient(new URI("ws://localhost:8887"));
    //Connect to a server normally
    c.connectBlocking();
    c.send("hi");
    Thread.sleep(10);
    c.closeBlocking();
    //Disconnect manually and reconnect blocking
    c.reconnectBlocking();
    c.send("it's");
    Thread.sleep(10000);
    c.closeBlocking();
    //Disconnect manually and reconnect
    c.reconnect();
    Thread.sleep(100);
    c.send("me");
    Thread.sleep(100);
    c.closeBlocking();
  }
}
