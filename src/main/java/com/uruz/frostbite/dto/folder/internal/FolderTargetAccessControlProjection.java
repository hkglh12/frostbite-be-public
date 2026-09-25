package com.uruz.frostbite.dto.folder.internal;


import com.uruz.frostbite.enums.FolderType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class FolderTargetAccessControlProjection {
    /** UUID */
    private UUID uuid;
    /** 폴더 식별자 */
    private Long folderId;
    /** 폴더종류 (1: private, 2: team) */
    private FolderType folderType;
    /** 부모 폴더 식별자 */
    private Long parentFolderId;
    /** 폴더 자원 소유 객체 정보 */
    private Integer resourceScopeTeamId;
    private Integer resourceScopeUserId;
    /** 폴더 실제 관리자 (USER PK) */
    private Integer resourceOwnerId;
}
