package com.infomaximum.network.struct.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransportInfo {

    private final List<HttpConnectorInfo> connectorsInfo;

    public TransportInfo() {
        this.connectorsInfo = new ArrayList<>();
    }

    public void addConnectorInfo(HttpConnectorInfo httpConnectorInfo) {
        connectorsInfo.add(httpConnectorInfo);
    }

    public List<? extends HttpConnectorInfo> getConnectorsInfo() {
        return Collections.unmodifiableList(connectorsInfo);
    }

    @Override
    public String toString() {
        return "Transport{" +
                "connectors=" + connectorsInfo +
                '}';
    }
}
