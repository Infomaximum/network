package com.infomaximum.network.transport.http;

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

    @OnWebSocketConnect
    public void afterConnectionEstablished(Session session) throws Exception {

        //Определяеи ip, проверяя загаловки возможно балансировщик добавит данные с реальным ip
        String remoteIpAddress = null;
        if (session.getUpgradeRequest().getHeaders()!=null) remoteIpAddress = session.getUpgradeRequest().getHeader("X-Real-IP");
        if (remoteIpAddress == null) remoteIpAddress = session.getRemoteAddress().getAddress().getHostAddress();

        HttpTransport.instance.fireConnect(session, remoteIpAddress);//Оповещаем о новом подключении
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message){
        HttpTransport.instance.fireIncomingMessage(session, message);
    }

    @OnWebSocketError
    public void handleTransportError(Session session, Throwable throwable) throws Exception {
        session.close();
        HttpTransport.instance.fireDisconnect(session, -1, throwable);//Соединение разрывается-оповещаем
    }

    @OnWebSocketClose
    public void afterConnectionClosed(Session session, int statusCode, String reason) throws Exception {
        HttpTransport.instance.fireDisconnect(session, statusCode, null);//Соединение разрывается-оповещаем
    }
}

