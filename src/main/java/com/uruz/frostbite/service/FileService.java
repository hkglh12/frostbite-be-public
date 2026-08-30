package com.uruz.frostbite.service;


import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.domain.File;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.FileWorkflowStatus;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.dto.file.FileMetaModifyCommand;
import com.uruz.frostbite.dto.file.FileMetaResponse;
import com.uruz.frostbite.dto.file.FileMoveCommand;
import com.uruz.frostbite.dto.file.internal.InternalFileMoveCommand;
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.FileTempStatus;
import com.uruz.frostbite.enums.WorkflowStatus;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.mapper.FileMapper;
import com.uruz.frostbite.service.resolver.FolderResolverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    /** 임시파일 -> 파일 DB에 등록 + 파일 워크플로우 상태 테이블 등록 */
    public File registerFileMeta(FileCreateRequest request, UUID folderUuid, Integer createUserId){
        // Temp UUID로부터 유효한 UUID인지 검증`
        FileTemp fileTemp = fileMapper.selectReadyTempFileByUuid(request.getTempFileUuid(), FileTempStatus.READY);
        if(fileTemp == null){
            throw new RequestProcessingException(ApiErrorCode.NOT_FOUND);
        }
        if(!createUserId.equals(fileTemp.getCreateUserId())){
            throw new RequestProcessingException(ApiErrorCode.RESOURCE_SCOPE_DENIED);
        }

        Folder folder = folderResolverService.resolveAccessibleFolderByFolderUuid(folderUuid, createUserId).orElseThrow( () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND));
        // TODO FolderUUID로부터 업로드 권한 확인

        // file_tb에 저장
        File initialFile = File.createInitial(folder.getFolderId(), UUID.randomUUID(), fileTemp.getOriginalFileName(),
                fileTemp.getOriginalFileName(), request.getDescription(),
                fileTemp.getFileSize(), createUserId, fileTemp.getCreateTime());

        // 파일 등록
        fileMapper.insertInitial(initialFile);


        // 파일 워크플로우 상태 1:1 row 등록
        FileWorkflowStatus workflowStatus = FileWorkflowStatus.createWaiting(initialFile.getFileId());
        fileMapper.insertFileWorkflowStatus(workflowStatus);

        // PK 외부 노출 금지
        initialFile.setFileId(null);
        return initialFile;
    }


    /** 파일 메타 제어 */
    // 파일 메타 수정
    public FileMetaResponse modifyFileMeta(Integer userId, FileMetaModifyCommand command){

        //TODO 파일을 대상으로 작업하려면, 폴더에 권한이 있는지 확인해야 한다.
        // Phase2에서 파일에 직접적인 Deny 권한이 있는지 확인해야 한다.
        LocalDateTime currentTime = LocalDateTime.now();

        FileMetaModifyCommand requiredArgsFilled = command.fillRequired(userId, currentTime);
        int affectedRow = fileMapper.updateFileMeta(requiredArgsFilled);

        return fileMapper.selectFileMeta(command.getUuid());
    }

    // 파일 폴더 이동
    public FileMetaResponse moveFile(Integer userId, FileMoveCommand command){

        Folder originalFolder = folderResolverService.resolveAccessibleFolderByFolderUuid(command.getOriginalFolderUuid(), userId).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );

        Folder targetFolder = folderResolverService.resolveAccessibleFolderByFolderUuid(command.getTargetFolderUuid(), userId).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );

        // TODO 파일 대상으로 작업하려면, 원본폴더와 타겟폴더 권한을 검사해야한다.
        // Phase2에서 파일에 직접적인 Deny 권한이 있는지 확인해야 한다.
        LocalDateTime currentTime = LocalDateTime.now();

        InternalFileMoveCommand requiredArgsFilled = InternalFileMoveCommand.convertInteralDto(command, originalFolder.getFolderId(), targetFolder.getFolderId(), userId, currentTime);

        int affectedRow = fileMapper.moveFile(requiredArgsFilled);

        return fileMapper.selectFileMeta(command.getUuid());
    }

    // 파일 메타 조회
    public FileMetaResponse getFileMeta(Integer userId, UUID folderUuid, UUID fileUuid){
        // TODO 폴더에 권한이 있는지 확인해야 한다.
        // Phase2에서 파일에 직접적인 Deny 권한이 있는지 확인해야 한다.
        return fileMapper.selectFileMeta(fileUuid);
    }

    // 파일 softDelete
    public void softDeleteFile(Integer userId, UUID folderUuid, UUID fileUuid){
        // TODO 폴더에 권한이 있는지 확인해야 한다.
        // Phase2에서 파일에 직접적인 Deny 권한이 있는지 확인해야 한다.
        fileMapper.updateForSoftDeletion(fileUuid, FileStatus.DELETED);
    }

    /** 임시파일 테이블에 임시파일 정보 등록 */
    public void insertFileTemp(FileTemp fileTemp){
        fileMapper.insertFileTemp(fileTemp);
    }
    public FileTemp selectFileTempByUuid(UUID targetUuid){
        return fileMapper.selectTempFileByUuid(targetUuid);
    }

    public FileTemp selectReadyFileTempByUuid(UUID targetUuid){
        return fileMapper.selectReadyTempFileByUuid(targetUuid, FileTempStatus.READY);
    }

    public void markFileWorkflowRegisterStarted(Long fileId, LocalDateTime currentTime){
        fileMapper.updateFileWorkflowRegisterStatusProcessing(fileId, WorkflowStatus.PROCESSING, currentTime);
    }

    public void markFileWorkflowRegisterSucceed(Long fileId, LocalDateTime currentTime){
        fileMapper.updateFileWorkflowRegisterStatusSuccess(fileId, WorkflowStatus.SUCCESS, currentTime);
    }

    public void markFileWorkflowRegisterFailed(Long fileId, LocalDateTime startedTime, String failedMessage){
        fileMapper.updateFileWorkflowRegisterStatusFailed(fileId, WorkflowStatus.FAILED_FINAL, startedTime, failedMessage);
    }
    // 파일 등록 자체는 종료. 그러나 파일 워크플로우는
    /** 파일 Storage 이관 종료 시 -> 파일 등록 자체는 완료된것으로 판단. 파일 워크플로우는 여전히 진행중이므로 PROCESSING으로 update*/
    public void completeRegistWorkflow(Long fileId,  LocalDateTime currentTime, String destFilePath, String destFileName){
        fileMapper.updateFileMetaInfoByRegistDone(fileId, destFilePath, destFileName, FileStatus.PROCESSING);
    }


    public void markFileActive(Long fileId){
        fileMapper.markFileAsActive(fileId, FileStatus.ACTIVE);
    }

}
