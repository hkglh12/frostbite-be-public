package com.uruz.frostbite.dto.folder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.dto.folder.internal.FolderDetailsDraft;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(
        description = "폴더 상세정보"
)
@AllArgsConstructor
public class FolderDetailsResponse {

    @Schema(description = "식별자 UUID")
    private UUID uuid;
    @Schema(description = "폴더 이름")
    private String name;
    @Schema(description = "폴더 구분[TEAM || PRIVATE]")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private FolderType folderType;
    @Schema(description = "소유자명[folderType->TEAM : 팀명 || PRIVATE : 소유자명")
    private String ownerName;
    @Schema(description = "생성자이름")
    private String createUserName;
    @Schema(description = "폴더 생성시각")
    @JsonFormat(pattern = "yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDateTime createTime;
    @Schema(description = "수정자이름")
    private String modifyUserName;
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


    public static FolderDetailsResponse from(FolderDetailsDraft draft, String ownerName, String createUserName, String modifyUserName){
        return new FolderDetailsResponse(draft.getUuid(), draft.getName(), draft.getFolderType(),
                ownerName, createUserName, draft.getCreateTime(), modifyUserName, draft.getModifyTime(),
                draft.getRenderSequence(), draft.getParentFolderUuid(), draft.getStatus());
    }
}
