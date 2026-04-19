package com.uruz.frostbite.enums.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApiErrorCode implements ResponseCode{

    UNRECOGNIZED("EU0000", "Unrecognized error occurred"),
    UNSATISFIED("EE0000", "Required parameter not satisfied"),
    NOT_FOUND("EN0001", "Unable to find request target"),
    INTERNAL_ERROR("EI0001", "Unrecognized Internal Error"),


    ACCESS_DENIED("EG0001", "Not enough role authority"),
    AUTHENTICATION_REQUIRED("EG0002", "Proceed login first"),
    AUTHENTICATION_FAILED("EG0003", "Authentication failed"),
    /** USER */
    USER_EMAIL_OR_PHONE_ALREADY_EXISTS("EN0001", "Email or Phonenumber already exists");




    /** TEAM */


    private final String code;
    private final String message;

    ApiErrorCode(String code, String message){
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
