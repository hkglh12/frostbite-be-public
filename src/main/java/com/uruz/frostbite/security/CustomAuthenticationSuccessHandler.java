package com.uruz.frostbite.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper mapper;

    private final CookieCsrfTokenRepository cookieCsrfTokenRepository;


    public CustomAuthenticationSuccessHandler(@Autowired ObjectMapper mapper, @Autowired CookieCsrfTokenRepository cookieCsrfTokenRepository, CsrfTokenRepository csrfTokenRepository){
        this.mapper = mapper;
        this.cookieCsrfTokenRepository = cookieCsrfTokenRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomUserDetails inSecurityCtx =  ((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        ApiSucceedResponse<SecurityContextUserContext> entity =
                new ApiSucceedResponse<>(new SecurityContextUserContext(
                       inSecurityCtx.getEmail(), inSecurityCtx.getUuid()
                ));

        CsrfToken csrfToken = cookieCsrfTokenRepository.generateToken(request);
        cookieCsrfTokenRepository.saveToken(csrfToken, request, response);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(mapper.writeValueAsString(entity));

    }
}
