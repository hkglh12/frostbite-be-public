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
public class TeamResolverService {

    private final TeamMapper teamMapper;
    private final TeamResolverService self;

    @Autowired
    public TeamResolverService(TeamMapper teamMapper, @Lazy TeamResolverService self){
        this.teamMapper = teamMapper;
        this.self = self;
    }

    public Team getTeamByUuid(UUID teamUuid){
        return teamMapper.selectTeam(teamUuid).orElseThrow(() -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "Target team not found"));
    }

    public Team getTeambyTeamId(Integer teamId){
        return teamMapper.selectTeamByTeamId(teamId).orElseThrow(() -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "Target team not found"));
    }
}
