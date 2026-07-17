package com.uruz.frostbite.dto.workflow.internal;

import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.enums.workflow.WorkflowDomainType;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileWorkflowDraft {
    /*
        - 최초 설계는 Workflow의 종류 별로 Draft를 만들어서 관리하려 했음.
            - FileWorkflowMoveOnlyDraft
            - FileWorkflowProvisionDraft
            - FileWorkflowEncryptionOnlyDraft
        - 이후 UnitTaskExecutor<T extends FilreWorkflowDraft> 라는 방식으로 UnitTask가 자신이 동작하는 워크플로우 흐름에 대해
          일정부분 인지할 수 있으면서도 타입 안정성을 챙기려 함.

        - 현재 구조는 Map<UnitTask, UnitTaskExecutor>와 같이 Executor를 등록 후 동적으로 실행중
        - 이 경우, Executor가 오히려 어떤 draft 타입을 처리하는지 알 수 없음.
        - 즉, UnitTaskExecutor<MoveOnlyDraft>, UnitTaskExecutor<ProvisionDraft> 를 모두 Map에 보관하게됨.
        - 이예따라 Processor 입장에서는 executor.process(draft) 호출 시 타입안정성 보장이 불능, Java Generic WildCard(PECS) 제약으로 인해 컴파일 어려워짐
        - 따라서, Phase1에서는 구조 단순화를 위해 FileWorkflowDraft 를 단일화함.
    * */

    private Map<String, Object> requestPayload;
    private WorkflowDomainType workflowDomainType;
    private long targetId;
    private int createUserId;

    private UUID applicationTempFileUuid;

    private boolean copySucceed;
    private String destFilePath;
    private String destFileName;

    private String storageFilePath;
    private boolean removeSucceed;

    public static FileWorkflowDraft createWorkflowDraft(WorkflowRequest request, Map<String, Object> requestPayload){
        return FileWorkflowDraft.builder()
                .requestPayload(requestPayload)
                .workflowDomainType(request.getWorkflowDomainType())
                .targetId(request.getTargetId())
                .createUserId(request.getCreateUserId())
                .build();
    }
}