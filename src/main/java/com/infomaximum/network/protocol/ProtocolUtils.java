package com.infomaximum.network.protocol;

import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.common.WebSocketSession;

public class ProtocolUtils {

    public static String getWebSocketProtocol(WebSocketSession webSocketSession) {
        return getWebSocketProtocol(webSocketSession.getUpgradeRequest());
    }

    public static String getWebSocketProtocol(UpgradeRequest upgradeRequest) {
        String protocol = upgradeRequest.getHeader("Sec-WebSocket-Protocol");
        return protocol;
    }
}
