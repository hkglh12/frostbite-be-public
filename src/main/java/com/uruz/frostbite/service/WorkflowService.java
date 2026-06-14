package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.dto.file.TempFileMetaInfoDraft;
import com.uruz.frostbite.enums.WorkflowStatus;
import com.uruz.frostbite.mapper.WorkflowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowService {
    /**
     * 실제 물리 파일을 핸들링하고, 워크플로우 단위유닛을 호출하는 객체
     * */

    // TODO : 메타정보 수정된 이력을 직접 관리할것인가? Orchestrator까지 Async로 묶어서 Orchestrator에서 보고할것인가?
    private final FileService fileService;
    private final DirectFileHandleComponent directFileHandleComponent;
    private final WorkflowMapper workflowMapper;

    // 우선 self로 처리. 추후 별도 분리 필요
    private final WorkflowService workflowService;


    @Autowired
    public WorkflowService(FileService fileService, DirectFileHandleComponent directFileHandleComponent, WorkflowMapper workflowMapper,
        @Lazy WorkflowService workflowService
    ){
        this.fileService = fileService;
        this.directFileHandleComponent = directFileHandleComponent;
        this.workflowMapper = workflowMapper;
        this.workflowService = workflowService;

    }

    /** 임시파일을 일종의 단발성 워크플로우로 간주하고, 메타 정보를 관리하는 fileService로부터 분리,
     * 실제 물리파일을 건드리는 fileWorkflowService에서 통합관리*/
    public TempFileMetaInfoDraft uploadTempFile(MultipartFile tempFile, UUID folderUuid, Integer createUserId){
        return directFileHandleComponent.reallocateTempFile(tempFile);
    }

    public void deleteTempFile(UUID uuid) throws Exception{
        directFileHandleComponent.deleteTempFile(uuid.toString());
    }
    public void deleteFile(String path) throws IOException{
        directFileHandleComponent.deleteFile(path);
    }

    public void registFile(FileCreateRequest request, Long fileId, Integer createUserId) throws IOException{
        Map<String, String> destInfo = null;
        UUID destinationUuid = UUID.randomUUID();
        String[] uuids = destinationUuid.toString().split("-");
        String attemptFileName = uuids[4];
        String persistedFilePath =
                directFileHandleComponent.resolvePersistedFilePath(destinationUuid.toString().replace("-"+attemptFileName, "").replace("-","/"));

        try {
            destInfo = workflowService.attempRegist(request, fileId, createUserId, persistedFilePath,attemptFileName);
        } catch (Exception e) {
            directFileHandleComponent.deleteFile(persistedFilePath + "/" + attemptFileName);
            workflowService.updateFileRegistFailed(
                    LocalDateTime.now(),
                    fileId,
                    WorkflowStatus.FAILED_FINAL
            );

            throw new GeneralCustomException("REGIST WORKFLOW FAILED");
        }
    }
    @Transactional
    public Map<String, String>  attempRegist(FileCreateRequest request, Long fileId, Integer createUserId,
                                             String attemptDestPath, String attemptFileName) {
        workflowMapper.updateFileRegistStartTime(
                LocalDateTime.now(),
                fileId,
                WorkflowStatus.PROCESSING
        );

        Map<String, String> destInfo =
                directFileHandleComponent.transferFileToPersistPath(
                        request.getTempFileUuid(),
                        attemptDestPath, attemptFileName
                );

        fileService.completeRegistWorkflow(fileId, LocalDateTime.now(), destInfo);

        workflowMapper.updateFileRegistDone(
                LocalDateTime.now(),
                fileId,
                WorkflowStatus.SUCCESS
        );

        return destInfo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFileRegistFailed(LocalDateTime current, Long fileId, WorkflowStatus status){
        workflowMapper.updateFileRegistDone(LocalDateTime.now(), fileId, WorkflowStatus.FAILED_FINAL);
    }
}
