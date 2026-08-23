package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
public class PrivateFolderCondition {
    private final FolderType folderType = FolderType.PRIVATE;
    private final FolderStatus status;
    private final Integer userId;
    private final Long folderId;
    private final UUID folderUuid;


    private PrivateFolderCondition(
            FolderStatus status,
            Integer userId,
            Long folderId,
            UUID folderUuid
    ) {
        this.status = status;
        this.userId = userId;
        this.folderId = folderId;
        this.folderUuid = folderUuid;

    }

    public static PrivateFolderCondition fromUserId(Integer userId) {
        return new PrivateFolderCondition(
                FolderStatus.ACTIVE,
                userId,
                null,
                null

        );
    }

    public PrivateFolderCondition withFolderid(Long folderId) {
        return new PrivateFolderCondition(
                this.status,
                this.userId,
                folderId,
                null
        );
    }

    public PrivateFolderCondition withFolderUuid(UUID folderUuid) {
        return new PrivateFolderCondition(
                this.status,
                this.userId,
                null,
                folderUuid
        );
    }
}
