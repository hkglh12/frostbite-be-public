package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.FileStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class File {

    /** 파일 식별자 */
    private Long fileId;

    /** 부모 폴더 식별자 */
    private Long folderId;

    /** 파일 UUID */
    private UUID uuid;

    /** 원본 파일명 */
    private String originalFileName;

    /** 사용자가 지정한 파일명 */
    private String fileName;

    /** 사용자가 지정한 부가설명*/
    private String description;

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

    /** SHA-256 체크섬 */
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

    /** 파일 상태 */
    private FileStatus status;

    @Builder
    private File(
            Long fileId,
            Long folderId,
            UUID fileUuid,
            String originalFileName,
            String fileName,
            String description,
            Long fileSize,
            String fileExtension,
            String mimeType,
            Integer storageMountPointId,
            String storageFilePath,
            String storageFileName,
            String checksumSha256,
            Integer createUserId,
            LocalDateTime createTime,
            Integer modifyUserId,
            LocalDateTime modifyTime,
            Integer deleteUserId,
            LocalDateTime deleteTime,
            FileStatus status
    ) {
        this.fileId = fileId;
        this.folderId = folderId;
        this.uuid = fileUuid;
        this.originalFileName = originalFileName;
        this.fileName = fileName;
        this.description = description;
        this.fileSize = fileSize;
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
        this.storageMountPointId = storageMountPointId;
        this.storageFilePath = storageFilePath;
        this.storageFileName = storageFileName;
        this.checksumSha256 = checksumSha256;
        this.createUserId = createUserId;
        this.createTime = createTime;
        this.modifyUserId = modifyUserId;
        this.modifyTime = modifyTime;
        this.deleteUserId = deleteUserId;
        this.deleteTime = deleteTime;
        this.status = status;
    }

    public static File createInitial(
            Long folderId,
            UUID fileUuid,
            String originalFileName,
            String fileName,
            String description,
            Long fileSize,
            Integer createUserId,
            LocalDateTime createTime
    ) {
        return File.builder()
                .folderId(folderId)
                .fileUuid(fileUuid)
                .originalFileName(originalFileName)
                .fileName(fileName)
                .description(description)
                .fileSize(fileSize)
                .createUserId(createUserId)
                .createTime(createTime)
                .status(FileStatus.UNREGISTERED)
                .build();
    }

    public void completeRegister(
            String mimeType,
            Integer storageMountPointId,
            String storageFilePath,
            String storageFileName,
            String checksumSha256,
            Integer modifyUserId
    ) {
        this.mimeType = mimeType;
        this.storageMountPointId = storageMountPointId;
        this.storageFilePath = storageFilePath;
        this.storageFileName = storageFileName;
        this.checksumSha256 = checksumSha256;
        this.modifyUserId = modifyUserId;
        this.modifyTime = LocalDateTime.now();
    }

    public void softDelete(Integer deleteUserId) {
        this.deleteUserId = deleteUserId;
        this.deleteTime = LocalDateTime.now();
        this.status = FileStatus.SOFT_DELETED;
    }
}