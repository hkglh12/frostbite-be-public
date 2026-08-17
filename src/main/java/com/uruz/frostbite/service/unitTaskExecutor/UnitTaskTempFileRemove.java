package com.uruz.frostbite.service.unitTaskExecutor;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.customException.InternalProcessingException;
import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.workflow.internal.FileWorkflowDraft;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.service.DirectFileHandleComponent;
import com.uruz.frostbite.service.FileService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Component
@Slf4j
/** 등록 후, 임시파일 삭제 처리*/
public class UnitTaskTempFileRemove implements UnitTaskExecutor{
    // 주의사항 -> unitTaskTempFileRemove ; 임시파일 삭제 실패로 인해 파일 전체 사용 불능은 조금 가혹한 정책으로 판단.
    // 따라서, 임시파일 삭제 시 -> 로깅만 남겨두고, 추후 처리방안 Phase2/3에서 처리
    // UnitTask 자체는 실패로 기록하되, 워크플로우와 파일 활성화는 정상 처리

    // RemoveTempFile UnitTask는 현재 설계상 "파일 등록 흐름" 에서 반드시 워크플로우의 마지막 UnitTask여야함.
    private UnitTask unitTask = UnitTask.UT_FILE_REMOVE_TEMP;
    private final FileService fileService;
    private final DirectFileHandleComponent directFileHandleComponent;

    public UnitTaskTempFileRemove(FileService fileService, DirectFileHandleComponent directFileHandleComponent){
        this.fileService = fileService;
        this.directFileHandleComponent = directFileHandleComponent;
    }
    @Override
    public UnitTask getUnitTask() {
        return this.unitTask;
    }

    @Override
    public FileWorkflowDraft process(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft) {
        String tempFilePath = directFileHandleComponent.resolveApplicationTempFilePathByUuid(draft.getApplicationTempFileUuid()).toString();
        if(StringUtils.isEmpty(tempFilePath)){
            throw new InternalProcessingException("TEMP FILE PATH IS NULL");
        }
        if(!Files.exists(Path.of(tempFilePath))) {
            throw new InternalProcessingException("TEMP FILE NOT EXISTS");
        }
        try{
            directFileHandleComponent.deleteTempFile(draft.getApplicationTempFileUuid());
            draft.setRemoveSucceed(true);
        }catch(IOException IOE){
            draft.setRemoveSucceed(false);
            IOE.printStackTrace();
            throw new InternalProcessingException("IO EXCEPTION, File Remove Failed");
        }

        return draft;
    }

    @Override
    public FileWorkflowDraft markOnSucceed(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft, LocalDateTime succeedTime){
        fileService.markFileActive(request.getTargetId());
        return null;
    }

    @Override
    public FileWorkflowDraft compensateOnFailure(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft, LocalDateTime failedTime, String errorMessage){
        fileService.markFileWorkflowRegisterFailed(request.getTargetId(), LocalDateTime.now(), "FILE REMOVE FAILED");

        return null;
    }
}
