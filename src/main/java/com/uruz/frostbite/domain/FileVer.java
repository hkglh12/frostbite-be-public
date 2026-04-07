package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileVer {

    /** 파일 변경이력 식별자 */
    private Long fileVerId;

    /** 파일 식별자 */
    private Long fileId;

    /** 파일명 */
    private String fileName;

    /** 파일크기 */
    private Long fileSize;

    /** 작업 설명 */
    private String operationDescription;

    /** 서버상 파일 최상위경로 */
    private String mountPoint;

    /** 서버상 파일경로 */
    private String filePath;

    /** 수정자 (user_id) */
    private Integer modifyUser;

    /** 수정시각 */
    private LocalDateTime modifyTime;
}
