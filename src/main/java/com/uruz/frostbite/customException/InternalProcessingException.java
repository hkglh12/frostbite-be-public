package com.uruz.frostbite.customException;

import lombok.Getter;

/** 외부로 내보내지 않을, 서버 내부에서 소비할 Exception 관리용도*/
@Getter
public class InternalProcessingException extends RuntimeException{
    private String message;

    public InternalProcessingException(String message){
        super(message);
    }
}
