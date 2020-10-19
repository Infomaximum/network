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

    void addNetworkListener(NetworkListener listener);

    void removeNetworkListener(NetworkListener listener);

    NetworkInfo getInfo();

    @Override
    void close();
}
