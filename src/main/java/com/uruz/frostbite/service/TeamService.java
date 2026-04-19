package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.RelUserTeam;
import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.team.TeamCreateRequest;
import com.uruz.frostbite.dto.team.TeamDetailResponse;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.enums.common.GeneralRelationStatus;
import com.uruz.frostbite.enums.team.TeamRole;
import com.uruz.frostbite.mapper.TeamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamMapper teamMapper;
    private final TeamService self;
    private final UserService userService;

    @Autowired
    public TeamService(TeamMapper teamMapper, UserService userService, @Lazy TeamService self){
        this.teamMapper = teamMapper;
        this.self = self;
        this.userService = userService;

    }

    // 팀 생성 및 팀 Relation 추가
    @Transactional
    public TeamDetailResponse createTeam(UUID createUserUuid, TeamCreateRequest request) throws GeneralCustomException {
        User createUser = userService.getUser(createUserUuid);

        Team team = Team.create(request.getName(), LocalDateTime.now(), createUser.getUserId(), UUID.randomUUID());
        teamMapper.insertTeam(team);

        if(team.getTeamId() == null){
            throw new GeneralCustomException(ApiErrorCode.INTERNAL_ERROR, "TEAM CREATION FAILED");
        }

        RelUserTeam relation = self.createUserTeamRelation(createUser.getUserId(), team.getTeamId());


        return TeamDetailResponse.builder()
                .name(team.getName())
                .createTime(team.getCreateTime())
                .createUserName(createUser.getName())
                .uuid(team.getUuid())
                .status(team.getStatus())
                .build();
    }
    public TeamDetailResponse getTeam(UUID teamUuid){

        TeamDetailResponse teamDetailResponse = teamMapper.selectTeam(teamUuid).orElseThrow(
                () ->  new GeneralCustomException(ApiErrorCode.NOT_FOUND, "team not found")
        );

        return teamDetailResponse;
    }

    public List<TeamDetailResponse> getTeams (UUID userUuid){
        User targetUser = userService.getUser(userUuid);

        return teamMapper.selectTeamsByUserId(targetUser.getUserId());

    }
    public RelUserTeam createUserTeamRelation(Integer userId, Integer teamId){
        RelUserTeam relation = RelUserTeam.create(userId, teamId, userId, LocalDateTime.now(), TeamRole.HEAD, GeneralRelationStatus.ACTIVE);

        teamMapper.insertUserTeamRelation(relation);

        return relation;
    }

    public RelUserTeam createUserTeamRelation(Integer userId, Integer teamId, Integer inviterId){
        RelUserTeam relation = RelUserTeam.create(userId, teamId, inviterId, LocalDateTime.now(), TeamRole.HEAD, GeneralRelationStatus.ACTIVE);
        teamMapper.insertUserTeamRelation(relation);

        return relation;
    }
}
