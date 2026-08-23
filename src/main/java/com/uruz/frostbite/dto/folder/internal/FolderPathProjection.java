package com.uruz.frostbite.dto.folder.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@Schema(
        description = "폴더 경로정보"
)
@NoArgsConstructor
@AllArgsConstructor
public class FolderPathProjection {
    String folderNamePath;
    String folderIdPath;
}
