package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.RelUserTeam;
import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.dto.TeamDetailResponse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Mapper
public interface TeamMapper {

    /** 팀 생성 */
    public int insertTeam(Team team);


    /** 팀 권한 생성*/
    public int insertUserTeamRelation(RelUserTeam relation);

    public Optional<TeamDetailResponse> selectTeam(String uuid);
}
