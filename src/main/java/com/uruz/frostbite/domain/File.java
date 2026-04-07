package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class File {

    /** 파일 식별자 */
    private Long fileId;

    /** 파일명 */
    private String fileName;

    /** 파일크기 */
    private Long fileSize;

    /** 부모 폴더 식별자 */
    private Long folderId;

    /** 서버상 파일 최상위경로 */
    private String mountPoint;

    /** 서버상 파일경로 */
    private String filePath;

    /** 생성자 (user_id) */
    private Integer createUser;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 수정자 (user_id) */
    private Integer modifyUser;

    /** 수정시각 */
    private LocalDateTime modifyTime;

    /** UUID */
    private UUID uuid;

    /** 삭제여부 */
    private Short isDelete;
}
