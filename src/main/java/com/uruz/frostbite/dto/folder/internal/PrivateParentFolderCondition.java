package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PrivateParentFolderCondition {
    private final FolderType folderType = FolderType.PRIVATE;
    private final FolderStatus status;
    private final Integer userId;
    private final Long parentFolderId;
    private final UUID parentFolderUuid;


    private PrivateParentFolderCondition(
            FolderStatus status,
            Integer userId,
            Long parentFolderId,
            UUID parentFolderUuid
    ) {
        this.status = status;
        this.userId = userId;
        this.parentFolderId = parentFolderId;
        this.parentFolderUuid = parentFolderUuid;

    }

    public static PrivateParentFolderCondition fromUserId(Integer userId) {
        return new PrivateParentFolderCondition(
                FolderStatus.ACTIVE,
                userId,
                null,
                null

        );
    }

    public PrivateParentFolderCondition withParentFolderId(Long parentFolderId) {
        return new PrivateParentFolderCondition(
                this.status,
                this.userId,
                parentFolderId,
                null
        );
    }

    public PrivateParentFolderCondition withFolderParentFolderUuid(UUID parentFolderUuid) {
        return new PrivateParentFolderCondition(
                this.status,
                this.userId,
                null,
                parentFolderUuid
        );
    }
}
