package com.uruz.frostbite.dto.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.ResponseCode;
import com.uruz.frostbite.enums.SucceedApiResponseCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class ApiSucceedResponse<T>{
    @Schema(description = "응답 결과 객체")
    private T result;
    @Schema(description = "부가 메시지")
    private String message;
    @Schema(description = "응답 결과 코드")
    private ResponseCode code;
    @Schema(description = "응답 발행 시간")
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
