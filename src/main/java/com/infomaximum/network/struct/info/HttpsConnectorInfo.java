package com.infomaximum.network.struct.info;


import java.util.Arrays;

public class HttpsConnectorInfo extends HttpConnectorInfo{

    private final String[] selectedProtocols;
    private final String[] selectedCipherSuites;

    public HttpsConnectorInfo(String host, int port, String[] selectedProtocols, String[] selectedCipherSuites) {
        super(host, port);
        this.selectedProtocols = selectedProtocols;
        this.selectedCipherSuites = selectedCipherSuites;
    }

    public String[] getSelectedProtocols() {
        return selectedProtocols;
    }

    public String[] getSelectedCipherSuites() {
        return selectedCipherSuites;
    }

    public boolean containsSelectedProtocol(String protocol) {
        return Arrays.asList(selectedProtocols).contains(protocol);
    }

    public boolean containsSelectedCipherSuite(String cipherSuite) {
        return Arrays.asList(selectedCipherSuites).contains(cipherSuite);
    }

    @Override
    public String toString() {
        return "HttpsConnectorInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                "selectedProtocols=" + Arrays.toString(selectedProtocols) +
                ", selectedCipherSuites=" + Arrays.toString(selectedCipherSuites) +
                '}';
    }
}
