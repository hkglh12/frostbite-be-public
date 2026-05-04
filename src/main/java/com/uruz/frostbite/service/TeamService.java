package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.RelUserTeam;
import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.team.*;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.enums.common.GeneralRelationStatus;
import com.uruz.frostbite.enums.team.TeamRole;
import com.uruz.frostbite.enums.team.TeamStatus;
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
    /** 팀 생성 및 관계추가*/
    @Transactional
    public TeamDetailResponse createTeam(Integer createUserId, TeamCreateRequest request) throws GeneralCustomException {
        // 사용자 UUID가 유효한가? 와 동시에 Team detail DTO 생성을 위한 데이터 조회가 주 목적
        // authenticationprincipal에서 pk를 가져다 쓰므로, User domain 데이터 조회 목적만 수행하면 됨
        User createUser = userService.getUserByUserId(createUserId);

        Team team = Team.create(request.getName(), LocalDateTime.now(), createUserId, UUID.randomUUID());
        teamMapper.insertTeam(team);

        if(team.getTeamId() == null){
            throw new GeneralCustomException(ApiErrorCode.INTERNAL_ERROR, "TEAM CREATION FAILED");
        }

        RelUserTeam relation = self.createUserTeamRelation(createUserId, team.getTeamId());


        // TODO 팀 최상위 폴더 자동생성 및 ACL 추가 필요
        return TeamDetailResponse.builder()
                .name(team.getName())
                .createTime(team.getCreateTime())
                .createUserName(createUser.getName())
                .modifyTime(null)
                .modifyUserName(null)
                .uuid(team.getUuid())
                .status(team.getStatus())
                .build();
    }
    /** 팀 상세정보 조회*/
    public TeamDetailResponse getTeamDetail(UUID teamUuid) throws GeneralCustomException{

        //TODO 추후 팀 소속 검증 필요, ACL 추가 후 where절에 추가하고 별도의 서비스 메서드 호출로 처리 필요
        TeamProjection teamProjection = teamMapper.selectTeamProjection(teamUuid).orElseThrow(
                () ->  new GeneralCustomException(ApiErrorCode.NOT_FOUND, "TEAM INFORMATION NOTFOUND")
        );

        return TeamDetailResponse.builder()
                .name(teamProjection.getName())
                .createTime(teamProjection.getCreateTime())
                .createUserName(teamProjection.getCreateUserName())
                .modifyTime(teamProjection.getModifyTime())
                .modifyUserName(teamProjection.getModifyUserName())
                .uuid(teamProjection.getUuid())
                .status(teamProjection.getStatus())
                .build();
    }

    /** 팀 정보 수정 및 기록 관리*/
    public TeamDetailResponse modifyTeam(TeamModifyRequest request, Integer userId) throws GeneralCustomException{
        User user = userService.getUserByUserId(userId);

        // TODO 추후 권한 검증 필요 (소속여부, 권한부여여부)
        TeamProjection teamProjection = teamMapper.selectTeamProjection(request.getUuid()).orElseThrow(
                () ->  new GeneralCustomException(ApiErrorCode.NOT_FOUND, "TEAM INFORMATION NOTFOUND")
        );
        TeamModifyCommand command = TeamModifyCommand.of(
                request.getUuid(),
                request.getName(),
                user.getUserId()
        );
        int affectedRow = teamMapper.updateTeam(command);

        if(affectedRow == 0){
            throw new GeneralCustomException(ApiErrorCode.INTERNAL_ERROR, "TEAM INFORMATION UPDATE FAILED");
        }


        return TeamDetailResponse.builder()
                .name(request.getName())
                .createTime(teamProjection.getCreateTime())
                .createUserName(teamProjection.getCreateUserName())
                .modifyUserName(user.getName())
                .modifyTime(command.getCurrentTime())
                .status(teamProjection.getStatus())
                .build();
    }

    public void softDeleteTeam(UUID teamUuid,Integer userId) throws GeneralCustomException{
        Team team = getTeam(teamUuid);
        User user = userService.getUserByUserId(userId);

        TeamStatusChangeCommand command = TeamStatusChangeCommand.of(
                teamUuid,
                TeamStatus.DELETED,
                user.getUserId()
        );


        teamMapper.updateTeamStatus(command);
    }

    /** 사용자 UUID로부터 가입환 팀 목록 조회 */
    public List<TeamDetailResponse> getTeamsByUserUuid(UUID userUuid){
        User targetUser = userService.getUserByUuid(userUuid);

        return teamMapper.selectTeamsByUserId(targetUser.getUserId());
    }

    public List<TeamDetailResponse> getTeamsByUserId(Integer userId){
        return teamMapper.selectTeamsByUserId(userId);
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
