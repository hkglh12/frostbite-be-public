package com.uruz.frostbite.dto.folder.internal;

import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SimpleTeamFolderOffsetConditionByFolderId {
    // TODO *Id -> UUID로 변경 현재 시점, PK를 이미 어디선가 조회했을 거라는 가정 하에 ID로 받고있음
    // 차라리 JOIN절, INNER JOIN 절로
    // FolderType 미사용 -> 이미 parentFolderId의 Context에 정보 있음
    private FolderType folderType = FolderType.TEAM;
    private Integer resourceScopeTeamId;
    private Integer userId;
    private Long parentFolderId;
    private FolderStatus status;
    private Integer offset;
    private Integer limit;

    private SimpleTeamFolderOffsetConditionByFolderId(Integer resourceScopeTeamId, Integer userId, Long parentFolderId,
                                                      FolderStatus status, Integer offset, Integer limit){
        //this.folderType = folderType;
        this.resourceScopeTeamId = resourceScopeTeamId;
        this.userId = userId;
        this.parentFolderId = parentFolderId;
        this.status = status == null ? FolderStatus.ACTIVE : status;
        this.offset = offset;
        this.limit = limit;
    }
    public static SimpleTeamFolderOffsetConditionByFolderId of(Integer resourceScopeTeamId, Integer userId, Long parentId,
                                                               FolderStatus status, Integer offset, Integer limit){

        return new SimpleTeamFolderOffsetConditionByFolderId(resourceScopeTeamId, userId, parentId, status, offset, limit);
    }
}
