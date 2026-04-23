package com.uruz.frostbite.security;

import lombok.Getter;

import java.util.UUID;

@Getter
public class SecurityContextUserContext {
    private String email;

    private UUID uuid;
    public SecurityContextUserContext(String email, UUID uuid){
        this.email = email;
        this.uuid = uuid;
    }


}
