package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.enums.WorkflowStatus;
import com.uruz.frostbite.enums.workflow.UnitTaskRequestStatus;
import com.uruz.frostbite.enums.workflow.WorkflowRequestStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Mapper
@Repository
public interface WorkflowRequestMapper {

    void insertWorkflowRequest(WorkflowRequest workflowRequest);
    void updateFileRegisterStartTime(@Param("startTime") LocalDateTime startTime, @Param("fileId") Long fileId, @Param("fileRegistStatus") WorkflowStatus workflowStatus);

    void updateFileRegisterDone(@Param("endTime")LocalDateTime endTime, @Param("fileId") Long fileId, @Param("fileRegistStatus")WorkflowRequestStatus workflowStatus);

    WorkflowRequest selectWorkflowRequest(@Param("workflowRequestId") long workflowRequestId);

    void updateWorkflowRequestAsProcessing(@Param("workflowRequestId") long workflowRequestId, @Param("status") WorkflowRequestStatus status, @Param("currentTime") LocalDateTime currentTime);

    void updateWorkflowRequestAsSucceed(@Param("workflowRequestId") long workflowRequestId, @Param("status") WorkflowRequestStatus status, @Param("currentTime") LocalDateTime currentTime);
    void updateWorkflowRequestAsFailedFinal(@Param("workflowRequestId") long workflowRequestId, @Param("status") WorkflowRequestStatus status, @Param("currentTime") LocalDateTime currentTime,
    @Param("failedMessage") String failedMessage);
    void insertUnitTaskRequest(UnitTaskRequest unitTaskRequest);
    UnitTaskRequest selectLatestUnitTaskRequest(@Param("workflowRequestId") long workflowRequestId);

    void updateUnitTaskRequestStatus(@Param("unitTaskRequestId") long unitTaskRequestId,
                                     @Param("status") UnitTaskRequestStatus status,
                                     @Param("currentTime") LocalDateTime currentTime);

    void updateUnitTaskRequestEndStatus(@Param("unitTaskRequestId") long unitTaskRequestId,
                                   @Param("status") UnitTaskRequestStatus status,
                                   @Param("currentTime") LocalDateTime currentTime);
}
