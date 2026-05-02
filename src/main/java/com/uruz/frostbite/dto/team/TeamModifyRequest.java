package com.uruz.frostbite.dto.team;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.team.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(
        description = "팀 수정요청"
)
public class TeamModifyRequest {

        @Schema(
                description = "팀 이름"
        )
        @NotNull
        /** 팀 이름 */
        private String name;

        @Schema(
                description = "팀 UUID"
        )
        @NotNull
        /** 팀 UUID */
        private UUID uuid;
        

}
