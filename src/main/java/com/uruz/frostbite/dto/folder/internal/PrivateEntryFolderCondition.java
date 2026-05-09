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
public class PrivateEntryFolderCondition {
    private UUID userUuid;
    private Integer userId;
    private FolderType folderType;
    private Long parentFolderId;
    private FolderStatus status;

    private PrivateEntryFolderCondition(UUID userUuid, FolderType folderType){
        this.userUuid = userUuid;
        this.folderType = folderType;
        this.parentFolderId = null;
        this.status = FolderStatus.ACTIVE;
    }
    private PrivateEntryFolderCondition(Integer userId, FolderType folderType){
        this.userId = userId;
        this.folderType = folderType;
        this.parentFolderId = null;
        this.status = FolderStatus.ACTIVE;
    }
    public static PrivateEntryFolderCondition fromUserUuid(UUID userUuid){

        return new PrivateEntryFolderCondition(userUuid, FolderType.PRIVATE);
    }
    public static PrivateEntryFolderCondition fromUserId(Integer userId){
        return new PrivateEntryFolderCondition(userId, FolderType.PRIVATE);
    }
}
