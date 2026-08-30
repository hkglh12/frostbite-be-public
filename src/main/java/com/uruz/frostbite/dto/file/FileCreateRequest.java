package com.uruz.frostbite.dto.file;

import com.uruz.frostbite.enums.workflow.Workflow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@Schema(description = "파일 생성 요청")
public class FileCreateRequest {

    @NotNull
    @Schema(description = "임시 파일 업로드 API로 수령한 UUID")
    private UUID tempFileUuid;

    //@NotNull
    //@Schema(description = "부모 폴더 UUID")
    //private UUID folderUuid;

    //@Schema(description = "별도 파일명")
    //String fileName;

    @Schema(description = "사용자 지정 추가 설명")
    String description;

    @Schema(description = "요청 WorkflowName")
    Workflow workflowName = Workflow.WF_FILE_REGISTER_ONLY;
}
