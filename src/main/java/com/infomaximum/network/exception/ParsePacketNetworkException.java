package com.infomaximum.network.exception;

public class ParsePacketNetworkException extends NetworkException {

    public ParsePacketNetworkException(String message) {
        super(message);
    }

    public ParsePacketNetworkException(Throwable cause) {
        super(cause);
    }

    public ParsePacketNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
