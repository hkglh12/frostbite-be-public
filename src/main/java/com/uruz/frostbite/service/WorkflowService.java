package com.uruz.frostbite.service;

import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.enums.WorkflowStatus;
import com.uruz.frostbite.enums.workflow.Workflow;
import com.uruz.frostbite.enums.workflow.WorkflowDomainType;
import com.uruz.frostbite.enums.workflow.WorkflowRequestStatus;
import com.uruz.frostbite.mapper.WorkflowRequestMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WorkflowService {
    /**
     * 워크플로우 및 유닛테스크의 메타데이터를 핸들링하는 서비스.
     * */

    private final WorkflowRequestMapper workflowRequestMapper;

    @Autowired
    public WorkflowService(WorkflowRequestMapper workflowRequestMapper){
        this.workflowRequestMapper = workflowRequestMapper;
    }

    /** 대기상태 워크플로우 요청 등록 */
    public WorkflowRequest registerWaitingWorkflowRequest(WorkflowDomainType workflowDomainType, Long targetId,
                                                          Workflow requestWorkflow, String requestPayloadString, int createUserId){
        WorkflowRequest workflowRequest =
                WorkflowRequest.createWaiting(
                        requestWorkflow,
                        workflowDomainType,
                        targetId,
                        requestPayloadString,
                        createUserId,
                        LocalDateTime.now()
                );

        workflowRequestMapper.insertWorkflowRequest(workflowRequest);

        return workflowRequest;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFileRegisterFailed(LocalDateTime current, Long fileId, WorkflowStatus status){
        workflowRequestMapper.updateFileRegisterDone(LocalDateTime.now(), fileId, WorkflowRequestStatus.FAILED_FINAL);
    }


    void updateFileRegisterStartTime(LocalDateTime startTime, Long fileId, WorkflowStatus workflowStatus){
        workflowRequestMapper.updateFileRegisterStartTime(startTime, fileId, workflowStatus);
    }

    void updateFileRegisterDone(@Param("endTime")LocalDateTime endTime, @Param("fileId") Long fileId, @Param("fileRegistStatus")WorkflowRequestStatus workflowStatus) {
        workflowRequestMapper.updateFileRegisterDone(endTime, fileId, workflowStatus);
    }

    /** 워크플로우 요청 조회 */
    public WorkflowRequest getWorkflowRequest(long workflowRequestId){
        return workflowRequestMapper.selectWorkflowRequest(workflowRequestId);
    }

    /** 워크플로우 ready 상태 mark -> QUEUE에서 벗어나 worker thread 진입 */
    public void markWorkflowRequestReady(long workflowRequestId, LocalDateTime currentTime){
        workflowRequestMapper.updateWorkflowRequestAsReady(workflowRequestId, WorkflowRequestStatus.READY, currentTime);
    }

    /** 워크플로우 진행중 마킹 */
    public void markWorkflowRequestProcessing(long workflowRequestId, LocalDateTime currentTime){
        workflowRequestMapper.updateWorkflowRequestAsProcessing(workflowRequestId, WorkflowRequestStatus.PROCESSING, currentTime);
    }


    public void markWorkflowRequestSucceed(long workflowRequestId, LocalDateTime currentTime){
        workflowRequestMapper.updateWorkflowRequestAsSucceed(workflowRequestId, WorkflowRequestStatus.SUCCEEDED, currentTime);
    }

    public void markWorkflowRequestFailed(long workflowRequestId, LocalDateTime currentTime, String failedMessage){
        workflowRequestMapper.updateWorkflowRequestAsFailedFinal(workflowRequestId, WorkflowRequestStatus.FAILED_FINAL, currentTime, failedMessage);
    }

}
