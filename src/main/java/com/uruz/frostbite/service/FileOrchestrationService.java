package com.uruz.frostbite.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.File;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.WorkflowRequest;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.dto.file.SimpleFileResponse;
import com.uruz.frostbite.dto.file.TempFileMetaInfoDraft;
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.workflow.Workflow;
import com.uruz.frostbite.enums.workflow.WorkflowDomainType;
import com.uruz.frostbite.utils.TempFileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.io.IOException;
import java.util.UUID;


@Service
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


    public FileOrchestrationService(FileService fileService,
                                    WorkflowService workflowService,
                                    WorkflowProcessor workflowProcessor, TempFileUtils tempFileUtils,
                                    DirectFileHandleComponent directFileHandleComponent,
                                    ObjectMapper mapper){
        this.fileService = fileService;
        this.workflowService = workflowService;
        this.workflowProcessor = workflowProcessor;
        this.tempFileUtils = tempFileUtils;
        this.directFileHandleComponent = directFileHandleComponent;
        this.mapper = mapper;
    }
    /** 임시파일 정보를 parameter로 file_tb 초안 등록 */
    @Transactional
    public SimpleFileResponse registFile(FileCreateRequest request, UUID folderUuid, Integer createUserId) throws IOException {

        // 파일 메타정보 등록
        // 해당 파일 메타등록 에서는 "파일의 스토리지 저장소" 를 저장하지 않음!
        // 해당 파일의 스토리지 저장소는 "암호화 UnitTask -> copy storage Unit Task"가 옮긴 뒤 저장함!
        File initialFile = fileService.registInitialFile(request, folderUuid, createUserId);

        Workflow inDevWorkflow = Workflow.WF_FILE_PROVISION_MOVEONLY;

        String requestPayloadString = mapper.writeValueAsString(request);

        WorkflowRequest workflowRequest = workflowService.registerWorkflowRequest(WorkflowDomainType.WF_TYPE_FILE,
                initialFile.getFileId(), inDevWorkflow, requestPayloadString, createUserId);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        workflowProcessor.processWorkflow(workflowRequest.getWorkflowRequestId());
                    }
                }
        );

        SimpleFileResponse response = SimpleFileResponse.builder()
                .uuid(initialFile.getUuid())
                .fileName(initialFile.getFileName())
                .parentFolderUuid(folderUuid)
                .fileStatus(FileStatus.UNREGISTERED)
                .build();

        return response;
    }

    /** 임시파일 정보 임시파일테이블 등록, workflow를 통해 임시경로 이동*/
    @Transactional
    public TempFileMetaInfoDraft uploadTempFile(MultipartFile file, UUID folderUuid, Integer createUserId){
        // TODO Folder_Tb 내 폴더에 접근 권한 있는지 확인 필요
        // 업로드하려는 폴더에 권한이 있는지 확인정도는 해줘야함.

        // 이 java임시파일경로 -> app임시파일 경로 전환 까지 @Async걸면, MultiPartResolver가 finally에서 제거할 수도 있음.
        // 이 작업 까지는 동기로 진행하는게 안전. 동일 FS, 동일 mount 내 move는 파일 디스크립터 수정으로 짧은시간만 쓴다고 들음
        TempFileMetaInfoDraft draft = directFileHandleComponent.reallocateTempFile(file);

        FileTemp fileTemp = FileTemp.createReady(draft.getPhysicalFileUuid(), draft.getOriginalFileName(),
                draft.getExtension(), draft.getFileSize(), createUserId, tempFileUtils.getTempFileTTLToSeconds());
        try {
            fileService.insertFileTemp(fileTemp);
        }catch(Exception e){
            // DB Insert 실패 시 copy한 임시파일 삭제필요
            try {
                directFileHandleComponent.deleteTempFile(draft.getPhysicalFileUuid().toString());
            } catch(Exception EE){
                EE.printStackTrace();
                throw new GeneralCustomException("FILE REMOVE FAILED IN REGIS TEMP FILE FLOW");
            }
            throw new GeneralCustomException("TEMP FILE REGISTRATION FAILED");
        }
        return draft;
    }

}
