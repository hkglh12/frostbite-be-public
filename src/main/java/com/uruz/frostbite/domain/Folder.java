package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Folder {

    /** 폴더 식별자 */
    private Long folderId;

    /** 폴더명 */
    private String name;

    /** 종속종류 (team || private) */
    private String ownerType;

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
    private Integer folderType;

    /** 부모 폴더 식별자 */
    private Long parentId;

    /** 출력순서 */
    private Integer renderSequence;

    /** UUID */
    private UUID uuid;

    /** 삭제여부 */
    private Short isDelete;
}
