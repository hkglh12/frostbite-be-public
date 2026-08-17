package com.uruz.frostbite.dto.common.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OffsetBasePagination {

    @Schema(description = "전체 개수")
    public int totalCount;
    @Schema(description = "OFFSET")
    public int offset;
    @Schema(description = "LIMIT")
    public int limit;

    public OffsetBasePagination(int totalCount, int offset, int limit){
        this.totalCount = totalCount;
        this.offset = offset;
        this.limit = limit;
    }
}
