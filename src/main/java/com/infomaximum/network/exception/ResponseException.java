package com.infomaximum.network.exception;

import net.minidev.json.JSONObject;

import java.io.Serializable;

/**
 * Created by kris on 26.08.16.
 */
public class ResponseException extends Exception {

    private final Serializable code;
    private final String comment;
    private final JSONObject date;

    public ResponseException(Serializable code) {
        this(code, (String)null);
    }

    public ResponseException(Serializable code, String comment) {
        super(code.toString());
        this.code = code;
        this.comment = comment;

        this.date = createDate(comment);
    }

    public ResponseException(Serializable code, JSONObject date) {
        super(code.toString());
        this.code = code;
        this.comment = null;

        this.date = date;
    }

    public ResponseException(Serializable code, String comment, Throwable cause) {
        super(code.toString(), cause);
        this.code = code;
        this.comment = comment;

        this.date = createDate(comment);
    }

    public Serializable getCode() {
        return code;
    }

    public String getComment() {
        return comment;
    }

    public JSONObject getDate(){
        return date;
    }

    private static JSONObject createDate(String comment){
        JSONObject date = new JSONObject();
        if (comment!=null && comment.length()>0) date.put("comment", comment);
        return date;
    }
}
