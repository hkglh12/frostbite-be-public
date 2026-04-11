package com.uruz.frostbite.enums;

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
}
