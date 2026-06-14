package com.uruz.frostbite.controller;

import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.dto.file.SimpleFileResponse;
import com.uruz.frostbite.dto.file.TempFileMetaInfoDraft;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.enums.common.ApiSucceedCode;
import com.uruz.frostbite.service.FileOrchestrationService;
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

    private final FileOrchestrationService fileOrchestrationService;
    @Autowired
    public FileController(FileOrchestrationService fileOrchestrationService){
        this.fileOrchestrationService = fileOrchestrationService;
    }

    /** 파일 등록 포인트*/
    @PostMapping(value = "/{folderUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadFile(
            @Validated @RequestBody FileCreateRequest request,
            @PathVariable(name="folderUuid") UUID folderUuid,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) throws IOException {
        SimpleFileResponse response = fileOrchestrationService.uploadFile(request, folderUuid,  userId);
        return ResponseEntity.ok(new ApiSucceedResponse<>(response, ApiSucceedCode.REQUEST_SUCCEED));
    }

    /** 임시 파일 업로드 포인트*/
    @PostMapping(value = "/{folderUuid}/temporal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTemporalFile(
            @RequestPart("file") MultipartFile file,
            @PathVariable(name="folderUuid") UUID folderUuid,
            @AuthenticationPrincipal(expression = "userId") Integer userId
            ){

        TempFileMetaInfoDraft tempFileDraft = fileOrchestrationService.uploadTempFile(file, folderUuid, userId);

        return ResponseEntity.ok(new ApiSucceedResponse<>(tempFileDraft));
    }
}
