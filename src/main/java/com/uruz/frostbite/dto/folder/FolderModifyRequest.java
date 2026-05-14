package com.uruz.frostbite.dto.folder;

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
@Schema(description = "Sub 폴더 부모변경 요청")
public class FolderModifyRequest {
    @NotEmpty
    @Size(max = 256)
    @Schema(description = "폴더 이름")
    private String name;

}
