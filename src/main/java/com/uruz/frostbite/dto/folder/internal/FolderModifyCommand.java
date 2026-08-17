package com.uruz.frostbite.dto.folder.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class FolderModifyCommand {
    /**새 폴더 이름*/
    private String folderName;
    private UUID folderUuid;
}
