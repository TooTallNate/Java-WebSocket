package org.java_websocket.drafts;

import org.java_websocket.handshake.*;

/**
 * Description:
 * Author:totoroYang 2017/8/7.
 */

public class Draft_6455_with_host extends Draft_6455 {

    @Override
    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
        super.postProcessHandshakeRequestAsClient(request);
		request.put("origin", request.getFieldValue("host"));
        return request;
    }

    @Override
    public Draft copyInstance() {
        return new Draft_6455_with_host();
    }
}