package com.uruz.frostbite.dto.team;

import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.enums.team.TeamStatus;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TeamProjection {

    /** 팀 식별자 */
    private Integer teamId;

    /** 팀 이름 */
    private String name;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 생성자 (user_id) */
    private Integer createUser;

    /** 생성시각 */
    private LocalDateTime modifyTime;

    /** 생성자 (user_id) */
    private Integer modifyUser;

    /** 팀 생성자 이름 */
    private String createUserName;

    private String modifyUserName;

    /** 팀 UUID */
    private UUID uuid;

    /** 삭제여부 */
    private TeamStatus status;

}
