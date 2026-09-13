package com.uruz.frostbite.service;

import ch.qos.logback.core.model.processor.ProcessorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.domain.File;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.dto.file.SimpleFileResponse;
import com.uruz.frostbite.dto.file.TempFileMetaInfoDraft;
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.enums.workflow.Workflow;
import com.uruz.frostbite.enums.workflow.WorkflowDomainType;
import com.uruz.frostbite.utils.TempFileUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;


@Service
@Slf4j
public class FileOrchestrationService {

    /**
     * 파일 추상화 진입점
     *
     * 내부 흐름은 아래와 같다.
     *
     * 1. 파일 메타정보 변경
     *    -> FileService 호출
     *
     * 2. 물리 파일 처리
     *    -> DirectFileHandleComponent 호출
     *
     * 유닛테스크작업은 workflowService.callUnitTask() 를 사용, 그 외  처리는 직접 수행 가능
     * 3. 파일 Workflow 생성 및 진행
     *    -> WorkflowService 호출
     *
     * 4. Workflow 수행 결과에 따라
     *    File 메타정보 변경이 필요한 경우
     *    WorkflowService 결과 반환
     *    -> FileOrchestrationService
     *    -> FileService 호출
     */
    private final FileService fileService;
    private final WorkflowService workflowService;
    private final WorkflowProcessor workflowProcessor;
    private final TempFileUtils tempFileUtils;
    private final DirectFileHandleComponent directFileHandleComponent;
    private final ObjectMapper mapper;
    private final ThreadPoolTaskExecutor workflowExecutor;


