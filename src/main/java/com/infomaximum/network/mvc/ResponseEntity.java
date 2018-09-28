package com.infomaximum.network.mvc;

import net.minidev.json.JSONObject;

public class ResponseEntity {

    public static final int RESPONSE_CODE_OK = 200;
    public static final int RESPONSE_CODE_ERROR = 500;

    public final int code;
    public final JSONObject data;

    private ResponseEntity(int code, JSONObject data) {
        this.code = code;
        this.data = data;
    }

    public static ResponseEntity success(JSONObject data) {
        return new ResponseEntity(RESPONSE_CODE_OK, data);
    }

    public static ResponseEntity error(JSONObject data) {
        return new ResponseEntity(RESPONSE_CODE_ERROR, data);
    }
}
