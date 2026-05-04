package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SimpleFolderConditionById {
    // TODO *Id -> UUID로 변경 현재 시점, PK를 이미 어디선가 조회했을 거라는 가정 하에 ID로 받고있음
    // 차라리 JOIN절, INNER JOIN 절로
    private FolderType folderType;
    private Long ownerId;
    private Long parentFolderId;
    private FolderStatus status;

    private SimpleFolderConditionById(FolderType folderType, Long ownerId, Long parentFolderId, FolderStatus status){
        this.folderType = folderType;
        this.ownerId = ownerId;
        this.parentFolderId = parentFolderId;
        this.status = status == null ? FolderStatus.ACTIVE : status;
    }
    public static SimpleFolderConditionById of(FolderType folderType, Long ownerId, Long parentId){

        return new SimpleFolderConditionById(folderType, ownerId, parentId, null);
    }
    public static SimpleFolderConditionById of(FolderType folderType, Long ownerId, Long parentId, FolderStatus status){

        return new SimpleFolderConditionById(folderType, ownerId, parentId, status);
    }
}
