package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Deprecated(forRemoval = true)
public class SimplePrivateFolderOffsetConditionByUuid {
    private UUID parentFolderUuid;
    private FolderStatus status;
    private Integer userId;
    private Integer offset;
    private Integer limit;

    private SimplePrivateFolderOffsetConditionByUuid(UUID parentFolderUuid, FolderStatus status, Integer userId, Integer offset, Integer limit){
        this.parentFolderUuid = parentFolderUuid;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.offset = offset;
        this.limit = limit;
    }

    public static SimplePrivateFolderOffsetConditionByUuid of(UUID parentFolderUuid, FolderStatus status, Integer userId, Integer offset, Integer limit){

        return new SimplePrivateFolderOffsetConditionByUuid(parentFolderUuid, status, userId, offset, limit);
    }
}
