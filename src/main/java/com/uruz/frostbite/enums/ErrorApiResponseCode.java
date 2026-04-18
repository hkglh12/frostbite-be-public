package com.uruz.frostbite.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorApiResponseCode implements ResponseCode{

    UNRECOGNIZED("EU0000", "Unrecognized error occurred"),
    UNSATISFIED("EE0000", "Required parameter not satisfied"),
    NOT_FOUND("EN0001", "Unable to find request target"),
    INTERNAL_ERROR("EI0001", "Unrecognized Internal Error"),

    /** USER */
    USER_EMAIL_OR_PHONE_ALREADY_EXISTS("EN0001", "Email or Phonenumber already exists"),




    /** TEAM */
    ;

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


    @JsonValue
    public String toJson() {
        return message;
    }
}
