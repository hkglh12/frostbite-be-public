package com.uruz.frostbite.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum AccountType {
    /** 전역 계정 종류
     * */
    USER(1, "USER"),    // 일반사용자
    ADMIN(2, "ADMIN");   // 시스템관리자

    private final int value;
    private final String name;

    AccountType(int value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }
    public String getName(){return name;}

    public static AccountType fromValue(int value) {
        for (AccountType status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AccountType value: " + value);
    }
    @JsonValue
    public String toSimpleAccountTypeString(){
        return name;
    }

    public SimpleGrantedAuthority toSimpleGrantedAuthority(){
        return new SimpleGrantedAuthority("ROLE_"+name);
    }
}
