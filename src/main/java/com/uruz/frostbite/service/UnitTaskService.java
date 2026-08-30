package com.uruz.frostbite.service;

import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.enums.workflow.UnitTaskRequestStatus;
import com.uruz.frostbite.mapper.UnitTaskMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UnitTaskService {

    private final UnitTaskMapper unitTaskMapper;
    public UnitTaskService(UnitTaskMapper unitTaskMapper){
        this.unitTaskMapper = unitTaskMapper;
    }
    public UnitTaskRequest getLatestUnitTaskRequest(long workflowRequestId){
        return unitTaskMapper.selectLatestUnitTaskRequest(workflowRequestId);
    }

    public void insertCurrentUnitTask(UnitTaskRequest unitTaskRequest) {
        unitTaskMapper.insertUnitTaskRequest(unitTaskRequest);
    }

    public void markUnitTaskAsProcessing(long unitTaskRequestId, LocalDateTime currentTime){
        unitTaskMapper.updateUnitTaskRequestStatus(unitTaskRequestId, UnitTaskRequestStatus.PROCESSING, currentTime);
    }

    public void markUnitTaskToSucceed(long unitTaskRequestId, LocalDateTime currentTime){
        unitTaskMapper.updateUnitTaskRequestEndStatus(unitTaskRequestId, UnitTaskRequestStatus.SUCCEEDED, currentTime);
    }
    public void markUnitTaskToFailedFinal(long unitTaskRequestId, LocalDateTime currentTime){
        unitTaskMapper.updateUnitTaskRequestEndStatus(unitTaskRequestId, UnitTaskRequestStatus.FAILED_FINAL, currentTime);
    }
    public void markUnitTaskToFailedRetryable(long unitTaskRequestId, LocalDateTime currentTime){
        unitTaskMapper.updateUnitTaskRequestEndStatus(unitTaskRequestId, UnitTaskRequestStatus.FAILED_RETRYABLE, currentTime);
    }

    public UnitTask resolveNextUnitTask(
            List<UnitTask> unitTasks,
            UnitTaskRequest previous
    ) {
        if (previous == null) {
            return unitTasks.get(0);
        }

        if (previous.isFailedFinal()) {
            return null;
        }

        if (previous.isFailedRetryable()) {
            return previous.getUnitTaskName();
        }

        if (previous.isSucceeded()) {
            int previousIndex =
                    unitTasks.indexOf(previous.getUnitTaskName());

            if(previousIndex <0) throw new IllegalStateException(" \"UnitTask is not registered in workflow: \" "+ previous.getUnitTaskName());

            int nextIndex = previousIndex + 1;

            if (nextIndex >= unitTasks.size()) {
                return null;
            }

            return unitTasks.get(nextIndex);
        }

        throw new IllegalStateException("처리 불가능한 UnitTask 상태");
    }
}
