package com.uruz.frostbite.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileMetaModifyRequest {

    @NotNull
    @Schema(description = "파일명")
    private String fileName;
}
