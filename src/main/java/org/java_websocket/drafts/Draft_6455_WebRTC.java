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

package org.java_websocket.drafts;

import org.java_websocket.extensions.IExtension;
import org.java_websocket.handshake.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 * Base on Draft_6455
 * You can use this Draft to connect the WebRTC Candidate server(WebSocket)
 * The official Candidate server run on go programming language
 * I found The ws/wss HandShake request must have origin param
 * If not it will return http code 403
 * If you get http code 403 and you ws/wss server run on go, you can try this Draft
 * Author:totoroYang 2017/8/8.
 */

public class Draft_6455_WebRTC extends Draft_6455 {

    /**
     * Constructor for the websocket protocol specified by RFC 6455 with default extensions for the WebRTC Candidate server(WebSocket)
     */
    public Draft_6455_WebRTC() {
        this( Collections.<IExtension>emptyList() );
    }

    /**
     * Constructor for the websocket protocol specified by RFC 6455 with custom extensions for the WebRTC Candidate server(WebSocket)
     *
     * @param inputExtension the extension which should be used for this draft
     */
    public Draft_6455_WebRTC( IExtension inputExtension ) {
        this( Collections.singletonList( inputExtension ) );
    }

    /**
     * Constructor for the websocket protocol specified by RFC 6455 with custom extensions for the WebRTC Candidate server(WebSocket)
     *
     * @param inputExtensions the extensions which should be used for this draft
     */
    public Draft_6455_WebRTC( List<IExtension> inputExtensions ) {
        super(inputExtensions);
    }

    @Override
    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
        super.postProcessHandshakeRequestAsClient(request);
		request.put("origin", request.getFieldValue("host"));
        return request;
    }

    @Override
    public Draft copyInstance() {
        ArrayList<IExtension> newExtensions = new ArrayList<IExtension>();
        for( IExtension extension : knownExtensions ) {
            newExtensions.add( extension.copyInstance() );
        }
        return new Draft_6455_WebRTC( newExtensions );
    }
}