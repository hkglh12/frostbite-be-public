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

    /** UUID */
    private UUID uuid;

    /** 폴더 식별자 */
    private Long folderId;

    /** 폴더명 */
    private String folderName;

    /** 폴더종류 (1: private, 2: team) */
    private FolderType folderType;

    /** 부모 폴더 식별자 */
    private Long parentFolderId;

    /** 폴더 자원 소유 객체 정보 */
    private Integer resourceScopeTeamId;
    private Integer resourceScopeUserId;
    /** 폴더 실제 관리자 (USER PK) */
    private Integer resourceOwnerId;
    /** 폴더 접근 정책 (USER || TEAM) */
    private AccessPolicy accessPolicy;

    private String folderIdPath;
    private String folderNamePath;

    /** 출력순서 */
    private Integer renderSequence;

    /** 삭제여부 */
    private FolderStatus status;

    /** 폴더 생성자 (user_id) */
    private Integer createUserId;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 수정자 (user_id) */
    private Integer modifyUserId;

    /** 수정시각 */
    private LocalDateTime modifyTime;

    private Integer deleteUserId;
    private LocalDateTime deleteTime;



    /** 회원가입 직후 entryFolder 생성*/

    public static Folder createPrivateEntryFolder(String name, Integer ownerId){
        if(name.trim().length()<3 || name.trim().length() > 512){
            throw new IllegalArgumentException("Name constraint violated");
        }

        return new Folder(UUID.randomUUID(),null, name, FolderType.PRIVATE, null , null, ownerId,
                ownerId, AccessPolicy.USER, null, null, 0, FolderStatus.ACTIVE,
                ownerId, LocalDateTime.now(), ownerId, LocalDateTime.now(), null, null);
    }

    public static Folder createTeamEntryFolder(String name, Integer teamId, Integer createUserId){
        if(name.trim().length()<3 || name.trim().length() > 512){
            throw new IllegalArgumentException("Name constraint violated");
        }

        return new Folder(UUID.randomUUID(),null, name, FolderType.TEAM, null ,teamId,null
                , createUserId, AccessPolicy.TEAM, null, null, 0, FolderStatus.ACTIVE,
                createUserId, LocalDateTime.now(), createUserId, LocalDateTime.now(), null, null);
    }

    /** 개인 루트폴더 생성용 엔티티 생성 */
    public static Folder createPrivateRootFolder(String name, Long parentFolderId, Integer ownerId){
        return new Folder( UUID.randomUUID(),null, name, FolderType.PRIVATE, parentFolderId,null,
                ownerId, ownerId, AccessPolicy.USER, null, null, null, FolderStatus.ACTIVE,
                ownerId, LocalDateTime.now(), null, null, null, null
                );
    }

    /** 팀 루트 폴더 생성용 엔티티 생성 */
    public static Folder createTeamRootFolder(String name, Long parentFolderId, Integer resourceScopeTeamId, Integer createUserId){
        return new Folder( UUID.randomUUID(),null, name, FolderType.TEAM, parentFolderId, resourceScopeTeamId, null,
                createUserId, AccessPolicy.TEAM, null, null,null, FolderStatus.ACTIVE,
                createUserId, LocalDateTime.now(), null, null, null, null
                );
    }

    /** 개인 서브 폴더 생성용 엔티티 생성 */
    public static Folder createPrivateSubFolder(String name, Long parentFolderId, Integer createUserId){
        return new Folder( UUID.randomUUID(),null, name, FolderType.PRIVATE, parentFolderId, null,
                createUserId, createUserId, AccessPolicy.USER, null, null, null, FolderStatus.ACTIVE,
                createUserId, LocalDateTime.now(), null, null, null, null
                );
    }

    /** 팀 서브폴더 생성용 엔티티 생성*/
    public static Folder createTeamSubFolder(String name, Long parentFolderId, Integer teamId, Integer createUserId){
        return new Folder( UUID.randomUUID(),null, name, FolderType.TEAM, parentFolderId, teamId, null,
                createUserId, AccessPolicy.TEAM, null, null,  null, FolderStatus.ACTIVE
                , createUserId, LocalDateTime.now(), null, null, null, null
                );
    }
}
