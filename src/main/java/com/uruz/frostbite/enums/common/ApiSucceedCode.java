package com.uruz.frostbite.enums.common;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.HttpStatus;

public enum ApiSucceedCode implements ResponseCode{

    SUCCEED("S00000", "SUCCEED", HttpStatus.OK),
    REQUEST_SUCCEED("S00001", "REQUEST_SUCCEED_FURTHER_WORKFLOW_WILL_PROCEED_SOON", HttpStatus.ACCEPTED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ApiSucceedCode(String code, String message, HttpStatus httpStatus){
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
    @Override
    public HttpStatus getHttpStatus(){
        return httpStatus;
    }

}
