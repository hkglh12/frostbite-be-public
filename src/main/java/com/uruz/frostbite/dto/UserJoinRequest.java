package com.uruz.frostbite.dto;

import com.uruz.frostbite.validator.annotation.PhoneNumber;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserJoinRequest {
    @NotEmpty
    @Size(min = 2)
    private String name;
    @Email(regexp="^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private String email;
    @Size(min = 6)
    private String password;
    @PhoneNumber
    private String phoneNumber;
    @NotNull
    private LocalDate birth;
}
