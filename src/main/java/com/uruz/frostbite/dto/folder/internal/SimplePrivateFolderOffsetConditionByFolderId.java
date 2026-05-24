package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SimplePrivateFolderOffsetConditionByFolderId {
    // TODO *Id -> UUID로 변경 현재 시점, PK를 이미 어디선가 조회했을 거라는 가정 하에 ID로 받고있음
    // 차라리 JOIN절, INNER JOIN 절로
    // FolderType 미사용 -> 이미 parentFolderId의 Context에 정보 있음
    //private FolderType folderType;

    private Integer resourceScopeTeamId;
    private Integer resourceScopeUserId;
    private Long parentFolderId;
    private FolderStatus status;
    private Integer offset;
    private Integer limit;

    private SimplePrivateFolderOffsetConditionByFolderId(Integer resourceScopeUserId, Long parentFolderId,
                                                         FolderStatus status, Integer offset, Integer limit){
        //this.folderType = folderType;
        this.resourceScopeUserId = resourceScopeUserId;
        this.parentFolderId = parentFolderId;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.offset = offset;
        this.limit = limit;
    }
    public static SimplePrivateFolderOffsetConditionByFolderId of(Integer resourceScopeUserId, Long parentId,
                                                                  FolderStatus status, Integer offset, Integer limit){

        return new SimplePrivateFolderOffsetConditionByFolderId(resourceScopeUserId, parentId, status, offset, limit);
    }
}
