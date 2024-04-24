package com.microservices.util.http;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum ErrorCode {
    NOT_FOUND(404, "Not found"),
    INVALID_INPUT(444, "invalid input"),
    DUPLICATE_KEY_DB(4444, "Key value database iss duplicated"),
    STREAM_ERROR(434, "consume a message is error")
    ;


    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    private int code;
    private String message;
}
