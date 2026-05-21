package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.AccessPolicy;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Folder {

    /** 폴더 식별자 */
    private Long folderId;

    /** 폴더명 */
    private String name;

    /** 폴더종류 (1: private, 2: team) */
    private FolderType folderType;

    /** 해당 폴더의 피종속 자원 식별자
     * folderType : private -> userid
     * folderType : team -> teamid
     * */
    private Integer resourceScopeId;

    private Integer resourceOwnerId;
    private AccessPolicy accessPolicy;

    /** 폴더 생성자 (user_id) */
    private Integer createUserId;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 수정자 (user_id) */
    private Integer modifyUserId;

    /** 수정시각 */
    private LocalDateTime modifyTime;

    /** 부모 폴더 식별자 */
    private Long parentFolderId;

    /** 출력순서 */
    private Integer renderSequence;

    /** UUID */
    private UUID uuid;

    /** 삭제여부 */
    private FolderStatus status;

    private String folderIdPath;
    private String folderNamePath;

    /** 회원가입 직후 entryFolder 생성*/

    // resourceScopeId == createUser
    public static Folder createEntryFolder(String name, Integer ownerId,
                                           FolderType folderType){
        if(name.trim().length()<3 || name.trim().length() > 512){
            throw new IllegalArgumentException("Name constraint violated");
        }

        return new Folder(null, name, folderType, ownerId, ownerId, AccessPolicy.USER, ownerId, LocalDateTime.now(),
                ownerId, LocalDateTime.now(), null, 0, UUID.randomUUID(), FolderStatus.ACTIVE, null, null);
    }

    /** 개인 루트폴더 생성용 엔티티 생성 */
    public static Folder createPrivateRootFolder(String name, Long parentFolderId, Integer ownerId){
        return new Folder(null, name, FolderType.PRIVATE, ownerId, ownerId, AccessPolicy.USER, ownerId, LocalDateTime.now(), null, null,
                parentFolderId, null, UUID.randomUUID(), FolderStatus.ACTIVE, null, null);
    }

    /** 팀 루트 폴더 생성용 엔티티 생성 */
    public static Folder createTeamRootFolder(String name, Long parentFolderId, Integer resourceScopeId, Integer createUserId){
        return new Folder(null, name, FolderType.TEAM, resourceScopeId, createUserId, AccessPolicy.TEAM, createUserId, LocalDateTime.now(), null, null,
                parentFolderId, null, UUID.randomUUID(), FolderStatus.ACTIVE, null, null);
    }

    /** 개인 서브 폴더 생성용 엔티티 생성 */
    public static Folder createPrivateSubFolder(String name, Long parentFolderId, Integer createUserId){
        return new Folder(null, name, FolderType.PRIVATE, createUserId, createUserId, AccessPolicy.USER, createUserId, LocalDateTime.now(), null, null,
                parentFolderId, null, UUID.randomUUID(), FolderStatus.ACTIVE, null, null);
    }

    /** 팀 서브폴더 생성용 엔티티 생성*/
    public static Folder createTeamSubFolder(String name, Long parentFolderId, Integer teamId, Integer createUserId){
        return new Folder(null, name, FolderType.TEAM, teamId, createUserId, AccessPolicy.TEAM, createUserId, LocalDateTime.now(), null, null,
                parentFolderId, null, UUID.randomUUID(), FolderStatus.ACTIVE, null, null);
    }
}
