package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.RelUserTeam;
import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.team.*;
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
    /** 팀 생성 및 관ㄱ{}추가*/
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
    /** 팀 상세정보 조회*/
    public TeamDetailResponse getTeamDetail(UUID teamUuid) throws GeneralCustomException{

        TeamProjection teamProjection = teamMapper.selectTeamProjection(teamUuid).orElseThrow(
                () ->  new GeneralCustomException(ApiErrorCode.NOT_FOUND, "TEAM INFORMATION NOTFOUND")
        );

        return TeamDetailResponse.builder()
                .name(teamProjection.getName())
                .createTime(teamProjection.getCreateTime())
                .createUserName(teamProjection.getCreateUserName())
                .uuid(teamProjection.getUuid())
                .status(teamProjection.getStatus())
                .build();
    }

    /** 팀 정보 수정 및 기록 관리*/
    public TeamDetailResponse modifyTeam(TeamModifyRequest request) throws GeneralCustomException{
        Team team = getTeam(request.getUuid());
        TeamModifyCommand command = TeamModifyCommand.of(
                request.getUuid(),
                request.getName()
        );
        int affectedRow = teamMapper.updateTeam(command);

        if(affectedRow == 0){
            throw new GeneralCustomException()
        }

        return null;
    }

    /** 사용자 UUID로부터 가입환 팀 목록 조회 */
    public List<TeamDetailResponse> getTeamsByUserUuid(UUID userUuid){
        User targetUser = userService.getUser(userUuid);

        return teamMapper.selectTeamsByUserId(targetUser.getUserId());

    }

    /** 사용자 팀 관계 생성*/
    public RelUserTeam createUserTeamRelation(Integer userId, Integer teamId){
        RelUserTeam relation = RelUserTeam.create(userId, teamId, userId, LocalDateTime.now(), TeamRole.HEAD, GeneralRelationStatus.ACTIVE);

        teamMapper.insertUserTeamRelation(relation);

        return relation;
    }



    /** 팀 엔티티 조회 헬퍼 메서드 */
    public Team getTeam(UUID uuid) throws GeneralCustomException{

        return teamMapper.selectTeam(uuid).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "TEAM INFORMATION NOTFOUND")
        );
    }

    public RelUserTeam createUserTeamRelation(Integer userId, Integer teamId, Integer inviterId){
        RelUserTeam relation = RelUserTeam.create(userId, teamId, inviterId, LocalDateTime.now(), TeamRole.HEAD, GeneralRelationStatus.ACTIVE);
        teamMapper.insertUserTeamRelation(relation);

        return relation;
    }
}
