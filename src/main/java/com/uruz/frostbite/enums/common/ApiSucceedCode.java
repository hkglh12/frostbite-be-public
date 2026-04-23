package com.uruz.frostbite.enums.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApiSucceedCode implements ResponseCode{

    SUCCEED("S00000", "SUCCEED");

    private final String code;
    private final String message;

    ApiSucceedCode(String code, String message){
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

}
