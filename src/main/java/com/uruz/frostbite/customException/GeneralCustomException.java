package com.uruz.frostbite.customException;

import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.enums.common.ResponseCode;
import lombok.Getter;

/** 예측 못한 500 에러에만 사용하도록 하자.*/
@Getter
public class GeneralCustomException extends RuntimeException{
    private ResponseCode code;

    public GeneralCustomException(ResponseCode code, String message){
        super(message);
        this.code = code;
    }
    public GeneralCustomException(ResponseCode code){
        super(code.getMessage());
        this.code = code;
    }
}
