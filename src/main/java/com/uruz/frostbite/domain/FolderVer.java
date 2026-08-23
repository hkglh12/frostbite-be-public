package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FolderVer {

    /** 폴더 버전 식별자 */
    private Long folderVerId;

    /** 폴더 식별자 */
    private Integer folderId;

    /** 폴더명 */
    private String folderName;

    /** 수정자 (user_id) */
    private Integer modifyUser;

    /** 수정시각 */
    private LocalDateTime modifyTime;
}
