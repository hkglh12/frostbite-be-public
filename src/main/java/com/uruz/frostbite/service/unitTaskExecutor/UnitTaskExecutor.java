package com.uruz.frostbite.service.unitTaskExecutor;

import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.workflow.internal.FileWorkflowDraft;
import com.uruz.frostbite.enums.workflow.UnitTask;

public interface UnitTaskExecutor {
    /**
     * Workflow의 단위작업을 직접 실행
     * UnitTask가 수행하려는 작업의 대상 도메인의 Service 객체를 통한
     * Database CRUD는 신중하게 사용하되,
     * Workflow 도메인 자체에 대한 수정은 지향해야 한다.
     * workflow 도메인 자체는 WorkflowProcessor가 핸들링해야 한다.
     * */
    UnitTask getUnitTask();
    FileWorkflowDraft process(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft);
    FileWorkflowDraft compensateOnFailure(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft);
}