    public FileOrchestrationService(FileService fileService,
                                    WorkflowService workflowService,
                                    WorkflowProcessor workflowProcessor, TempFileUtils tempFileUtils,
                                    DirectFileHandleComponent directFileHandleComponent,
                                    @Qualifier("workflowExecutorPool") ThreadPoolTaskExecutor workflowExecutor,
                                    ObjectMapper mapper){
        this.fileService = fileService;
        this.workflowService = workflowService;
        this.workflowProcessor = workflowProcessor;
        this.tempFileUtils = tempFileUtils;
        this.workflowExecutor = workflowExecutor;
        this.directFileHandleComponent = directFileHandleComponent;
        this.mapper = mapper;
    }
    /** 임시파일 정보를 parameter로 file_tb 초안 등록 */
    @Transactional
    public SimpleFileResponse registerFile(FileCreateRequest request, UUID folderUuid, Integer createUserId) throws RequestProcessingException{

        // 파일 테이블 등록 + 파일 워크플로우 상태 row 등록
        File registeringAttemptFile = fileService.registerFileMeta(request, folderUuid, createUserId);

        // 현 시점 모든 파일은 동일한 흐름을 타도록
        Workflow inDevWorkflow = Workflow.WF_FILE_REGISTER_ONLY;

        String requestPayloadString = null;
        try{
            requestPayloadString = mapper.writeValueAsString(request);
        } catch(IOException IOE) {
            // CheckedException은 rollback 트리거 대상이 아님. 그러나 unCheckedException으로 래핑해서 던지면 Rollback 대상이됨
            // 파일 정식등록에 실패해도, 재시도할수있음. 이에따라 보상패턴으로 임시파일삭제까지는 유지하지 않음. 스케쥴돌려서 임시파일 제거하는 뱡향으로 유지
            throw new RequestProcessingException(ApiErrorCode.INTERNAL_ERROR_FOR_JSON_PARSE);
        }
        // 대기상태인 워크플로우 요청 등록
        WorkflowRequest workflowRequest = workflowService.registerWaitingWorkflowRequest(WorkflowDomainType.WF_TYPE_FILE,
                registeringAttemptFile.getFileId(), inDevWorkflow, requestPayloadString, createUserId);

        log.info(Thread.currentThread().getName() + " register async synchronization process async Execute");
        /**
         * SEE : docs/analyze/transactionManager.md
         * */
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    // throw 하면 caller한테 던질수는 있지만, caller는 "등록"만 책임한계로 지정했으므로,
                    // 실행에 관한건 비동기로 넘긴시점이라 굳이 http response throw를 구현하지는 않기로함
                    @Override
                    public void afterCommit() {

                        try {
                            // 왜 직접 executor에 execute 호출?
                            // submit은 exception 을 Future 안에 감추는 성향이 있다고..
                            // 1. @Async 호출 사용 시, Policy를 Abort로 잡으면 예외 발생시 Spring Proxy 내에서 Exception이 터짐
                            // 2. Async 진입 중 Proxy 내에서 터진 Exception을 관리하려면 별도 설정을 잡아줘야함. 비즈니스 로직 외부로 튕겨나가서
                            // 잡히므로 흐름 따라가기 힘들것 같기도 하고, 설정 공부가 오버헤드 같기도 함.
                            //  -> 추후 다시 공부하기로 하고, 현시점 execute() 호출로 변경
                            // * 유의점 : 이 execute는 "진입시" exception만 잡힘. 비동기 진입 후 Exception을 잡지는 못함을 명심할것
                            workflowExecutor.execute(() -> {
                                System.out.println("WORKFLOW THREAD = " + Thread.currentThread().getName());
                                workflowProcessor.processWorkflow(workflowRequest.getWorkflowRequestId());
                            });

                        }catch(RejectedExecutionException e){
                            // SEE: ... 에 의해, requires_new가 명시적이어야함!
                            workflowService.markWorkflowRequestFailed(workflowRequest.getWorkflowRequestId(), LocalDateTime.now(), e.getMessage());
                        }
                    }
                }
        );

        SimpleFileResponse response = SimpleFileResponse.builder()
                .uuid(registeringAttemptFile.getUuid())
                .fileName(registeringAttemptFile.getFileName())
                .parentFolderUuid(folderUuid)
                .fileStatus(FileStatus.UNREGISTERED)
                .build();

        return response;
    }

    /** 임시파일 JAVA -> Application Temp로 이동, 임시파일 정보 등록*/
    public TempFileMetaInfoDraft registerTempFile(MultipartFile file, UUID folderUuid, Integer createUserId) {
        // TODO Folder_Tb 내 폴더에 접근 권한 있는지 확인 필요
        // 업로드하려는 폴더에 권한이 있는지 확인정도는 해줘야함.

        // 이 java임시파일경로 -> app임시파일 경로 전환 까지 @Async걸면, MultiPartResolver가 finally에서 제거할 수도 있음.
        // 이 작업 까지는 동기로 진행하는게 안전. 동일 FS, 동일 mount 내 move는 파일 디스크립터 수정으로 짧은시간만 쓴다고 들음
        TempFileMetaInfoDraft draft = directFileHandleComponent.reallocateTempFile(file);

        FileTemp fileTemp = FileTemp.createPreparedTempFile(draft.getTempFileUuid(), draft.getOriginalFileName(),
              draft.getFileSize(), createUserId, tempFileUtils.getTempFileTTLToSeconds());
        try {
            fileService.insertFileTemp(fileTemp);
        } catch(Exception e) {
            e.printStackTrace();
            // DB Insert 실패 시 copy한 임시파일 삭제필요
            try {
                directFileHandleComponent.deleteTempFile(draft.getTempFileUuid());
            } catch(Exception EE){
                EE.printStackTrace();
                throw new RequestProcessingException(ApiErrorCode.INTERNAL_ERROR_FOR_RESOURCE_HANDLE);
            }
            throw e;
        }
        return draft;
    }

    public void downloadDecryptedFile(HttpServletResponse response, Integer userId, UUID folderUuid, UUID fileUuid) throws RequestProcessingException {
        // TODO 권한 검증
        File file = fileService.selectFileMetaForInternalProcess(fileUuid);
        Path persistedPath = Path.of(file.getStorageFilePath() + java.io.File.separator + file.getStorageFileName());

        try {
            directFileHandleComponent.decryptToStream(persistedPath.toFile(), file.getOriginalFileName(), response);
        }catch (Exception e){
            throw new RequestProcessingException(ApiErrorCode.INTERNAL_ERROR_FOR_RESOURCE_HANDLE);
        }
    }

}
