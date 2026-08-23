package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TeamFolderCursorBaseConditionByFolderUuid {
    private FolderType folderType = FolderType.TEAM;
    private FolderStatus status = FolderStatus.ACTIVE;
    private UUID teamUuid;
    private Integer teamId;
    private Integer userId;
    private Long parentFolderId;
    private UUID nextFolderUuid;
    private Integer limit;

    private UUID lastFolderUuid;



    private TeamFolderCursorBaseConditionByFolderUuid(UUID teamUuid, Integer teamId, Integer userId, Long parentFolderId, UUID nextFolderUuid , FolderStatus status, Integer limit){
        this.teamUuid = teamUuid;
        this.teamId = teamId;
        this.userId = userId;
        this.parentFolderId = parentFolderId;
        this.nextFolderUuid = nextFolderUuid;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.limit = limit;
    }

    private TeamFolderCursorBaseConditionByFolderUuid(UUID teamUuid, Integer teamId, Integer userId, Long parentFolderId, UUID nextFolderUuid , FolderStatus status, Integer limit, UUID lastFolderUuid){
        this.teamUuid = teamUuid;
        this.teamId = teamId;
        this.userId = userId;
        this.parentFolderId = parentFolderId;
        this.nextFolderUuid = nextFolderUuid;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.limit = limit;
        this.lastFolderUuid = lastFolderUuid;
    }

    public static TeamFolderCursorBaseConditionByFolderUuid ofTeamId(Integer teamId, Integer userId, Long parentFolderUuid, UUID nextFolderUuid, FolderStatus status, Integer limit){

        return new TeamFolderCursorBaseConditionByFolderUuid(null, teamId, userId, parentFolderUuid,  nextFolderUuid, status, limit);
    }

    public static TeamFolderCursorBaseConditionByFolderUuid ofTeamUuid(UUID teamUuid, Integer userId, Long parentFolderUuid, UUID nextFolderUuid, FolderStatus status, Integer limit){

        return new TeamFolderCursorBaseConditionByFolderUuid(teamUuid, null, userId, parentFolderUuid,  nextFolderUuid, status, limit);
    }


    /**
     * 이전 Cursor base 조회의 마지막 FolderUUID를 통해 이후 조회 대상이 남아있는지 확인하기 위한 조회조건 생성
     * @param lastFolderUuid 마지막folderUuid
     * */
    public TeamFolderCursorBaseConditionByFolderUuid nextFolderUuidCondition(UUID lastFolderUuid){
        return new TeamFolderCursorBaseConditionByFolderUuid(
                this.teamUuid,
                this.teamId,
                this.userId,
                this.parentFolderId,
                this.nextFolderUuid,
                this.status,
                this.limit,
                lastFolderUuid
        );
    }
}
