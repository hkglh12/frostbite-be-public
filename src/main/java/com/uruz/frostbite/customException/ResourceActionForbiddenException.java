package com.uruz.frostbite.customException;

import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.enums.common.ResponseCode;

public class ResourceActionForbiddenException extends RuntimeException{
    private ResponseCode code;
    public ResourceActionForbiddenException(ResponseCode code, String message){
        super(message);
        this.code = code;
    }
    public ResourceActionForbiddenException(ResponseCode code){
        super(code.getMessage());
        this.code = code;
    }
    public ResourceActionForbiddenException(String message){
        super(message);
        this.code = ApiErrorCode.UNRECOGNIZED;
    }
}
