package com.infomaximum.network.session;

import com.infomaximum.network.struct.UpgradeRequest;
import jakarta.servlet.http.Cookie;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeRequest;

import java.net.HttpCookie;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class UpgradeRequestImpl implements UpgradeRequest {

    private final RemoteAddress remoteAddress;
    private final Map<String, String> parameters;
    private final Cookie[] cookies;

    public UpgradeRequestImpl(RemoteAddress remoteAddress, Map<String, String> parameters, Cookie[] cookies) {
        this.remoteAddress = remoteAddress;
        this.parameters = Collections.unmodifiableMap(parameters);
        this.cookies = cookies;
    }

    @Override
    public RemoteAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }


    public static UpgradeRequestImpl create(JettyServerUpgradeRequest request) {

        Map<String, String> parameters = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : request.getParameterMap().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue().get(0));
        }

        Cookie[] cookies;
        List<HttpCookie> requestCookiesCookies = request.getCookies();
        if (requestCookiesCookies == null) {
            cookies = new Cookie[0];
        } else {
            cookies = requestCookiesCookies.stream()
                    .map(httpCookie -> new Cookie(httpCookie.getName(), httpCookie.getValue()))
                    .toArray(Cookie[]::new);
        }

        String rawRemoteAddress = request.getRemoteSocketAddress().toString();
        String endRemoteAddress = request.getHeader("X-Real-IP");
        if (endRemoteAddress == null) {
            endRemoteAddress = rawRemoteAddress;
        }
        RemoteAddress remoteAddress = new RemoteAddress(rawRemoteAddress, endRemoteAddress);

        return new UpgradeRequestImpl(remoteAddress, parameters, cookies);
    }
}
