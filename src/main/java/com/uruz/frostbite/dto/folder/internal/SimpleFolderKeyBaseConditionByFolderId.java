package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SimpleFolderKeyBaseConditionByFolderId {
    // TODO *Id -> UUID로 변경 현재 시점, PK를 이미 어디선가 조회했을 거라는 가정 하에 ID로 받고있음
    // 차라리 JOIN절, INNER JOIN 절로
    // FolderType 미사용 -> 이미 parentFolderId의 Context에 정보 있음
    //private FolderType folderType;

    private Long parentFolderId;
    private FolderStatus status;
    private UUID nextFolderUuid;
    private Integer limit;

    private SimpleFolderKeyBaseConditionByFolderId(Long parentFolderId,
                                                   FolderStatus status, UUID nextFolderUuid, Integer limit){
        //this.folderType = folderType;
        //this.ownerId = ownerId;
        this.parentFolderId = parentFolderId;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.nextFolderUuid = nextFolderUuid;
        this.limit = limit;
    }
    public static SimpleFolderKeyBaseConditionByFolderId of(Long parentId,
                                                            FolderStatus status, UUID nextFolderUuid, Integer limit){

        return new SimpleFolderKeyBaseConditionByFolderId(parentId, status, nextFolderUuid, limit);
    }
}
