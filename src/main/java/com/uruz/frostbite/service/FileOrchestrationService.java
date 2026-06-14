package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.File;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.dto.file.SimpleFileResponse;
import com.uruz.frostbite.dto.file.TempFileMetaInfoDraft;
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.utils.TempFileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;


@Service
public class FileOrchestrationService {

    /**
     * @Async 최초 호출, 파일 도메인 유스케이스 흐름에 따른 호출 메서드 조립자 역할만 수행한다.
     * 일종의 Proxy / Router로 이해하면 간편하다.
     *
     * */
    private final FileService fileService;
    private final WorkflowService workflowService;
    private final TempFileUtils tempFileUtils;


    public FileOrchestrationService(FileService fileService, WorkflowService workflowService, TempFileUtils tempFileUtils){
        this.fileService = fileService;
        this.workflowService = workflowService;
        this.tempFileUtils = tempFileUtils;
    }
    /** 임시파일 정보를 parameter로 file_tb 초안 등록 */
    public SimpleFileResponse uploadFile(FileCreateRequest request, UUID folderUuid, Integer createUserId) throws IOException {

        File initialFile = fileService.uploadFile(request, folderUuid, createUserId);

        // TODO 현재 workflow 설계 전 흐름으로 개발되어있음.
        // workflow 개념이 도입된 이상, unit_work 로 전환해야함.
        //workflowService.createRegistWorkflowRequest(initialFile.getFileId(), createUserId);


        workflowService.registFile(request, initialFile.getFileId(), createUserId);

        SimpleFileResponse response = SimpleFileResponse.builder()
                .uuid(initialFile.getUuid())
                .fileName(initialFile.getFileName())
                .parentFolderUuid(folderUuid)
                .fileStatus(FileStatus.ACTIVE)
                .build();

        return response;
    }

    /** 임시파일 정보 임시파일테이블 등록, workflow를 통해 임시경로 이동*/
    @Transactional
    public TempFileMetaInfoDraft uploadTempFile(MultipartFile file, UUID folderUuid, Integer createUserId){

        // TODO 상위폴더 생성 권한 존재하는지 확인 필요.

        // TODO 현재 workflow 설계 전 흐름으로 개발되어있음.
        // workflow 개념이 도입된 이상, unit_work 로 전환해야함.
        TempFileMetaInfoDraft draft = workflowService.uploadTempFile(file, folderUuid, createUserId);

        FileTemp fileTemp = FileTemp.createReady(draft.getPhysicalFileUuid(), draft.getOriginalFileName(),
                draft.getExtension(), draft.getFileSize(), createUserId, tempFileUtils.getTempFileTTLToSeconds());

        try {
            fileService.insertFileTemp(fileTemp);
        }catch(Exception e){
            // DB Insert 실패 시 copy한 임시파일 삭제필요
            try {
                workflowService.deleteTempFile(draft.getPhysicalFileUuid());
            } catch(Exception EE){
                EE.printStackTrace();
                throw new GeneralCustomException("FILE REMOVE FAILED IN REGIS TEMP FILE FLOW");
            }
            throw new GeneralCustomException("TEMP FILE REGISTRATION FAILED");
        }
        return draft;
    }

}
