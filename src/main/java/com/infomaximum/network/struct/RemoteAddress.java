package com.infomaximum.network.struct;

public class RemoteAddress {

    private String rawRemoteAddress;
    private String endRemoteAddress;

    public RemoteAddress(String rawRemoteAddress, String endRemoteAddress) {
        this.rawRemoteAddress = rawRemoteAddress;
        this.endRemoteAddress = endRemoteAddress;
    }

    public String getRawRemoteAddress() {
        return rawRemoteAddress;
    }

    public String getEndRemoteAddress() {
        return endRemoteAddress;
    }
}
