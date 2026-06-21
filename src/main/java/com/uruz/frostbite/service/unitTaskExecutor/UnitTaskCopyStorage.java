package com.uruz.frostbite.service.unitTaskExecutor;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.workflow.internal.FileWorkflowDraft;
import com.uruz.frostbite.enums.WorkflowStatus;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.service.DirectFileHandleComponent;
import com.uruz.frostbite.service.FileService;
import com.uruz.frostbite.service.WorkflowService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class UnitTaskCopyStorage implements UnitTaskExecutor{
    private final UnitTask unitTask = UnitTask.UT_FILE_COPY_STORAGE;

    private final DirectFileHandleComponent directFileHandleComponent;
    private final FileService fileService;


    private final WorkflowService workflowService;

    public UnitTaskCopyStorage(DirectFileHandleComponent directFileHandleComponent, FileService fileService,
                               WorkflowService workflowService){
        this.directFileHandleComponent = directFileHandleComponent;
        this.fileService = fileService;
        this.workflowService = workflowService;
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
        // unitTask 작업시작 정보는 직접 UnitTask에서
        workflowService.insertCurrentUnitTask(unitTaskRequest);
        workflowService.markUnitTaskAsProcessing(unitTaskRequest.getUnitTaskRequestId(), startedTime);

        fileService.markFileWorkflowRegistStarted(request.getTargetId(), startedTime);
        UUID tempFileUuid = UUID.fromString(MapUtils.getString(draft.getRequestPayload(), "tempFileUuid"));
        FileTemp fileTemp = fileService.selectFileTempByUuid(tempFileUuid);
        Map<String, String> destInfo = null;
        UUID destinationUuid = UUID.randomUUID();
        String[] uuids = destinationUuid.toString().split("-");
        String attemptFileName = uuids[4];
        // 저장 경로 resolve
        String persistedFilePath =
                directFileHandleComponent.resolvePersistedFilePath(destinationUuid.toString().replace("-" + attemptFileName, "").replace("-", "/"));

        // 파일 이동
        try {
            destInfo = directFileHandleComponent.transferFileToPersistPath(tempFileUuid, persistedFilePath, attemptFileName);
            draft.setCopySucceed(true);
            draft.setDestFileName(MapUtils.getString(destInfo, "destFileName"));
            draft.setDestFilePath(MapUtils.getString(destInfo, "destFilePath"));

            LocalDateTime succeedTime = LocalDateTime.now();
            // 성공 기록
            workflowService.markUnitTaskToSucceed(unitTaskRequest.getUnitTaskRequestId(), succeedTime);
            fileService.markFileWorkflowRegistSucceed(request.getTargetId(), succeedTime);
            fileService.completeRegistWorkflow(request.getTargetId(), succeedTime, destInfo);

            return draft;
        } catch (Exception e) {
            LocalDateTime failedTime = LocalDateTime.now();
            // 파일 이동 실패 -> DB UnitTask 실패기록
            workflowService.markUnitTaskToFailedFinal(unitTaskRequest.getUnitTaskRequestId(), failedTime);
            fileService.markFileWorkflowRegistFailed(request.getTargetId(), failedTime, e.getMessage());

            throw new GeneralCustomException("UNITTASK FAILED");

            // 실패시에는 파일 정보 갱신 안함
            //fileService.completeRegistWorkflow(request.getTargetId(), failedTime, destInfo);
        /*
        // 파일 등록 실패 갱신
        workflowService.updateFileRegistFailed(
                LocalDateTime.now(),
                request.getTargetId(),
                WorkflowStatus.FAILED_FINAL
        );

         */
        }
    }


    @Override
    public FileWorkflowDraft compensateOnFailure(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft) {
        return null;
    }


}
