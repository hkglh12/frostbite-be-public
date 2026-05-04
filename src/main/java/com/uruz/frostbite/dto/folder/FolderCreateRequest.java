package com.uruz.frostbite.dto.folder;

import com.uruz.frostbite.enums.FolderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "비 ROOT 폴더 생성 요청")
public class FolderCreateRequest {
    @NotEmpty
    @Size(max = 256)
    @Schema(description = "폴더 이름")
    private String name;

    @NotNull
    @Schema(description = "부모폴더 UUID")
    private UUID parentFolderUuid;

    @NotNull
    @Schema(description = "폴더 타입")
    private FolderType folderType;
}
