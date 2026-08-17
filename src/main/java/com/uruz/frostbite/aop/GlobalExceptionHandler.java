package com.uruz.frostbite.aop;

import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {



    @ExceptionHandler({
            RequestProcessingException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleProcessingException(RequestProcessingException CPE){
        HttpStatus exceptionStatus = CPE.getCode().getHttpStatus();

        return ResponseEntity.status(exceptionStatus.value()).body(new ApiErrorResponse(CPE.getCode(), CPE.getMessage()));
    }

    @ExceptionHandler({
            GeneralCustomException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleCustomException(GeneralCustomException GE){
        return ResponseEntity.internalServerError()
                .body(new ApiErrorResponse(ApiErrorCode.UNRECOGNIZED, GE.getMessage()));
    }

}
