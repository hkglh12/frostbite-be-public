package com.uruz.frostbite.dto.file.internal;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class InternalFileMoveCommand {
    private final UUID uuid;
    private final Long originalFolderId;
    private final Long targetFolderId;
    private final Integer userId;
    private final LocalDateTime modifyTime;

    private InternalFileMoveCommand(
            UUID uuid,
            Long originalFolderId,
            Long targetFolderId,
            Integer userId,
            LocalDateTime modifyTime
    ) {
        this.uuid = uuid;
        this.originalFolderId = originalFolderId;
        this.targetFolderId = targetFolderId;
        this.userId = userId;
        this.modifyTime = modifyTime;
    }

    public static InternalFileMoveCommand create(
            UUID fileUuid,
            Long originalFolderId,
            Long targetFolderId,
            Integer modifyUserId,
            LocalDateTime currentTime
    ) {
        return new InternalFileMoveCommand(fileUuid, originalFolderId, targetFolderId, modifyUserId, currentTime);
    }
}
