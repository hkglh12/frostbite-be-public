package com.uruz.frostbite.dto.common.response;

import com.uruz.frostbite.dto.common.paging.OffsetBasePagination;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@Setter
@Schema(description = "OffsetBase (페이지네이션) 리스트 응답")
public class OffsetBaseListResponse<T> {

    @Schema(description = "응답 리스트")
    List<T> contents;
    @Schema(description = "페이지네이션 정보")
    OffsetBasePagination pagination;

    public OffsetBaseListResponse(List<T> contents, OffsetBasePagination pagination){
        this.contents = contents;
        this.pagination = pagination;
    }
}
