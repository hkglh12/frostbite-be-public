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
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper;

    public CustomUsernamePasswordAuthenticationFilter(@Autowired  ObjectMapper mapper, @Autowired AuthenticationManager authenticationManager,
        SecurityContextRepository repository){
        super(authenticationManager);
        this.setSecurityContextRepository(repository);
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

        // authenticate() 호출 후 -> session setCookie 값들이 보이지 않음. 이 위치에서 debug evaluate로 어떤 authenticationManager가 호출되었는지 확인 가능
        // this.getAuthenticationManager()

        //89be998c02da1022bd69d1cc6cafbfc77ce5a44c 시점에서, 로그인은 했으나 로그인 성공에 따른 세션 쿠키 설정이 보이지 않았음.
        // 실제로, UsernamePasswordAuthenticationFilter -> AbstractAuthenticationProcessingFilter 에는
        // private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();
        // 로, session 정보를 HTTP에 저장 안하는 repository 정책이 default로 들어가있음.
        // session에 저장하려면 HttpSessionSecurityContextRepository 가 들어가야 함.

        // REF : https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html?utm_source=chatgpt.com#moving-away-from-sessionmanagementfilter
        // debugPoint -> securityContextConfigurer
        return this.getAuthenticationManager().authenticate(authRequest);
    }


}
