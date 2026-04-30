package com.uruz.frostbite.dto.team;

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
public class TeamModifyCommand {

    /** TEAM UUID*/
    private UUID uuid;

    /** TEAM NAME*/
    private String name;

    /**수정 명령서 생성시간*/
    private LocalDateTime currentTime;
    public static TeamModifyCommand of(UUID uuid, String name) {
        return new TeamModifyCommand(uuid, name, LocalDateTime.now());
    }
}
