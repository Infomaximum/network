package com.infomaximum.network.protocol;

import org.eclipse.jetty.websocket.common.WebSocketSession;

public class ProtocolUtils {

    public static String getWebSocketProtocol(WebSocketSession webSocketSession) {
        String protocol = webSocketSession.getUpgradeRequest().getHeader("Sec-WebSocket-Protocol");
        return protocol;
    }
}
