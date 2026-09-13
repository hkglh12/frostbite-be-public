package com.uruz.frostbite.dto.file;

import com.uruz.frostbite.enums.FileStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class FileMetaResponse {
    private UUID uuid;
    private String fileName;
    private UUID parentFolderUuid;
    private Long fileSize;
    private String fileExtension;
    private String mimeType;
    private String createUserName;
    private String modifyUserName;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    private FileStatus status;
}
