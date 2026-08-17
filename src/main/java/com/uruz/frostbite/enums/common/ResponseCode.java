package com.uruz.frostbite.enums.common;

import org.springframework.http.HttpStatus;

public interface ResponseCode {
    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
