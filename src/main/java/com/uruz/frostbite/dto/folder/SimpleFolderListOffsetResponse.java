package com.uruz.frostbite.dto.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(
        description = "폴더 간략정보"
)
@AllArgsConstructor
public class SimpleFolderListOffsetResponse {

    private List<SimpleFolder> folders;
    private Integer total;
    private Integer currentMax;

}

