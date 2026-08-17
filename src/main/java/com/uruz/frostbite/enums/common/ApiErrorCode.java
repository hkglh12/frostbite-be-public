package com.uruz.frostbite.enums.common;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode implements ResponseCode{

    /** FE 파라미터 문제*/
    // GENERAL
    UNSATISFIED("EFP001", "Required parameter not satisfied", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("EFP002", "Request parameter is not valid", HttpStatus.BAD_REQUEST),
    UNIQUE_VIOLATED("EFP003", "Requestd paramter violated unique constraint", HttpStatus.BAD_REQUEST),

    // USER
    USER_EMAIL_ALREADY_EXISTS("EFP003", "User email already exists", HttpStatus.CONFLICT),
    USER_PHONE_NUMBER_ALREADY_EXISTS("EFP004", "User cellphone already exists", HttpStatus.CONFLICT),
    USER_PASSWORD_NOT_MATCH("EFP005", "Inserted Password does not match with origin", HttpStatus.BAD_REQUEST),

    /** 공통 */
    // 자원
    NOT_FOUND("EIR001", "Unable to find request target", HttpStatus.NOT_FOUND),
    INTERNAL_ERROR_FOR_JSON_PARSE("EIR002", "Submitted JSON is unable to parse", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR_FOR_RESOURCE_HANDLE("EIR003", "Server failed to HANDLE resources", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_DATA_INCONSISTENCY_FOLDER_RESOURCE("ESD001", "Folder resource should be exists, but failed to found", HttpStatus.INTERNAL_SERVER_ERROR),

    /** 권한 */
    // 요청 속행 불능
    ACCESS_DENIED("EIA001", "Not enough role or authority", HttpStatus.FORBIDDEN),
    RESOURCE_SCOPE_DENIED("EIA002", "Resource scope is not match with request information", HttpStatus.BAD_REQUEST),
    ACTION_DENIED("EIA003", "Requested action is not allowed to target", HttpStatus.FORBIDDEN),

    // 명시적 거절
    RESOURCE_EXPLICITLY_DENIED("EID001", "Request tried to access explicitly denied resource", HttpStatus.FORBIDDEN),


    /** 서버측 자원관리*/
    INTERNAL_DATA_INCONSISTENCY_TEMP_PATH("ESD001", "Temp File path is null", HttpStatus.INTERNAL_SERVER_ERROR),


    /** 예측 외 오류 */
    UNRECOGNIZED("000000", "Unrecognized error occuered", HttpStatus.INTERNAL_SERVER_ERROR),

    AUTHENTICATION_FAILED("EXTERNAL001", "Authentication failed", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_REQUIRED("EXTERNAL002", "Authorization required", HttpStatus.UNAUTHORIZED);
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ApiErrorCode(String code, String message, HttpStatus httpStatus){
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
    @Override
    public HttpStatus getHttpStatus(){
        return httpStatus;
    }

}
