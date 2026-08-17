package com.uruz.frostbite.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.customException.InternalProcessingException;
import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.dto.workflow.internal.FileWorkflowDraft;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.enums.workflow.WorkflowRequestStatus;
import com.uruz.frostbite.service.unitTaskExecutor.UnitTaskExecutor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final TransactionTemplate transactionTemplateForRequiesNew;

    @Autowired
    public WorkflowProcessor(FileService fileService, DirectFileHandleComponent directFileHandleComponent,
                             WorkflowService workflowService,
                             UnitTaskService unitTaskService,
                             List<UnitTaskExecutor> executors,
                             ObjectMapper mapper,
                             PlatformTransactionManager transactionManager
    ){
        this.fileService = fileService;
        this.directFileHandleComponent = directFileHandleComponent;
        this.workflowService = workflowService;
        this.unitTaskService = unitTaskService;
        this.executors = executors;
        this.mapper = mapper;
        // platformTransactionManager는 저수준 rollback, commit을 수행해주는 객체
        // transactionTempalte는 transactinoManager를 가지고 별도로 작업을 추가로 해주는 래퍼
        this.transactionTemplateForRequiesNew = new TransactionTemplate(transactionManager);
        //
        this.transactionTemplateForRequiesNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

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

    /** 등록된 워크플로우를 실행 */
    public void processWorkflow(Long workflowRequestId){
        // 워크플로우가 실행 가능한 상태인지 확인
        WorkflowRequest workflowRequest = workflowService.getWorkflowRequest(workflowRequestId);

        if(workflowRequest == null){
            // afterCommit()으로 들어온 이상, 문제는 없을것같지만..
            return;
        }

        if(workflowRequest.getStatus() != WorkflowRequestStatus.WAITING){
            workflowService.markWorkflowRequestFailed(workflowRequestId, LocalDateTime.now(), "STATUS IS NOT WAITING");
            // TODO Phase2 SSE 도입 시, 작업 실패 안내를 해야함
            return;
        }
        // executor 진입 후, 작업 대기상태로 전환
        workflowService.markWorkflowRequestReady(workflowRequestId, LocalDateTime.now());

        // 요청 내역 파싱
        Map<String, Object> requestPayload = null;
        try{
            requestPayload = mapper.readValue(workflowRequest.getRequestPayload(), new TypeReference<Map<String, Object>>() {});
        } catch(JsonProcessingException JSE){
            //TODO event publish 필요
            workflowService.markWorkflowRequestFailed(workflowRequestId, LocalDateTime.now(), JSE.getMessage());
            return;
        }

        // 워크플로우 작업 시작
        workflowService.markWorkflowRequestProcessing(workflowRequestId, LocalDateTime.now());
        FileWorkflowDraft draft = FileWorkflowDraft.createWorkflowDraft(workflowRequest, requestPayload);
        UnitTaskRequest currentUnitTaskRequest = null;
        try {
            while (true) {
                // 이전작업 조회
                UnitTaskRequest previousUnitTask = unitTaskService.getLatestUnitTaskRequest(workflowRequestId);
                // 다음작업 조회
                UnitTask task = unitTaskService.resolveNextUnitTask(workflowRequest.getWorkflowName().getUnitTasks(), previousUnitTask);

                if(task == null){
                    break;
                }

                // workflow 처리기 workflowProcessor는 워크플로우 흐름과 UnitTask의 상태관리/호출 까지 담당
                // 각 UintTask는 자기 자신의 관심사 도메인의 상태값 변경까지 담당.
                currentUnitTaskRequest = UnitTaskRequest.createCurrentUnitTask(task, workflowRequestId, workflowRequest.getWorkflowName().getUnitTaskOrder(task),
                                                                                workflowRequest.getWorkflowDomainType(), workflowRequest.getTargetId(), workflowRequest.getCreateUserId(), LocalDateTime.now());

                UnitTaskExecutor taskExecutor = null;

                taskExecutor = executorMap.get(currentUnitTaskRequest.getUnitTaskName());

                if (taskExecutor == null) {
                    workflowService.markWorkflowRequestFailed(
                            workflowRequestId,
                            LocalDateTime.now(),
                            "UNIT TASK EXECUTOR NOT FOUND: "
                                    + currentUnitTaskRequest.getUnitTaskName()
                    );
                    return;
                }
                try{
                    LocalDateTime startedTime = LocalDateTime.now();
                    // unitTask 작업시작 정보는 직접 UnitTask에서
                    unitTaskService.insertCurrentUnitTask(currentUnitTaskRequest);
                    unitTaskService.markUnitTaskAsProcessing(currentUnitTaskRequest.getUnitTaskRequestId(), startedTime);
                    draft = taskExecutor.process(currentUnitTaskRequest, workflowRequest, draft);

                    UnitTaskExecutor succeedTaskExecutor = taskExecutor;
                    FileWorkflowDraft succeedDraft = draft;
                    UnitTaskRequest succeedUnitTaskRequest = currentUnitTaskRequest;

                    transactionTemplateForRequiesNew.executeWithoutResult(transactionStatus -> {
                        LocalDateTime succeedTime = LocalDateTime.now();
                        unitTaskService.markUnitTaskToSucceed(succeedUnitTaskRequest.getUnitTaskRequestId(), succeedTime);
                        succeedTaskExecutor.markOnSucceed(succeedUnitTaskRequest, workflowRequest, succeedDraft, succeedTime);
                    });

                } catch (InternalProcessingException ge){

                    // 실패 정보 관리는 원자적이어야 한다고 생각
                    // WorkflowProcessor가 UnitTask실패 통보를, UnitTask가 관심사 도메인의 상태값 변경을
                    // 이경우, @Transactional이 필요, 그러나 WorkflowProcessor가 호출하는 메서드가 Transactional(Requires_new)여야 함
                    // .this 호출은 Proxy 안타서 transactional 안먹음. 하려면 _self, 명시적, 별도bean 필요
                    // Spring에서는 별도의 Delegating Bean 등록을 1권장, 명시적 신규 트랜잭션 (명령형 프로그램 트랜잭션) 이 2권장.
                    // 굳이 Bean을 더 늘리지 않고 개발 해보기로 시도
                    FileWorkflowDraft failedDraft = draft;
                    UnitTaskExecutor failedUnitTaskExecutor = taskExecutor;
                    UnitTaskRequest failedUnitTaskRequest = currentUnitTaskRequest;
                    transactionTemplateForRequiesNew.executeWithoutResult(transactionStatus -> {
                        LocalDateTime failedTime = LocalDateTime.now();
                        unitTaskService.markUnitTaskToFailedFinal(failedUnitTaskRequest.getUnitTaskRequestId(), failedTime);
                        failedUnitTaskExecutor.compensateOnFailure(failedUnitTaskRequest, workflowRequest, failedDraft, failedTime, ge.getMessage());
                        workflowService.markWorkflowRequestFailed(workflowRequestId, LocalDateTime.now(), failedUnitTaskExecutor.getUnitTask() + " : UNIT TASK FAILED");
                    });

                    throw new InternalProcessingException("FAILURE TRIGGERED");
                } catch (RequestProcessingException qe){
                    UnitTaskExecutor succeedTaskExecutor = taskExecutor;
                    FileWorkflowDraft succeedDraft = draft;
                    UnitTaskRequest succeedUnitTaskRequest = currentUnitTaskRequest;

                    // Quitely Propagated Exception은 내부 흐름상 재처리 대상. 파일 처리 흐름 자체에서는 성공으로 간주.
                    // 추후, 스케쥴러를 통해 unitTask Type 별 별도 처리를 처리하는 방향으로 진행.
                    transactionTemplateForRequiesNew.executeWithoutResult(transactionStatus -> {
                        LocalDateTime succeedTime = LocalDateTime.now();
                        unitTaskService.markUnitTaskToFailedFinal(succeedUnitTaskRequest.getUnitTaskRequestId(), succeedTime);
                        succeedTaskExecutor.markOnSucceed(succeedUnitTaskRequest, workflowRequest, succeedDraft, succeedTime);
                    });
                }
            }
            workflowService.markWorkflowRequestSucceed(workflowRequestId, LocalDateTime.now());
        } catch (RequestProcessingException e){
            // 후속처리 (workflow 실패)
            // 후속처리 (SSE 발급)
            e.printStackTrace();;
            throw e;
        } catch(Exception e){
            LocalDateTime failedTime = LocalDateTime.now();
            unitTaskService.markUnitTaskToFailedFinal(currentUnitTaskRequest.getUnitTaskRequestId(), failedTime);
            workflowService.markWorkflowRequestFailed(workflowRequestId, LocalDateTime.now(), e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


}
