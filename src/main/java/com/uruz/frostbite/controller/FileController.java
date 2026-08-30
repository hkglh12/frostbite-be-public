package com.uruz.frostbite.controller;

import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.dto.file.*;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.enums.common.ApiSucceedCode;
import com.uruz.frostbite.service.FileOrchestrationService;
import com.uruz.frostbite.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1/file")
public class FileController {

    // 워크플로우, UnitTask등을 태울 때, 중추로 활용할 서비스
    private final FileOrchestrationService fileOrchestrationService;
    // 파일 메타정보를 건드릴때 사용하는 서비스
    private final FileService fileService;
    @Autowired
    public FileController(FileOrchestrationService fileOrchestrationService, FileService fileService){
        this.fileOrchestrationService = fileOrchestrationService;
        this.fileService = fileService;
    }

    /** 임시 파일 업로드 포인트
     *
     * MultiPart(java.temp.io) -> forstbite temp까지 이동
     * */
    @PostMapping(value = "/{folderUuid}/temporal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTemporalFile(
            @RequestPart("file") MultipartFile file,
            @PathVariable(name="folderUuid") UUID folderUuid,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ){

        TempFileMetaInfoDraft tempFileDraft = fileOrchestrationService.registerTempFile(file, folderUuid, userId);

        return ResponseEntity.ok(new ApiSucceedResponse<>(tempFileDraft));
    }

    /** 파일 등록 포인트*/
    @PostMapping(value = "/{folderUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadFile(
            @Validated @RequestBody FileCreateRequest request,
            // 이미 tempFile에 UUID가 있으나, URL상에서 직관성 좋게 표현되는것도 중요하다고 판단, path variable로 유지
            @PathVariable(name="folderUuid") UUID folderUuid,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) throws IOException {
        SimpleFileResponse response = fileOrchestrationService.registerFile(request, folderUuid,  userId);
        return ResponseEntity.ok(new ApiSucceedResponse<>(response, ApiSucceedCode.REQUEST_SUCCEED));
    }

    // 파일 메타 조회
    @GetMapping(value = "/{folderUuid}/{fileUuid}/meta")
    public ResponseEntity<?> getFileMeta(
            @PathVariable(name="folderUuid") UUID folderUuid,
            @PathVariable(name ="fileUuid") UUID fileUuid,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) throws RequestProcessingException {
        FileMetaResponse response = fileService.getFileMeta(userId, folderUuid, fileUuid);

        return ResponseEntity.ok(new ApiSucceedResponse<>(response, ApiSucceedCode.REQUEST_SUCCEED));
    }

    // 파일 메타 수정
    @PutMapping(value = "/{folderUuid}/{fileUuid}/meta")
    public ResponseEntity<?> modifyFileMeta(
            @Validated @RequestBody FileMetaModifyCommand command,
            @PathVariable(name="folderUuid") UUID folderUuid,
            @PathVariable(name ="fileUuid") UUID fileUuid,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) throws RequestProcessingException {
        // 리펙터링 시, httpRequest 수신용 -> command 전환용 DTO 따로만드는건 어떨지?
        command = command.fillPathVariables(fileUuid, folderUuid);
        FileMetaResponse response = fileService.modifyFileMeta(userId, command);

        return ResponseEntity.ok(new ApiSucceedResponse<>(response, ApiSucceedCode.REQUEST_SUCCEED));

    }

    // 파일 이동
    @PutMapping(value = "/{originalFolderUuid}/{fileUuid}/parentFolder")
    public ResponseEntity<?> moveFile(
            @Validated @RequestBody FileMoveCommand command,
            @PathVariable(name="originalFolderUuid") UUID originalFolderUuid,
            @PathVariable(name ="fileUuid") UUID fileUuid,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) throws RequestProcessingException {
        // 리펙터링 시, httpRequest 수신용 -> command 전환용 DTO 따로만드는건 어떨지?
        command = command.fillPathVariables(fileUuid, originalFolderUuid);
        FileMetaResponse response = fileService.moveFile(userId, command);

        return ResponseEntity.ok(new ApiSucceedResponse<>(response, ApiSucceedCode.REQUEST_SUCCEED));

    }

    // 파일 soft delete
    @DeleteMapping(value = "/{folderUuid}/{fileUuid}")
    public ResponseEntity<?> softDeleteFile(
            @PathVariable(name="folderUuid") UUID folderUuid,
            @PathVariable(name ="fileUuid") UUID fileUuid,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) throws RequestProcessingException {
        fileService.softDeleteFile(userId, folderUuid, fileUuid);

        return ResponseEntity.ok(new ApiSucceedResponse<>(null, ApiSucceedCode.REQUEST_SUCCEED));

    }
}
