package com.uruz.frostbite.dto.workflow.internal;

import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.enums.workflow.WorkflowDomainType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileWorkflowDraft {

    /**
     * Draft를 Workflow 별로 분리하려고 했음.
     *
     * 원래 의도:
     *  - FileWorkflowMoveOnlyDraft
     *  - FileWorkflowProvisionDraft
     *  - ...
     *
     * 와 같이 Draft를 세분화하고,
     *
     * UnitTaskExecutor<T extends FileWorkflowDraft>
     *
     * 형태로 타입 안정성을 확보하려고 했음.
     *
     * 하지만 현재 구조는
     *
     * Map<UnitTask, UnitTaskExecutor>
     *
     * 에 Executor를 보관 후 동적으로 조회하여 실행하는 구조임.
     *
     * 이 경우 조회 시점에는 Executor가 실제로 어떤 Draft 타입을
     * 처리하는지 알 수 없음.
     *
     * 예:
     *
     * UnitTaskExecutor<MoveOnlyDraft>
     * UnitTaskExecutor<ProvisionDraft>
     *
     * 를 모두 동일 Map에 보관하게 됨.
     *
     * 따라서 Processor 입장에서는
     *
     * executor.process(draft)
     *
     * 호출 시 Draft 타입 안전성을 보장할 수 없으며,
     * Java Generic Wildcard(PECS) 제약으로 인해 컴파일이 어려워짐.
     *
     * Phase1에서는 구조 단순화를 위해
     * FileWorkflowDraft 단일 객체를 사용하고,
     * 필요한 값을 누적하는 Draft Buffer 형태로 운영함.
     *
     * 추후 Workflow Engine 분리 또는 Executor 타입 체계 재설계 시
     * Draft 세분화를 재검토 가능.
     */

    private Map<String, Object> requestPayload;
    private WorkflowDomainType workflowDomainType;
    private long targetId;
    private int createUserId;

    private String applicationTempFilePath;

    private boolean copySucceed;
    private String destFilePath;
    private String destFileName;

    private String storageFilePath;
    private boolean removeSucceed;

    public static FileWorkflowDraft createInitialForMoveOnly(WorkflowRequest request, Map<String, Object> requestPayload){
        return FileWorkflowDraft.builder()
                .requestPayload(requestPayload)
                .workflowDomainType(request.getWorkflowDomainType())
                .targetId(request.getTargetId())
                .createUserId(request.getCreateUserId())
                .build();
    }
}