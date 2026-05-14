package com.uruz.frostbite.dto.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Sub 폴더 부모변경 요청")
public class FolderMoveRequest {
    @NotNull
    @Schema(description = "신규 부모폴더 UUID (종착지 부모폴더UUID)")
    private UUID destinationFolderUuid;
}
