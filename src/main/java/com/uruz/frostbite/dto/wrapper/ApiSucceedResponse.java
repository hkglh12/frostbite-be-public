package com.uruz.frostbite.dto.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.ResponseCode;
import com.uruz.frostbite.enums.SucceedApiResponseCode;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class ApiSucceedResponse<T>{
    private T result;
    private String message;
    private ResponseCode code;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ApiSucceedResponse(T result){
        this.result = result;
        this.code = SucceedApiResponseCode.SUCCEED;
        this.message = this.code.getMessage();
        this.timestamp = LocalDateTime.now();
    }
    public ApiSucceedResponse(T result, ResponseCode code){
        this.result = result;
        this.code = code;
        this.message = this.code.getMessage();
        this.timestamp = LocalDateTime.now();
    }
}
