package com.uruz.frostbite.customException;

import com.uruz.frostbite.enums.common.ResponseCode;
import lombok.Getter;

/** FE로 보내줘야 할 "의미있는 오류값" 을 처리하기 위한 Exception */
@Getter
public class RequestProcessingException extends RuntimeException{
    private ResponseCode code;

    public RequestProcessingException(ResponseCode code){
        super(code.getMessage());
        this.code = code;
    }
}
