package com.uruz.frostbite.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
//@RequiredArgsConstructor
@AllArgsConstructor
public class FileMoveCommand {
    @Schema(description = "파일 UUID")
    private UUID uuid;
    @Schema(description = "원본 폴더 UUID")
    private UUID originalFolderUuid;
    @NotNull
    @Schema(description = "대상 폴더 UUID")
    private UUID targetFolderUuid;

    @Schema(description = "요청자 PK")
    private Integer userId;
    @Schema(description = "요청시각")
    private LocalDateTime modifyTime;

    public FileMoveCommand fillPathVariables(UUID fileUuid, UUID originalFolderUuid){
        return new FileMoveCommand(fileUuid, originalFolderUuid, this.getTargetFolderUuid(), null, null);
    }

    public FileMoveCommand fillRequired(
                                              Integer userId,
                                              LocalDateTime currentDateTime){

        return new FileMoveCommand(this.getUuid(), this.getOriginalFolderUuid(), this.getTargetFolderUuid(), userId, currentDateTime);
    }
}
