/*
 * Copyright (c) 2010-2019 Nathan Rajlich
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

package org.java_websocket.server;

import org.java_websocket.SSLSocketChannel2;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.drafts.Draft;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebSocketFactory that take a SSLEngine as a parameter allow for customization of SSLParameters, Cipher Suites, Supported Protocols, ClientMode, ClientAuth ...
 * @link https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLEngine.html
 */
public class SSLEngineWebSocketServerFactory implements WebSocketServerFactory {

  private final SSLEngine sslEngine;
  protected ExecutorService exec;

  public SSLEngineWebSocketServerFactory(SSLEngine sslEngine) {
    this(sslEngine, Executors.newSingleThreadScheduledExecutor());
  }

  private SSLEngineWebSocketServerFactory(SSLEngine sslEngine, ExecutorService exec) {
    this.sslEngine = sslEngine;
    this.exec = exec;
  }

  @Override
  public WebSocketImpl createWebSocket( WebSocketAdapter a, Draft d) {
    return new WebSocketImpl( a, d );
  }

  @Override
  public WebSocketImpl createWebSocket( WebSocketAdapter a, List<Draft> d) {
    return new WebSocketImpl( a, d );
  }

  @Override
  public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key) throws IOException {
    return new SSLSocketChannel2(channel, sslEngine, exec, key);
  }

  @Override
  public void close() {

  }
}