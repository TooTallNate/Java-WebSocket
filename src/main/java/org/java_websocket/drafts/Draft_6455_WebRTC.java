package org.java_websocket.drafts;

import org.java_websocket.handshake.*;

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

    @Override
    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
        super.postProcessHandshakeRequestAsClient(request);
		request.put("origin", request.getFieldValue("host"));
        return request;
    }

    @Override
    public Draft copyInstance() {
        return new Draft_6455_WebRTC();
    }
}