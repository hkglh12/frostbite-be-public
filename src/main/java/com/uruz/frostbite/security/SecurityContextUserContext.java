package com.uruz.frostbite.security;

import lombok.Getter;

@Getter
public class SecurityContextUserContext {
    private String email;

    public SecurityContextUserContext(String email){
        this.email = email;
    }


}
