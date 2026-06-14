package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.FileStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class FileVer {

    /** 파일 변경이력 식별자 */
    private Long fileVerId;

    /** 원본 파일 식별자 */
    private Long fileId;

    /** 버전 순번 */
    private Integer versionSequence;

    /** 부모 폴더 식별자 */
    private Long folderId;

    /** 파일 UUID */
    private UUID fileUuid;

    /** 원본 파일명 */
    private String originalFileName;

    /** 파일 크기 */
    private Long fileSize;

    /** 파일 확장자 */
    private String fileExtension;

    /** MIME Type */
    private String mimeType;

    /** 저장소 마운트포인트 식별자 */
    private Integer storageMountPointId;

    /** 저장소 내 상대 경로 */
    private String storageFilePath;

    /** 저장소 내 실제 파일명 */
    private String storageFileName;

    /** SHA256 체크섬 */
    private String checksumSha256;

    /** 생성자 */
    private Integer createUserId;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 수정자 */
    private Integer modifyUserId;

    /** 수정시각 */
    private LocalDateTime modifyTime;

    /** 삭제자 */
    private Integer deleteUserId;

    /** 삭제시각 */
    private LocalDateTime deleteTime;

    /** 상태값 */
    private FileStatus status;
}