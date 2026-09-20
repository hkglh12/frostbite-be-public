package com.uruz.frostbite.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class FileMoveRequest {

    @NotNull
    @Schema(description = "대상 폴더 UUID")
    private UUID targetFolderUuid;
}
