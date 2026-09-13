package com.uruz.frostbite.dto.file.internal;

import com.uruz.frostbite.dto.file.FileMoveCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
//@RequiredArgsConstructor
@AllArgsConstructor
public class InternalFileMoveCommand {
    @NotNull
    @Schema(description = "파일 UUID")
    private UUID uuid;
    @NotNull
    @Schema(description = "원본 폴더 UUID")
    private Long originalFolderId;
    @NotNull
    @Schema(description = "대상 폴더 UUID")
    private Long targetFolderId;

    @Schema(description = "요청자 PK")
    private Integer userId;
    @Schema(description = "요청시각")
    private LocalDateTime modifyTime;



    public static InternalFileMoveCommand convertInteralDto(FileMoveCommand command, Long originalFolderId, Long targetFolderId, Integer modifyUserId, LocalDateTime currentTime){
        return new InternalFileMoveCommand(command.getUuid(), originalFolderId, targetFolderId, modifyUserId, currentTime);
    }
}
