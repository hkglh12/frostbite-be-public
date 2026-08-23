package com.uruz.frostbite.mapper;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.RelUserTeam;
import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.dto.team.TeamDetailResponse;
import com.uruz.frostbite.dto.team.TeamModifyCommand;
import com.uruz.frostbite.dto.team.TeamProjection;
import com.uruz.frostbite.dto.team.TeamStatusChangeCommand;
import com.uruz.frostbite.dto.team.internal.TeamUserRelationProjection;
import com.uruz.frostbite.enums.common.GeneralRelationStatus;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Mapper
public interface TeamMapper {

    /** 팀 생성 */
    public int insertTeam(Team team);

    /** 팀 상세정보 조회 (프로젝션)*/
    public Optional<TeamProjection> selectTeamProjection(UUID teamUuid);
    /** 팀 정보 수정*/
    public int updateTeam(TeamModifyCommand command);
    /** 팀 softdelete */
    public void updateTeamStatus(TeamStatusChangeCommand command);
    /** 팀 엔티티 조회*/
    public Optional<Team> selectTeam(UUID teamUuid);
    public Optional<Team> selectTeamByTeamId(Integer teamId);

    public List<TeamDetailResponse> selectTeamsByUserId(Integer userId);


    /** 팀 권한 생성*/
    public int insertUserTeamRelation(RelUserTeam relation);

    public TeamUserRelationProjection selectTeamUserRelation(@Param("userId") Integer userId,
                                                             @Param("teamId") Integer teamId,
                                                             @Param("status")GeneralRelationStatus status);

    Optional<Team> selectAccessibleTeamByTeamUuid(@Param("userId") Integer userId, @Param("teamUuid") UUID teamUuid);

}
