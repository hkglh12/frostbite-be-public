package com.uruz.frostbite.dto.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.ErrorApiResponseCode;
import com.uruz.frostbite.enums.ResponseCode;
import com.uruz.frostbite.enums.SucceedApiResponseCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiErrorResponse<T> {
    private String message;
    private String codeMessage;
    private ResponseCode code;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ApiErrorResponse(String message){
        this.code = ErrorApiResponseCode.UNRECOGNIZED;
        this.codeMessage = this.code.getMessage();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    public ApiErrorResponse(ResponseCode code, String message){
        this.code = code;
        this.codeMessage = this.code.getMessage();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
