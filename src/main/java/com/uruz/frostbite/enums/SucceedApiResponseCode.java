package com.uruz.frostbite.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SucceedApiResponseCode implements ResponseCode{

    SUCCEED("S00000", "SUCCEED");

    private final String code;
    private final String message;

    SucceedApiResponseCode(String code, String message){
        this.code = code;
        this.message = message;
    }
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }


    @JsonValue
    public String toJson() {
        return message;
    }
}
