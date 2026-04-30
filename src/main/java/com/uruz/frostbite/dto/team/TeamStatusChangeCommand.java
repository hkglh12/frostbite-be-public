package com.uruz.frostbite.dto.team;

import com.uruz.frostbite.enums.team.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(description = "팀 수정 요청용 DTO, Command")
public class TeamStatusChangeCommand {

    /** TEAM UUID*/
    private UUID uuid;

    private TeamStatus status;
    /**수정 명령서 생성시간*/
    private LocalDateTime currentTime;
    public static TeamStatusChangeCommand of(UUID uuid, TeamStatus status) {
        return new TeamStatusChangeCommand(uuid, status, LocalDateTime.now());
    }
}
