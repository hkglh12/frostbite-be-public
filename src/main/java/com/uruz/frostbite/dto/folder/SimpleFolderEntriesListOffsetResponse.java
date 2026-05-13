package com.uruz.frostbite.dto.folder;

import com.uruz.frostbite.dto.file.SimpleFileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(
        description = "폴더 간략정보"
)
@AllArgsConstructor
@NoArgsConstructor
public class SimpleFolderEntriesListOffsetResponse {

    private List<SimpleFolderResponse> folders;
    private List<SimpleFileResponse> files;
    private Integer folderTotal;
    private Integer fileTotal;
    private Integer currentFolderMax;
    private Integer currentFileMax;

}

