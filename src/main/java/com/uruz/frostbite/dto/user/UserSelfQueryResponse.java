package com.uruz.frostbite.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.user.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Schema(
        description = "사용자 본인정보 조회 결과"
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSelfQueryResponse {
    @Schema(
            description = "사용자 UUID(식별자)"
    )
    private UUID uuid;
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
            description = "사용자 가입날짜"
    )
    @JsonFormat(pattern = "yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate createDate;

    private AccountType account_type;
}
