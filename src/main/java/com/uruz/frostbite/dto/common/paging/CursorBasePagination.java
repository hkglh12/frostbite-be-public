package com.uruz.frostbite.dto.common.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CursorBasePagination {

    @Schema(description = "마지막 구분자")
    private UUID lastUuid;
    @Schema(description = "다음 구분자")
    private UUID nextUuid;
    @Schema(description = "후속 데이터 존재여부")
    private Boolean hasNext;
    @Schema(description = "LIMIT")
    private int limit;

}
