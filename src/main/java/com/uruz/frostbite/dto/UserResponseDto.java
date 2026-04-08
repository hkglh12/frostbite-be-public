package com.uruz.frostbite.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UserResponseDto {

    private Integer userId;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDate birth;
    private LocalDate createDate;
    private UUID uuid;
    private Short isDelete;
}
