package com.infomaximum.network.exception;

import net.minidev.json.JSONObject;

import java.io.Serializable;

/**
 * Created by kris on 01.09.16.
 */
public class HandshakeException extends ResponseException {

    public HandshakeException(Serializable code) {
        super(code);
    }

    public HandshakeException(Serializable code, String comment) {
        super(code, comment);
    }

    public HandshakeException(Serializable code, JSONObject date) {
        super(code, date);
    }
}
