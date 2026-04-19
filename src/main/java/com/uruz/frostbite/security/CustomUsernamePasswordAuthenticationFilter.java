package com.uruz.frostbite.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper;

    public CustomUsernamePasswordAuthenticationFilter(@Autowired  ObjectMapper mapper, @Autowired AuthenticationManager authenticationManager){
        super(authenticationManager);
        this.mapper = mapper;
    }

    private boolean postOnly = true;
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        Map<String, String> requestParamMap = null;
        try {
            requestParamMap = mapper.readValue(request.getInputStream(), Map.class);
        }catch(IOException IOE){
            log.error(IOE.getMessage());
            throw new AuthenticationServiceException("JSON PARSE ERROR");
        }
        String username = MapUtils.getString(requestParamMap, "email", "");
        username = (username != null) ? username.trim() : "";
        String password = MapUtils.getString(requestParamMap, "password", "");
        password = (password != null) ? password : "";
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username,
                password);
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }


}
