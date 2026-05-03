package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.team.TeamStatus;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class Team {

    /** 팀 식별자 */
    private Integer teamId;

    /** 팀 이름 */
    private String name;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 생성자 (user_id) */
    private Integer createUserId;

    /** 수정시각 */
    private LocalDateTime modifyTime;

    /** 수정자 */
    private Integer modifyUserId;

    /** 팀 UUID */
    private UUID uuid;

    /** 삭제여부 */
    private TeamStatus status;

    private Team(String name, LocalDateTime createTime, Integer createUserId, UUID uuid, TeamStatus status){
        this.name = name;
        this.createTime = createTime;
        this.createUserId= createUserId;
        this.uuid = uuid;
        this.status = status;
    }

    public static Team create(String name, LocalDateTime createTime, Integer createUserId, UUID uuid){
        if(StringUtils.isEmpty(name) || name.length() > 256){
            throw new IllegalArgumentException("Name constraint violated");
        }
        return new Team(
                name, createTime, createUserId, uuid, TeamStatus.ACTIVE
        );
    }
}
