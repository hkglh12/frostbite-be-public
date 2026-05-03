package com.uruz.frostbite.dto.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "팀 루트 폴더 생성 요청")
public class TeamRootFolderCreateRequest {
    @NotEmpty
    @Size(max=256)
    @Schema(description = "팀 이름")
    private String name;

    @NotEmpty
    @Schema(description = "팀 UUID")
    private UUID teamUuid;
}
