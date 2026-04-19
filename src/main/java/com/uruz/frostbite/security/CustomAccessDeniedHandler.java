package com.uruz.frostbite.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;

    public CustomAccessDeniedHandler(@Autowired ObjectMapper mapper){
        this.mapper = mapper;
    }
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error(accessDeniedException.getMessage());
        ResponseEntity<ApiErrorResponse> entity = ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiErrorResponse(ApiErrorCode.ACCESS_DENIED, ApiErrorCode.ACCESS_DENIED.getMessage())
        );
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(mapper.writeValueAsString(entity));

    }
}
