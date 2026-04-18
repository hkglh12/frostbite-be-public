package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.TeamRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RelUserTeam {

    /** 사용자-팀 관계식별자 */
    private Integer relId;

    /** 사용자 식별자 */
    private Integer userId;

    /** 팀 식별자 */
    private Integer teamId;

    /** 초대자 (user_id) */
    private Integer createUser;

    /** 합류시각 */
    private LocalDateTime joinTime;

    /** 삭제여부 */
    private Short isDelete;
    /** 팀 내 역할 */
    private TeamRole teamRole;
}
