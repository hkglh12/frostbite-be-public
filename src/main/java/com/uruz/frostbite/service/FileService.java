package com.uruz.frostbite.service;


import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.File;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.FileWorkflowStatus;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.WorkflowStatus;
import com.uruz.frostbite.enums.workflow.Workflow;
import com.uruz.frostbite.mapper.FileMapper;
import com.uruz.frostbite.service.resolver.FolderResolverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FileService {
    /**
     - 파일 도메인 비즈니스 흐름에서, 파일 메타정보를 관리 조작한다.

     CF) FileWorkflowServie : workflow 오케스트레이터
     CF) DirectFileHandlerComponent : 실제 물리 파일을 핸들링하는 객체
    */

    private final FileMapper fileMapper;

    private final FolderResolverService folderResolverService;

    @Autowired
    public FileService(FileMapper fileMapper, FolderResolverService folderResolverService){
        this.fileMapper = fileMapper;
        this.folderResolverService = folderResolverService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public File registInitialFile(FileCreateRequest request, UUID folderUuid, Integer createUserId){
        // Temp UUID로부터 유효한 UUID인지 검증
        FileTemp fileTemp = fileMapper.selectTempFileByUuid(request.getTempFileUuid());
        if(fileTemp == null){
            throw new GeneralCustomException("TEMP FILE UUID INVALID");
        }
        if(!createUserId.equals(fileTemp.getCreateUserId())){
            throw new GeneralCustomException("YOU ARE TRYING TO ACCESS OTHERS' FILE");
        }
        Folder folder = folderResolverService.resolveFolderByUuid(folderUuid);
        // TODO FolderUUID로부터 업로드 권한 확인

        // file_tb에 저장
        File initialFile = File.createInitial(folder.getFolderId(), UUID.randomUUID(), fileTemp.getOriginalFileName(),
                request.getFileName(), request.getDescription(),
                fileTemp.getFileSize(), fileTemp.getFileExtension(), createUserId, fileTemp.getCreateTime());


        fileMapper.insertInitial(initialFile);

        FileWorkflowStatus workflowStatus = FileWorkflowStatus.createReady(initialFile.getFileId());
        fileMapper.insertFileWorkflowStatus(workflowStatus);

        return initialFile;
    }

    /** 외부 호출 헬퍼 메서드
     * 임시파일 테이블에 저장
     * */
    public void insertFileTemp(FileTemp fileTemp){
        fileMapper.insertFileTemp(fileTemp);
    }
    public FileTemp selectFileTempByUuid(UUID targetUuid){
        return fileMapper.selectTempFileByUuid(targetUuid);
    }

    public void completeRegistWorkflow(Long fileId,  LocalDateTime currentTime, Map<String, String> destInfo){

        // 현재 Stroage_mount_table을 사용하지 않음. 임시완성이 현 시점 절대적 목표
        // 따라서 storage_mount_point_id null로 유지
        fileMapper.updateFileMetaInfoByRegistDone(fileId, destInfo.get("destFilePath"), destInfo.get("destFileName"), FileStatus.ACTIVE);
    }

    public void markFileWorkflowRegistStarted(Long fileId, LocalDateTime currentTime){
        fileMapper.updateFileWorkflowStatus(fileId, WorkflowStatus.PROCESSING, currentTime);
    }

    public void markFileWorkflowRegistSucceed(Long fileId, LocalDateTime currentTime){
        fileMapper.updateFileWorkflowEndStatus(fileId, WorkflowStatus.SUCCESS, currentTime);
    }

    public void markFileWorkflowRegistFailed(Long fileId, LocalDateTime startedTime, String failedMessage){
        fileMapper.markFileWorkflowRegistFailed(fileId, WorkflowStatus.FAILED_FINAL, startedTime, failedMessage);
    }

}
