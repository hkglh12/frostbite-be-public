package com.uruz.frostbite.dto.folder.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class FolderModifyCommand {
    /**새 폴더 이름*/
    private String name;
    private UUID folderUuid;
}
