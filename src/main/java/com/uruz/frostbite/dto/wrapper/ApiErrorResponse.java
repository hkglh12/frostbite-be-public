package com.uruz.frostbite.dto.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.ErrorApiResponseCode;
import com.uruz.frostbite.enums.ResponseCode;
import com.uruz.frostbite.enums.SucceedApiResponseCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiErrorResponse<T> {
    @Schema(description = "실패시 응답 메시지 (내부용)")
    private String message;
    @Schema(description = "실패시 코드 메시지 (출력용)")
    private String codeMessage;
    @Schema(description = "실패 응답 코드")
    private ResponseCode code;
    @Schema(description = "실패 응답 발행 시간")
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
