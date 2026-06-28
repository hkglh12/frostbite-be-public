package com.uruz.frostbite.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.dto.workflow.internal.FileWorkflowDraft;
import com.uruz.frostbite.enums.WorkflowStatus;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.enums.workflow.WorkflowRequestStatus;
import com.uruz.frostbite.service.unitTaskExecutor.UnitTaskExecutor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkflowProcessor {
    /**
     * 실제 물리 파일을 핸들링하고, 워크플로우의 흐름을 수행하는 주체
     * */

    // TODO : 메타정보 수정된 이력을 직접 관리할것인가? Orchestrator까지 Async로 묶어서 Orchestrator에서 보고할것인가?
    private final FileService fileService;
    private final DirectFileHandleComponent directFileHandleComponent;

    private final WorkflowService workflowService;
    private final UnitTaskService unitTaskService;

    private final List<UnitTaskExecutor> executors;

    private Map<UnitTask, UnitTaskExecutor> executorMap;
    private final ObjectMapper mapper;

    @Autowired
    public WorkflowProcessor(FileService fileService, DirectFileHandleComponent directFileHandleComponent,
                             WorkflowService workflowService,
                             UnitTaskService unitTaskService,
                             List<UnitTaskExecutor> executors,
                             ObjectMapper mapper
    ){
        this.fileService = fileService;
        this.directFileHandleComponent = directFileHandleComponent;
        this.workflowService = workflowService;
        this.unitTaskService = unitTaskService;
        this.executors = executors;
        this.mapper = mapper;

    }

    @PostConstruct
    void init() {
        executorMap = executors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        UnitTaskExecutor::getUnitTask,
                        Function.identity()
                ));
    }

    @Deprecated(forRemoval = true)
    public void provisionFile(FileCreateRequest request, Long fileId, Integer createUserId) throws IOException{

    }
/*
    @Transactional
    public Map<String, String>  attempRegist(FileCreateRequest request, Long fileId, Integer createUserId,
                                             String attemptDestPath, String attemptFileName) {
        workflowService.updateFileRegistStartTime(
                LocalDateTime.now(),
                fileId,
                WorkflowStatus.PROCESSING
        );



        fileService.completeRegistWorkflow(fileId, LocalDateTime.now(), destInfo);

        workflowService.updateFileRegistDone(
                LocalDateTime.now(),
                fileId,
                WorkflowStatus.SUCCESS
        );

        return destInfo;
    }
*/

    public void processWorkflow(Long workflowRequestId){
        // 워크플로우가 실행 가능한 상태인지 확인
        WorkflowRequest request = workflowService.getWorkflowRequest(workflowRequestId);

        if(request.getStatus() != WorkflowRequestStatus.READY){
            // 문제가 있음. 이벤트 퍼블리셔로 안내 필요
            workflowService.markWorkflowRequestFailed(workflowRequestId, LocalDateTime.now(), "Status not ready");
            return;
        }
        // 워크플로우 작업 시작
        workflowService.markWorkflowRequestStarted(workflowRequestId, LocalDateTime.now());

        Map<String, Object> requestPayload = null;

        try{
            requestPayload = mapper.readValue(request.getRequestPayload(), new TypeReference<Map<String, Object>>() {});
        } catch(JsonProcessingException JSE){
            //TODO event publish 필요
            workflowService.markWorkflowRequestFailed(workflowRequestId, LocalDateTime.now(), JSE.getMessage());
            return;
        }

        FileWorkflowDraft draft = FileWorkflowDraft.createInitialForMoveOnly(request, requestPayload);
        try {
            while (true) {
                UnitTaskRequest previousUnitTask = unitTaskService.getLatestUnitTaskRequest(workflowRequestId);
                UnitTask task = unitTaskService.resolveNextUnitTask(request.getWorkflowName().getUnitTasks(), previousUnitTask);

                if(task == null){
                    break;
                }

                UnitTaskRequest currentUnitTask = UnitTaskRequest.createUnitTask(task, workflowRequestId, request.getWorkflowName().getUnitTaskOrder(task),
                                                                                request.getWorkflowDomainType(), request.getTargetId(), request.getCreateUserId(), LocalDateTime.now());

                try {
                    UnitTaskExecutor taskExecutor = executorMap.get(currentUnitTask.getUnitTaskName());
                    draft = taskExecutor.process(currentUnitTask, request, draft);
                } catch (GeneralCustomException ge){

                    // phase1에서는 영구실패만 지원
                    workflowService.markWorkflowRequestFailed(workflowRequestId, LocalDateTime.now(), ge.getMessage());
                    return;
                    /*
                    유닛태스크 처리 실패 기록;
                        -> 만약 이번 시도 회차가 retrable max면 -> failed_final unittask status 변경, allow_retry false로 전환
                        exception 삼킴
                        -> 이번 시도 회차 < retrable 면 allow_retry true로 지정
                        exception throw


                     */
                }

            }
            workflowService.markWorkflowRequestSucceed(workflowRequestId, LocalDateTime.now());
        } catch (Exception e){
            // 후속처리 (workflow 실패)
            // 후속처리 (SSE 발급)
            workflowService.markWorkflowRequestFailed(workflowRequestId, LocalDateTime.now(), "unexpected workflow trigger failed");
        }
    }


}
