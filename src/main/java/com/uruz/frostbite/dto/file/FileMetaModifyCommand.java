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
public class FileMetaModifyCommand {
    @Schema(description = "파일 UUID")
    private UUID uuid;
    // 현시점에선 수정가능한게 이거 하나라서 NotNull검
    @NotNull
    @Schema(description = "파일명")
    private String fileName;

    @Schema(description = "폴더 UUID")
    private UUID folderUuid;


    @Schema(description = "요청자 PK")
    private Integer userId;
    @Schema(description = "요청시각")
    private LocalDateTime modifyTime;


    public FileMetaModifyCommand fillPathVariables(
            UUID fileUuid,
            UUID folderUuid){

        return new FileMetaModifyCommand(fileUuid, this.getFileName(), folderUuid, null, null);
    }


    public FileMetaModifyCommand fillRequired(
                                              Integer userId,
                                              LocalDateTime currentDateTime){

        return new FileMetaModifyCommand(this.getUuid(), this.getFileName(), this.getFolderUuid(), userId, currentDateTime);
    }
}
