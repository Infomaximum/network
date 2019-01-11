package com.infomaximum.network;

import com.infomaximum.network.event.NetworkListener;
import com.infomaximum.network.struct.info.NetworkInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Ulitin
 * Date: 01.02.12
 * Time: 21:00
 */
public interface Network extends AutoCloseable {

    public void addNetworkListener(NetworkListener listener);

    public void removeNetworkListener(NetworkListener listener);

    public NetworkInfo getInfo();
}
