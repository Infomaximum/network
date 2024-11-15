package com.infomaximum.network.transport.http;

import com.infomaximum.network.struct.UpgradeRequest;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kris on 08.04.17.
 */
@WebSocket
public class PacketWebSocketHandler {

    private final static Logger log = LoggerFactory.getLogger(PacketWebSocketHandler.class);

    private final HttpTransport httpTransport;
    private final UpgradeRequest upgradeRequest;

    public PacketWebSocketHandler(HttpTransport httpTransport, UpgradeRequest upgradeRequest) {
        this.httpTransport = httpTransport;
        this.upgradeRequest = upgradeRequest;
    }

    @OnWebSocketOpen
    public void afterConnectionEstablished(Session session) throws Exception {
        httpTransport.fireConnect(session, upgradeRequest);//Оповещаем о новом подключении
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        httpTransport.fireIncomingMessage(session, message);
    }

    @OnWebSocketError
    public void handleTransportError(Session session, Throwable throwable) throws Exception {
        session.close();
        httpTransport.fireDisconnect(session, -1, throwable);//Соединение разрывается-оповещаем
    }

    @OnWebSocketClose
    public void afterConnectionClosed(Session session, int statusCode, String reason) throws Exception {
        httpTransport.fireDisconnect(session, statusCode, null);//Соединение разрывается-оповещаем
    }

    public UpgradeRequest getUpgradeRequest() {
        return upgradeRequest;
    }
}

