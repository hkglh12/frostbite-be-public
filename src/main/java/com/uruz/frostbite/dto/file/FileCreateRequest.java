package com.uruz.frostbite.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "파일 생성 요청")
public class FileCreateRequest {

    @NotNull
    @Schema(description = "임시 파일 업로드 API로붙 수령한 UUID")
    private UUID tempFileUuid;

    @NotNull
    @Schema(description = "부모 폴더 UUID")
    private UUID folderUuid;

    @Schema(description = "별도 파일명")
    String fileName;

    @Schema(description = "사용자 지정 추가 설명")
    String descrption;
}
