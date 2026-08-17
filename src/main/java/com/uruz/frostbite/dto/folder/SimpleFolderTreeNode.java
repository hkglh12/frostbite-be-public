package com.uruz.frostbite.dto.folder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Schema(
        description = "폴더 간략정보"
)
@NoArgsConstructor
@AllArgsConstructor
public class SimpleFolderTreeNode extends SimpleFolder{

    @Schema(description = "식별자 UUID")
    private boolean hasChild;

}

