package com.uruz.frostbite.dto.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(
        description = "간략 폴더 리스트 베이스 응답"
)
@AllArgsConstructor
@Deprecated(forRemoval = true)
public class SimpleFolderListKeysetResponse{
    private List<SimpleFolder> folders;
    private Boolean hasNext;
    private UUID lastFolderUuid;
}

