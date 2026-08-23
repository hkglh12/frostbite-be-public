package com.uruz.frostbite.dto.folder.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(
        description = "폴더 경로 조회 조건"
)
@NoArgsConstructor
@AllArgsConstructor
public class FolderPathCondition {

    UUID folderUuid;
    Long folderId;

}
