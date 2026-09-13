package com.uruz.frostbite.aop;

import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleControllerValidationException(MethodArgumentNotValidException MNE){
        FieldError error = MNE.getBindingResult().getFieldError();
        log.info(MNE.getMessage());
        return ResponseEntity.status(ApiErrorCode.BAD_REQUEST.getHttpStatus()).body(new ApiErrorResponse(ApiErrorCode.BAD_REQUEST, "FIELD : " + error.getField() + " didn't satisfy requirements"));
    }

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

    @ExceptionHandler({
            HttpMessageNotReadableException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(GeneralCustomException GE){
        return ResponseEntity.internalServerError()
                .body(new ApiErrorResponse(ApiErrorCode.BAD_REQUEST, GE.getMessage()));
    }

}
