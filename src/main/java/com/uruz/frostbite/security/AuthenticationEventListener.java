package com.uruz.frostbite.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationEventListener {

    private final ObjectMapper objectMapper;
    @Autowired
    public AuthenticationEventListener(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent onSuccess) throws JsonProcessingException {
        log.info("LOGIN SUCCEED : " + objectMapper.writeValueAsString(onSuccess));
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent onFailure) throws JsonProcessingException {
        log.info("LOGIN FAILED : " + objectMapper.writeValueAsString(onFailure));

    }
}
