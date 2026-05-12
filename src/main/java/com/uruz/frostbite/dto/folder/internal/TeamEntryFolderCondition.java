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
public class TeamEntryFolderCondition {
    private Integer userId;
    private UUID teamUuid;
    private FolderType folderType;
    private Long parentFolderId;
    private FolderStatus status;

    private TeamEntryFolderCondition(Integer userId, UUID teamUuid, FolderType folderType){
        this.userId = userId;
        this.teamUuid = teamUuid;
        this.folderType = folderType;
        this.parentFolderId = null;
        this.status = FolderStatus.ACTIVE;
    }
    public static TeamEntryFolderCondition of(Integer userId, UUID teamUuid){

        return new TeamEntryFolderCondition(userId, teamUuid, FolderType.TEAM);
    }
}
