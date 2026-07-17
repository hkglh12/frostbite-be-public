package com.uruz.frostbite.service.unitTaskExecutor;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.workflow.internal.FileWorkflowDraft;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.service.DirectFileHandleComponent;
import com.uruz.frostbite.service.FileService;
import com.uruz.frostbite.service.UnitTaskService;
import com.uruz.frostbite.service.WorkflowService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class UnitTaskCopyStorage implements UnitTaskExecutor{
    private final UnitTask unitTask = UnitTask.UT_FILE_COPY_STORAGE;

    private final DirectFileHandleComponent directFileHandleComponent;
    private final FileService fileService;
    private final UnitTaskService unitTaskService;


    private final WorkflowService workflowService;

    public UnitTaskCopyStorage(DirectFileHandleComponent directFileHandleComponent, FileService fileService,
                               WorkflowService workflowService,
                               UnitTaskService unitTaskService){
        this.directFileHandleComponent = directFileHandleComponent;
        this.fileService = fileService;
        this.workflowService = workflowService;
        this.unitTaskService = unitTaskService;
    }
    @Override
    public UnitTask getUnitTask() {
        return this.unitTask;
    }

    @Override
    public FileWorkflowDraft process(UnitTaskRequest unitTaskRequest, WorkflowRequest request, FileWorkflowDraft draft) {
        /**
         * 실제 파일 이동
         *
         *
         * */
        LocalDateTime startedTime = LocalDateTime.now();
        fileService.markFileWorkflowRegistStarted(request.getTargetId(), startedTime);

        UUID tempFileUuid = UUID.fromString(MapUtils.getString(draft.getRequestPayload(), "tempFileUuid"));
        FileTemp fileTemp = fileService.selectFileTempByUuid(tempFileUuid);

        Map<String, String> destInfo = null;

        UUID destinationUuid = UUID.randomUUID();
        String[] uuids = destinationUuid.toString().split("-");
        // 사용하게 될 파일명 직접 파싱
        String attemptFileName = uuids[4];
        // 저장 경로 resolve ->
        String persistedFilePath = directFileHandleComponent.resolvePersistFilePath(destinationUuid, attemptFileName);

        // 파일 이동
        try {
            destInfo = directFileHandleComponent.transferFileToPersistPath(tempFileUuid, persistedFilePath, attemptFileName);
            draft.setApplicationTempFileUuid(fileTemp.getTargetFileUuid());
            draft.setCopySucceed(true);
            draft.setDestFileName(MapUtils.getString(destInfo, "destFileName"));
            draft.setDestFilePath(MapUtils.getString(destInfo, "destFilePath"));
            return draft;
        } catch (Exception e) {
            throw new GeneralCustomException("COPY STORAGE UNITTASK FAILED : " + e.getMessage());

        }
    }

    @Override
    public FileWorkflowDraft markOnSucceed(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft, LocalDateTime succeedTime){
        String destFilePath = draft.getDestFilePath();
        String destFileName = draft.getDestFileName();
        fileService.markFileWorkflowRegistSucceed(request.getTargetId(), succeedTime);
        fileService.completeRegistWorkflow(request.getTargetId(), succeedTime, destFilePath, destFileName);
        return null;
    }

    @Override
    public FileWorkflowDraft compensateOnFailure(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft, LocalDateTime failedTime, String errorMessage) {
        fileService.markFileWorkflowRegistFailed(unitTask.getTargetId(), failedTime, errorMessage);
        return null;
    }


}
