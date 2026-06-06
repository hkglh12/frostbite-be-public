package com.uruz.frostbite.aop;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.customException.ResourceAccessDeniedException;
import com.uruz.frostbite.customException.ResourceActionForbiddenException;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleArguments(Exception IAE){
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse(ApiErrorCode.UNSATISFIED, IAE.getMessage()));
    }
    @ExceptionHandler({
            UsernameNotFoundException.class
    })
    protected  ResponseEntity<ApiErrorResponse> handleUserNotFound(Exception UNF){
        return ResponseEntity.badRequest().body(new ApiErrorResponse(ApiErrorCode.NOT_FOUND, UNF.getMessage()));
    }

    @ExceptionHandler({
            GeneralCustomException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleCustomException(GeneralCustomException GE){
        return ResponseEntity.internalServerError()
                .body(new ApiErrorResponse(GE.getCode(), GE.getMessage()));
    }
    @ExceptionHandler({
            IllegalStateException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException GE){
        return ResponseEntity.internalServerError()
                .body(new ApiErrorResponse(ApiErrorCode.INTERNAL_ERROR, GE.getMessage()));
    }

    @ExceptionHandler({
            ResourceAccessDeniedException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleRestrictedAccess(ResourceAccessDeniedException GE){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(ApiErrorCode.ACCESS_DENIED, GE.getMessage()));
    }

    @ExceptionHandler({
            ResourceActionForbiddenException.class
    })
    protected ResponseEntity<ApiErrorResponse> handleForbiddenAction(ResourceActionForbiddenException GE){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(ApiErrorCode.ACCESS_DENIED, GE.getMessage()));
    }

    @ExceptionHandler({
            Exception.class
    })
    protected ResponseEntity<ApiErrorResponse> handleGeneral(Exception E,
                                                             HttpServletRequest request){
        log.error(request.getRequestURL().toString());
        E.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(new ApiErrorResponse(ApiErrorCode.UNRECOGNIZED, E.getMessage()));
    }

}
