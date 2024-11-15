package com.infomaximum.network.struct;

import jakarta.servlet.http.Cookie;

import java.util.Map;

public interface UpgradeRequest {

    public record RemoteAddress(String rawRemoteAddress, String endRemoteAddress) { }

    Map<String, String> getParameters();

    Cookie[] getCookies();

    RemoteAddress getRemoteAddress();
}

