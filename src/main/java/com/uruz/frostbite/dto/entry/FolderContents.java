package com.uruz.frostbite.dto.entry;

import com.uruz.frostbite.enums.entry.FolderContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@Schema(description = "폴더 내 컨텐츠")
public class FolderContents {

    @Schema(description = "객체 UUID")
    private UUID uuid;

    @Schema(description = "부모 폴더 UUID")
    private UUID parentFolderUuid;

    @Schema(description = "엔트리 명")
    private String entryName;

    @Schema(description = "엔트리 종류 [1:폴더 || 2:파일]")
    private FolderContentType folderContentType;

    @Schema(description = "entry 생성시각")
    private LocalDateTime createTime;
}
