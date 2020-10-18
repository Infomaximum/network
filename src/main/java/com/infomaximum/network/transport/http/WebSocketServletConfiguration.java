package com.infomaximum.network.transport.http;

import com.infomaximum.network.protocol.ProtocolUtils;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.eclipse.jetty.websocket.servlet.*;

/**
 * Created by kris on 08.04.17.
 */
public class WebSocketServletConfiguration extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setMaxTextMessageSize(1 * 1024 * 1024);

        //Мы переопределяем Creator, только для того, что бы добавить header: Sec-WebSocket-Protocol
        //Без этого браузеры на базе chromium - отказываются подтвержать соединение
        //В будущем когда это будет нормально решаться на уровне jetty - этот кусок кода можно просто удалить
        factory.setCreator(new WebSocketCreator() {
            @Override
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {

                //TODO Ulitin V. Need validation protocol
                String nameProtocol = ProtocolUtils.getWebSocketProtocol(req);
                if (nameProtocol != null) {
                    resp.addHeader("Sec-WebSocket-Protocol", nameProtocol);
                }

                return ((WebSocketServerFactory) factory).createWebSocket(req, resp);
            }
        });

        factory.register(PacketWebSocketHandler.class);
    }
}
