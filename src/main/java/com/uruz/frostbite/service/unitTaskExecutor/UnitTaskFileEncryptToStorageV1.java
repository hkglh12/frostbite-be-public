package com.uruz.frostbite.service.unitTaskExecutor;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.workflow.internal.FileWorkflowDraft;
import com.uruz.frostbite.enums.EncryptFormatVersion;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.service.DirectFileHandleComponent;
import com.uruz.frostbite.service.FileService;
import org.apache.commons.collections4.MapUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class UnitTaskFileEncryptToStorageV1 implements UnitTaskExecutor {

    private final UnitTask unitTask = UnitTask.UT_FILE_ENCRYPT_TO_STORAGE_V1;

    private final EncryptFormatVersion currentDefaultEncryptFormatVersion = EncryptFormatVersion.AES256GCM;
    // ASCII 코드 기준 FRST = 4byte
    private static final String FROST_BITE_MAGIC_CHARSET = "FRST";

    private final DirectFileHandleComponent directFileHandleComponent;
    private final FileService fileService;

    public UnitTaskFileEncryptToStorageV1(DirectFileHandleComponent directFileHandleComponent,
                                          FileService fileService){
        this.directFileHandleComponent = directFileHandleComponent;
        this.fileService = fileService;
    }
    @Override
    public UnitTask getUnitTask() {
        return this.unitTask;
    }

    @Override
    public FileWorkflowDraft process(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft) {
        LocalDateTime startedTime = LocalDateTime.now();

        fileService.markFileWorkflowRegisterStarted(request.getTargetId(), startedTime);

        UUID tempFileUuid = UUID.fromString(MapUtils.getString(draft.getRequestPayload(), "tempFileUuid"));
        FileTemp fileTemp = fileService.selectFileTempByUuid(tempFileUuid);

        Map<String, String> destInfo = null;

        UUID destinationUuid = UUID.randomUUID();
        String[] uuids = destinationUuid.toString().split("-");
        // 사용하게 될 파일명 직접 파싱
        String attemptFileName = uuids[4];
        // 저장 경로 resolve ->
        String persistedFilePath = directFileHandleComponent.resolvePersistFilePath(destinationUuid, attemptFileName);
        try{
            destInfo = directFileHandleComponent.encryptFileToPersistPath(tempFileUuid, persistedFilePath, attemptFileName, currentDefaultEncryptFormatVersion);
            draft.setApplicationTempFileUuid(fileTemp.getTargetFileUuid());
            draft.setCopySucceed(true);
            draft.setDestFileName(MapUtils.getString(destInfo, "destFileName"));
            draft.setDestFilePath(MapUtils.getString(destInfo, "destFilePath"));
            return draft;
        }catch (Exception e){
            e.printStackTrace();
            throw new GeneralCustomException("COPY STORAGE UNITTASK FAILED : " + e.getMessage());
        }
    }

    @Override
    public FileWorkflowDraft markOnSucceed(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft, LocalDateTime succeedTime) {
        String destFilePath = draft.getDestFilePath();
        String destFileName = draft.getDestFileName();
        fileService.markFileWorkflowRegisterSucceed(request.getTargetId(), succeedTime);
        fileService.completeRegistWorkflow(request.getTargetId(), succeedTime, destFilePath, destFileName);
        return null;
    }

    @Override
    public FileWorkflowDraft compensateOnFailure(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft, LocalDateTime failedTime, String errorMessage) {
        fileService.markFileWorkflowRegisterFailed(unitTask.getTargetId(), failedTime, errorMessage);
        return null;
    }

}
