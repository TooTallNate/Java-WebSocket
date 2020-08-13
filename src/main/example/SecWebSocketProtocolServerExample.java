/*
 *  Copyright (c) 2010-2020 Nathan Rajlich
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;

/**
 * This example demonstrates how to use a specific Sec-WebSocket-Protocol for your connection.
 */
public class SecWebSocketProtocolServerExample {

  public static void main(String[] args) throws URISyntaxException {
    // This draft only allows you to use the specific Sec-WebSocket-Protocol without a fallback.
    Draft_6455 draft_ocppOnly = new Draft_6455(Collections.<IExtension>emptyList(),
        Collections.<IProtocol>singletonList(new Protocol("ocpp2.0")));

    // This draft allows the specific Sec-WebSocket-Protocol and also provides a fallback, if the other endpoint does not accept the specific Sec-WebSocket-Protocol
    ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
    protocols.add(new Protocol("ocpp2.0"));
    protocols.add(new Protocol(""));
    Draft_6455 draft_ocppAndFallBack = new Draft_6455(Collections.<IExtension>emptyList(),
        protocols);

    ChatServer chatServer = new ChatServer(8887, draft_ocppOnly);
    chatServer.start();
  }
}
