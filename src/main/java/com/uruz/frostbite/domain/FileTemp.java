package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
/** 임시파일 정보 */
public class FileTemp {
    private UUID targetFileUuid;
    private String originalFileName;
    private Long fileSize;
    private Integer createUserId;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private FileStatus status;

    /** 임시파일 정보 집합 생성 */
    public static FileTemp createPreparedTempFile(
            UUID targetFileUuid,
            String originalFileName,
            Long fileSize,
            Integer createUserId,
            Long ttlSeconds
    ) {
        LocalDateTime now = LocalDateTime.now();

        FileTemp fileTemp = new FileTemp();
        fileTemp.targetFileUuid = targetFileUuid;
        fileTemp.originalFileName = originalFileName;
        fileTemp.fileSize = fileSize;
        fileTemp.createUserId = createUserId;
        fileTemp.createTime = now;
        fileTemp.expireTime = now.plusSeconds(ttlSeconds);
        fileTemp.status = FileStatus.UNREGISTERED;

        return fileTemp;
    }
}
