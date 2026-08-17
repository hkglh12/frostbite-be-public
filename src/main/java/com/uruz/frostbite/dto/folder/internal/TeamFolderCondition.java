package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TeamFolderCondition {
    private final FolderType folderType = FolderType.TEAM;
    private final FolderStatus status;
    private final Integer teamId;
    private final Integer userId;
    private final Long folderId;
    private final UUID folderUuid;


    private TeamFolderCondition(
            FolderStatus status,
            Integer teamId,
            Integer userId,
            Long folderId,
            UUID folderUuid
    ) {
        this.status = status;
        this.teamId = teamId;
        this.userId = userId;
        this.folderId = folderId;
        this.folderUuid = folderUuid;

    }

    public static TeamFolderCondition fromTeamIdWithUserId(Integer teamId, Integer userId) {
        return new TeamFolderCondition(
                FolderStatus.ACTIVE,
                teamId,
                userId,
                null,
                null

        );
    }

    public TeamFolderCondition withFolderid(Long folderId) {
        return new TeamFolderCondition(
                this.status,
                this.teamId,
                this.userId,
                folderId,
                null
        );
    }

    public TeamFolderCondition withFolderUuid(UUID folderUuid) {
        return new TeamFolderCondition(
                this.status,
                this.teamId,
                this.userId,
                null,
                folderUuid
        );
    }
}
