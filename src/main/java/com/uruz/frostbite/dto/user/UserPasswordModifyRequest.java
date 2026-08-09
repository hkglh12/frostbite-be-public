package com.uruz.frostbite.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
        description = "사용자 비밀번호 변경 요청"
)
public class UserPasswordModifyRequest {
    @Schema
            (description = "현재 비밀번호")
    String currentPassword;
    @Schema(
            description = "신규 비밀번호"
    )
    String newPassword;
}
