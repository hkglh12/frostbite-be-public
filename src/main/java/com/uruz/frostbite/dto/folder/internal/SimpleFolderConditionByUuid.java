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
public class SimpleFolderConditionByUuid {
    private FolderType folderType;
    private UUID ownerUuid;
    private UUID parentFolderUuid;
    private FolderStatus status;

    private SimpleFolderConditionByUuid(FolderType folderType, UUID ownerUuid, UUID parentFolderUuid, FolderStatus status){
        this.folderType = folderType;
        this.ownerUuid = ownerUuid;
        this.parentFolderUuid = parentFolderUuid;
        this.status = status == null ? FolderStatus.ACTIVE : status;
    }
    public static SimpleFolderConditionByUuid of(FolderType folderType,UUID ownerUuid, UUID parentFolderUuid){

        return new SimpleFolderConditionByUuid(folderType, ownerUuid, parentFolderUuid, null);
    }

    public static SimpleFolderConditionByUuid of(FolderType folderType,UUID ownerUuid, UUID parentFolderUuid, FolderStatus status){

        return new SimpleFolderConditionByUuid(folderType, ownerUuid, parentFolderUuid, null);
    }
}
