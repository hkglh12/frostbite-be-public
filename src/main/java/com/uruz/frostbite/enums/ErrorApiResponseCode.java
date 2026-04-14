package com.uruz.frostbite.enums;

public enum ErrorApiResponseCode implements ResponseCode{

    UNRECOGNIZED("EU0000", "Unrecognized error occurred"),
    UNSATISFIED("EE0000", "Required parameter not satisfied"),
    NOT_FOUND("EN0001", "Unable to find request target");

    private final String code;
    private final String message;

    ErrorApiResponseCode(String code, String message){
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
