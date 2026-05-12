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
public class SimpleFolderOffsetConditionByUuid {
    private UUID parentFolderUuid;
    private FolderStatus status;

    private Integer offset;
    private Integer limit;

    private SimpleFolderOffsetConditionByUuid(UUID parentFolderUuid, FolderStatus status, Integer offset, Integer limit){
        this.parentFolderUuid = parentFolderUuid;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.offset = offset;
        this.limit = limit;
    }

    public static SimpleFolderOffsetConditionByUuid of(UUID parentFolderUuid, FolderStatus status, Integer offset, Integer limit){

        return new SimpleFolderOffsetConditionByUuid(parentFolderUuid, status, offset, limit);
    }
}
