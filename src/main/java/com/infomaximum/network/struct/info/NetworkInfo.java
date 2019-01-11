package com.infomaximum.network.struct.info;

import com.infomaximum.network.transport.Transport;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkInfo {

    private final List<TransportInfo> transportsInfo;

    public NetworkInfo(List<Transport> transportsInfo) {
        this.transportsInfo = transportsInfo.stream().map(Transport::getInfo).collect(Collectors.toList());
    }

    public List<TransportInfo> getTransportsInfo() {
        return Collections.unmodifiableList(transportsInfo);
    }

    @Override
    public String toString() {
        return "Network{" +
                "transports=" + transportsInfo +
                '}';
    }
}
