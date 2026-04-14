package com.uruz.frostbite.customException;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.ErrorApiResponseCode;
import com.uruz.frostbite.enums.ResponseCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class GeneralCustomException extends RuntimeException{
    private ResponseCode code;
    public GeneralCustomException(ResponseCode code, String message){
        super(message);
        this.code = code;
    }
    public GeneralCustomException(String message){
        super(message);
        this.code = ErrorApiResponseCode.UNRECOGNIZED;
    }
}
