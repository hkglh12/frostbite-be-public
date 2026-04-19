package com.uruz.frostbite.security;

import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.enums.user.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class CustomUserDetails implements UserDetails {
    private String email;
    private String password;
    private UUID uuid;
    private SimpleGrantedAuthority authority;
    private UserStatus status;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public UUID getUuid(){return uuid;}

    @Override
    public boolean isAccountNonExpired(){
        return status != UserStatus.EXPIRED;
    }

    @Override
    public boolean isAccountNonLocked(){
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isEnabled(){
        return status == UserStatus.ACTIVE;
    }

    private CustomUserDetails(String email, String password, SimpleGrantedAuthority authority, UUID uuid, UserStatus status){
        this.email = email;
        this.password = password;
        this.authority = authority;
        this.status = status;
        this.uuid = uuid;
    }
    public static CustomUserDetails fromUser (User user){
        return new CustomUserDetails(user.getEmail(), user.getPassword(),
                user.getAccountType().toSimpleGrantedAuthority(), user.getUuid(), user.getStatus());
    }
}
