package com.uruz.frostbite.dto.user;

import com.uruz.frostbite.validator.annotation.PhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(
        description = "사용자 회원가입 요청"
)
public class UserJoinRequest {
    @NotEmpty
    @Size(min = 2)
    @Schema(
            description = "사용자 이름"
    )
    private String name;
    @Schema(
            description = "사용자 이메일"
    )
    @Email(regexp="^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private String email;
    @Size(min = 6)
    @Schema(
            description = "사용자 비밀번호"
    )
    private String password;
    @PhoneNumber
    @Schema(
            description = "사용자 휴대전화번호"
    )
    private String phoneNumber;

}
