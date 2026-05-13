package com.uruz.frostbite.dto.folder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.FolderType;
import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.media.DependentRequired;
import io.swagger.v3.oas.annotations.media.Schema;

@Deprecated(forRemoval = true)
public class PrivateRootFolderOffsetCondition {
    @Schema(description = "폴더 구분[TEAM || PRIVATE]")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Nullable
    private FolderType folderType;

}
