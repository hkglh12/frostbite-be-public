package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Team {

    /** 팀 식별자 */
    private Integer teamId;

    /** 팀 이름 */
    private String name;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 생성자 (user_id) */
    private Integer createUser;

    /** 팀 UUID */
    private UUID uuid;

    /** 삭제여부 */
    private Short isDelete;
}
