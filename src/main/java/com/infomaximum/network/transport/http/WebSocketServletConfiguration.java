package com.infomaximum.network.transport.http;

import com.infomaximum.network.protocol.ProtocolUtils;
import com.infomaximum.network.session.UpgradeRequestImpl;
import org.eclipse.jetty.ee10.websocket.server.*;

public class WebSocketServletConfiguration extends JettyWebSocketServlet {

    private final HttpTransport httpTransport;

    public WebSocketServletConfiguration(HttpTransport httpTransport) {
        this.httpTransport = httpTransport;
    }

    @Override
    public void configure(JettyWebSocketServletFactory factory) {
        factory.setMaxTextMessageSize(1 * 1024 * 1024);
        factory.setCreator(new JettyWebSocketCreatorImpl(httpTransport));
    }


    private static class JettyWebSocketCreatorImpl implements JettyWebSocketCreator {

        private final HttpTransport httpTransport;

        public JettyWebSocketCreatorImpl(HttpTransport httpTransport) {
            this.httpTransport = httpTransport;
        }

        @Override
        public Object createWebSocket(JettyServerUpgradeRequest request, JettyServerUpgradeResponse response) {

            //TODO Ulitin V. Need validation protocol
            String nameProtocol = ProtocolUtils.getWebSocketProtocol(request);
            if (nameProtocol != null) {
                //Без этого браузеры на базе chromium - отказываются подтвержать соединение
                //В будущем когда это будет нормально решаться на уровне jetty - это можно просто удалить
                response.addHeader("Sec-WebSocket-Protocol", nameProtocol);
            }

            return new PacketWebSocketHandler(httpTransport, UpgradeRequestImpl.create(request));
        }
    }
}
