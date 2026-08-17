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
@Deprecated(forRemoval = true)
public class PrivateEntryFolderCondition {
    private UUID userUuid;
    private Integer userId;
    private FolderType folderType = FolderType.PRIVATE;
    private FolderStatus status;

    private PrivateEntryFolderCondition(UUID userUuid){
        this.userUuid = userUuid;
        this.status = FolderStatus.ACTIVE;
    }
    private PrivateEntryFolderCondition(Integer userId){
        this.userId = userId;
        this.status = FolderStatus.ACTIVE;
    }
    public static PrivateEntryFolderCondition fromUserUuid(UUID userUuid){
        return new PrivateEntryFolderCondition(userUuid);
    }
    public static PrivateEntryFolderCondition fromUserId(Integer userId){
        return new PrivateEntryFolderCondition(userId);
    }
}
