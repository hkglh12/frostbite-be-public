package com.uruz.frostbite.dto.folder.internal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(
        description = "폴더 상세정보"
)
public class FolderDetailsDraft {

    @Schema(description = "식별자 UUID")
    private UUID uuid;
    @Schema(description = "폴더 이름")
    private String name;
    @Schema(description = "폴더 구분[TEAM || PRIVATE]")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private FolderType folderType;
    @Schema(description = "소유자명[folderType->TEAM : 팀명 || PRIVATE : 소유자명")
    private Integer ownerId;
    @Schema(description = "생성자PK")
    private Integer createUserId;
    @Schema(description = "폴더 생성시각")
    @JsonFormat(pattern = "yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDateTime createTime;
    @Schema(description = "수정자PK")
    private Integer modifyUserId;
    @Schema(description = "폴더 수정시각")
    @JsonFormat(pattern = "yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDateTime modifyTime;
    @Schema(description = "랜더순서")
    private Integer renderSequence;
    @Schema(description = "부모폴더UUID")
    private UUID parentFolderUuid;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "폴더상태")
    private FolderStatus status;

}
