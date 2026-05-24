package com.uruz.frostbite.dto.folder.internal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.AccessPolicy;
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
@NoArgsConstructor
@AllArgsConstructor
public class FolderDetailsDraft {

    @Schema(description = "식별자 UUID")
    private UUID uuid;
    @Schema(description = "폴더 이름")
    private String name;
    @Schema(description = "폴더 구분")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private FolderType folderType;
    @Schema(description = "부모폴더UUID")
    private UUID parentFolderUuid;
    @Schema(description = "소유자 식별자")
    private Integer resourceScopeTeamId;
    private Integer resourceScopeUserId;
    @Schema(description = "실 관리 사용자 식별자")
    private Integer resourceOwnerId;
    @Schema(description = "접근정책")
    private AccessPolicy accessPolicy;

    private String folderIdPath;
    private String folderNamePath;

    @Schema(description = "랜더순서")
    private Integer renderSequence;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "폴더상태")
    private FolderStatus status;

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
    @Schema(description = "삭제자PK")
    private Integer deleteUserId;
    @Schema(description = "폴더 삭제시각")
    @JsonFormat(pattern = "yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDateTime deleteTime;



}
