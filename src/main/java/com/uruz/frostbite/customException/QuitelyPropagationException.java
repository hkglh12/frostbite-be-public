package com.uruz.frostbite.customException;

import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.enums.common.ResponseCode;
import lombok.Getter;

@Getter
public class QuitelyPropagationException extends RuntimeException{
    private ResponseCode code;
    public QuitelyPropagationException(ResponseCode code, String message){
        super(message);
        this.code = code;
    }
    public QuitelyPropagationException(ResponseCode code){
        super(code.getMessage());
        this.code = code;
    }
    public QuitelyPropagationException(String message){
        super(message);
        this.code = ApiErrorCode.UNRECOGNIZED;
    }
}
