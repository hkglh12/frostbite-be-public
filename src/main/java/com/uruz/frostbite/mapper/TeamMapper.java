package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.RelUserTeam;
import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.dto.team.TeamDetailResponse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Mapper
public interface TeamMapper {

    /** 팀 생성 */
    public int insertTeam(Team team);


    /** 팀 권한 생성*/
    public int insertUserTeamRelation(RelUserTeam relation);

    public Optional<TeamDetailResponse> selectTeam(UUID teamUuid);

    public List<TeamDetailResponse> selectTeamsByUserId(Integer userId);
}
