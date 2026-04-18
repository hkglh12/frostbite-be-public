package com.uruz.frostbite.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(
        description = "사용자 회원가입 결과 응답"
)
public class UserJoinResponse {
    @Schema(
            description = "사용자 이름"
    )
    private String name;
    @Schema(
            description = "사용자 이메일"
    )
    private String email;
    @Schema(
            description = "사용자 휴대전화번호"
    )
    private String phoneNumber;
    @Schema(
            description = "사용자 생년월일"
    )
    @JsonFormat(pattern = "yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate birth;
    @Schema(
            description = "사용자 가입날짜"
    )
    @JsonFormat(pattern = "yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate createDate;
    @Schema(
            description = "사용자 UUID, 식별자"
    )
    private UUID uuid;
    @Schema(
            description = "사용자 계정상태"
    )
    private UserStatus status;
}
