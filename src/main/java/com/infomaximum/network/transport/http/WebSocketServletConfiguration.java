package com.infomaximum.network.transport.http;

import com.infomaximum.network.protocol.ProtocolUtils;
import org.eclipse.jetty.websocket.server.*;

/**
 * Created by kris on 08.04.17.
 */
public class WebSocketServletConfiguration extends JettyWebSocketServlet {

    @Override
    public void configure(JettyWebSocketServletFactory factory) {
        factory.setMaxTextMessageSize(1 * 1024 * 1024);

        //Мы переопределяем Creator, только для того, что бы добавить header: Sec-WebSocket-Protocol
        //Без этого браузеры на базе chromium - отказываются подтвержать соединение
        //В будущем когда это будет нормально решаться на уровне jetty - этот кусок кода можно просто удалить
        factory.setCreator(new JettyWebSocketCreator() {
            @Override
            public Object createWebSocket(JettyServerUpgradeRequest req, JettyServerUpgradeResponse resp) {
                //TODO Ulitin V. Need validation protocol
                String nameProtocol = ProtocolUtils.getWebSocketProtocol(req);
                if (nameProtocol != null) {
                    resp.addHeader("Sec-WebSocket-Protocol", nameProtocol);
                }
                return new PacketWebSocketHandler();
            }
        });

        factory.register(PacketWebSocketHandler.class);
    }
}
