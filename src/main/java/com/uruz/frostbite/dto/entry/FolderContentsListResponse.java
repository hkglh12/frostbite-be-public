package com.uruz.frostbite.dto.entry;

import com.uruz.frostbite.dto.common.paging.OffsetBasePagination;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@Setter
@Schema(description = "폴더 내 컨텐츠")
@Deprecated(forRemoval = true)
public class FolderContentsListResponse {

    @Schema(description = "컨텐츠 리스트")
    List<FolderContents> contents;
    @Schema(description = "페이지네이션 정보")
    OffsetBasePagination offsetBasePagination;

    public FolderContentsListResponse(List<FolderContents> contents, OffsetBasePagination offsetBasePagination){
        this.contents = contents;
        this.offsetBasePagination = offsetBasePagination;
    }
}
