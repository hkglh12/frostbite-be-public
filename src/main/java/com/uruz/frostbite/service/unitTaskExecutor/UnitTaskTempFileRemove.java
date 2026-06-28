package com.uruz.frostbite.service.unitTaskExecutor;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.workflow.internal.FileWorkflowDraft;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.service.DirectFileHandleComponent;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class UnitTaskTempFileRemove implements UnitTaskExecutor{
    private UnitTask unitTask = UnitTask.UT_FILE_REMOVE_TEMP;

    private final DirectFileHandleComponent directFileHandleComponent;

    public UnitTaskTempFileRemove(DirectFileHandleComponent directFileHandleComponent){
        this.directFileHandleComponent = directFileHandleComponent;
    }
    @Override
    public UnitTask getUnitTask() {
        return this.unitTask;
    }

    @Override
    public FileWorkflowDraft process(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft) {
        String tempFilePath = directFileHandleComponent.resolvePersistedFilePath(draft.getApplicationTempFileUuid());
        if(StringUtils.isEmpty(tempFilePath)){
            throw new GeneralCustomException("TEMP FILE PATH IS NULL");
        }
        if(!Files.exists(Path.of(tempFilePath))) {
            throw new GeneralCustomException("TEMP FILE NOT EXISTS");
        }
        try{
            directFileHandleComponent.deleteTempFile(draft.getApplicationTempFileUuid());
        }catch(IOException IOE){
            draft.setRemoveSucceed(false);
            IOE.printStackTrace();
            throw new GeneralCustomException("IO EXCEPTION");
        }
        draft.setRemoveSucceed(true);
        return draft;
    }

    @Override
    public FileWorkflowDraft compensateOnFailure(UnitTaskRequest unitTask, WorkflowRequest request, FileWorkflowDraft draft) {
        return null;
    }
}
