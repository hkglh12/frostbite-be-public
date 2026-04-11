package com.uruz.frostbite.dto.wrapper;

import com.uruz.frostbite.enums.ResponseCode;
import com.uruz.frostbite.enums.SucceedApiResponseCode;
import lombok.Getter;

@Getter
public class ApiResponse <T>{
    private T result;
    private String message;
    private ResponseCode code;

    public ApiResponse(T result){
        this.result = result;
        this.code = SucceedApiResponseCode.SUCCEED;
        this.message = this.code.getMessage();
    }
    public ApiResponse(T result, ResponseCode code){
        this.result = result;
        this.code = code;
        this.message = this.code.getMessage();
    }
}
