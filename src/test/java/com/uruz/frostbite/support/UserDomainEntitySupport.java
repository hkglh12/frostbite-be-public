package com.uruz.frostbite.support;

import com.uruz.frostbite.dto.user.UserJoinRequest;

import java.time.LocalDate;
import java.util.UUID;

public final class UserDomainEntitySupport {


    public static UserJoinRequest joinRequest() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UserJoinRequest request = new UserJoinRequest();
        request.setName("사용자테스트");
        request.setEmail("join-" + suffix + "@example.com");
        request.setPassword("password123");
        request.setPhoneNumber(String.format("010%08d", Math.floorMod(suffix.hashCode(), 100_000_000)));
        return request;
    }
}
