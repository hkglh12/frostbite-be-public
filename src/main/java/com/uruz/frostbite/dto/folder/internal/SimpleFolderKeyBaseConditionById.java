package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SimpleFolderKeyBaseConditionById {
    // TODO *Id -> UUID로 변경 현재 시점, PK를 이미 어디선가 조회했을 거라는 가정 하에 ID로 받고있음
    // 차라리 JOIN절, INNER JOIN 절로
    // FolderType 미사용 -> 이미 parentFolderId의 Context에 정보 있음
    //private FolderType folderType;
    private Integer ownerId;
    private Long parentFolderId;
    private FolderStatus status;
    private UUID lastFolderUuid;
    private Integer limit;

    private SimpleFolderKeyBaseConditionById(Integer ownerId, Long parentFolderId,
                                             FolderStatus status, UUID lastFolderUuid, Integer limit){
        //this.folderType = folderType;
        this.ownerId = ownerId;
        this.parentFolderId = parentFolderId;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.lastFolderUuid = lastFolderUuid;
        this.limit = limit;
    }
    public static SimpleFolderKeyBaseConditionById of(Integer ownerId, Long parentId,
                                                      FolderStatus status, UUID lastFolderUuid, Integer limit){

        return new SimpleFolderKeyBaseConditionById(ownerId, parentId, status, lastFolderUuid, limit);
    }
}
