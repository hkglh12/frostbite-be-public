package com.uruz.frostbite.dto;

import com.uruz.frostbite.validator.annotation.UserReadConditionRequireOne;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@UserReadConditionRequireOne
@Schema(
        description = "사용자(본인정보) 조회 조건 (uuid, email, name 중 하나 이상 필수)"
)
public class UserSelfQueryCondition {
    @Schema(description = "조회 대상 UUID")
    private String uuid;
    @Schema(description = "조회 대상 email")
    private String email;
    @Schema(description = "조회 대상 name")
    private String name;
}
