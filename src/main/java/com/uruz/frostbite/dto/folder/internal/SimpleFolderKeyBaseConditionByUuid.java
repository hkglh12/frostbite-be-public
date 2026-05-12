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
public class SimpleFolderKeyBaseConditionByUuid {
    private UUID parentFolderUuid;
    private FolderStatus status;
    private UUID lastFolderUuid;
    private Integer limit;

    private SimpleFolderKeyBaseConditionByUuid(UUID parentFolderUuid, FolderStatus status, UUID lastFolderUuid, Integer limit){
        this.parentFolderUuid = parentFolderUuid;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.lastFolderUuid = lastFolderUuid;
        this.limit = limit;
    }

    public static SimpleFolderKeyBaseConditionByUuid of(UUID parentFolderUuid, FolderStatus status, UUID lastFolderUuid, Integer limit){

        return new SimpleFolderKeyBaseConditionByUuid(parentFolderUuid, status, lastFolderUuid, limit);
    }
}
