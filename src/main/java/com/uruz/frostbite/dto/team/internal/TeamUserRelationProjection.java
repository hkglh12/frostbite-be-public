package com.uruz.frostbite.dto.team.internal;

import com.uruz.frostbite.enums.team.TeamRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TeamUserRelationProjection {

    /* 식별자 */
    private Long relId;

    private Integer userId;
    private Integer teamId;
    private TeamRole role;

}
