package com.uruz.frostbite.domain;

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
@AllArgsConstructor
public class Folder {

    /** 폴더 식별자 */
    private Long folderId;

    /** 폴더명 */
    private String name;


    /** 피종속 자원 식별자 */
    private Integer ownerId;

    /** 폴더 생성자 (user_id) */
    private Integer createUser;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 수정자 (user_id) */
    private Integer modifyUser;

    /** 수정시각 */
    private LocalDateTime modifyTime;

    /** 폴더종류 (1: private, 2: team) */
    private FolderType folderType;

    /** 부모 폴더 식별자 */
    private Long parentId;

    /** 출력순서 */
    private Integer renderSequence;

    /** UUID */
    private UUID uuid;

    /** 삭제여부 */
    private FolderStatus status;

    /** 특정 케이스에 대해 ROOT 폴더 생성*/
    public static Folder createRoot(String name, Integer ownerId,
                                       FolderType folderType){
        if(name.trim().length()<3 || name.trim().length() > 512){
            throw new IllegalArgumentException("Name constraint violated");
        }

        return new Folder(null, name, ownerId, ownerId, LocalDateTime.now(),
                ownerId, LocalDateTime.now(), folderType, 0L, 0, UUID.randomUUID(), FolderStatus.ACTIVE);
    }
}
