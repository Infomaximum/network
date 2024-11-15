package com.infomaximum.network.transport;

import com.infomaximum.network.struct.UpgradeRequest;

/**
 * Created by kris on 26.08.16.
 */
public interface TransportListener {

    public void onConnect(Transport transport, Object channel, UpgradeRequest upgradeRequest);

    public void incomingMessage(Transport transport, Object channel, String message);

    public void onDisconnect(Transport transport, Object channel, int statusCode, Throwable throwable);
}
