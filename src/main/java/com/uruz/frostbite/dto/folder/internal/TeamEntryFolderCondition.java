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
    private UUID userUuid;
    private UUID teamUuid;
    private FolderType folderType;
    private Long parentFolderId;
    private FolderStatus status;

    private TeamEntryFolderCondition(UUID userUuid, UUID teamUuid, FolderType folderType){
        this.userUuid = userUuid;
        this.teamUuid = teamUuid;
        this.folderType = folderType;
        this.parentFolderId = null;
        this.status = FolderStatus.ACTIVE;
    }
    public static TeamEntryFolderCondition of(UUID userUuid, UUID teamUuid){

        return new TeamEntryFolderCondition(userUuid, teamUuid, FolderType.TEAM);
    }
}
