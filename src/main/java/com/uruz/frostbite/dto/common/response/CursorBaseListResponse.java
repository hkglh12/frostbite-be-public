package com.uruz.frostbite.dto.common.response;

import com.uruz.frostbite.dto.common.paging.CursorBasePagination;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(
        description = "Keybase (무한스크롤베이스) 리스트 응답"
)
@NoArgsConstructor
@AllArgsConstructor
public class CursorBaseListResponse<T> {
    @Schema(description = "응답 리스트")
    private List<T> contents;

    @Schema(description = "Cursor Pagination")
    private CursorBasePagination pagination;

    // TODO 정렬조건 고도화
}
