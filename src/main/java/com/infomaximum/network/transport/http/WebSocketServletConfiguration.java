package com.infomaximum.network.transport.http;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Created by kris on 08.04.17.
 */
public class WebSocketServletConfiguration extends WebSocketServlet {

    @Override
    public void configure( WebSocketServletFactory factory ){
        factory.getPolicy().setMaxTextMessageSize(1 * 1024 * 1024);

        factory.register(PacketWebSocketHandler.class);
    }
}
