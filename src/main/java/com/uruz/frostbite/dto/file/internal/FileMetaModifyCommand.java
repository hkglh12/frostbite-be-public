package com.uruz.frostbite.dto.file.internal;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class FileMetaModifyCommand {

    private final UUID uuid;
    private final String fileName;
    private final Integer userId;
    private final LocalDateTime modifyTime;

    private FileMetaModifyCommand(
            UUID uuid,
            String fileName,
            Integer userId,
            LocalDateTime modifyTime
    ) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.userId = userId;
        this.modifyTime = modifyTime;
    }

    public static FileMetaModifyCommand create(
            UUID fileUuid,
            String fileName,
            Integer userId,
            LocalDateTime modifyTime
    ) {
        return new FileMetaModifyCommand(fileUuid, fileName, userId, modifyTime);
    }
}
