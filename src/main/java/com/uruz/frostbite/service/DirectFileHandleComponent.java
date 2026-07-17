package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.dto.file.TempFileMetaInfoDraft;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class DirectFileHandleComponent {
    @Value("${frostbite.multipart.temp.location}")
    private String FROSTBITE_TEMP_LOCATION;

    private String PERSIST_PATH = "C:/Users/hkglh/Projects/volumes/permenant/";

    @PostConstruct
    public void inspectInitialFolder() throws IllegalStateException{
        File frostbiteRootTempFolder = new File(FROSTBITE_TEMP_LOCATION);
        if(!frostbiteRootTempFolder.exists() || !frostbiteRootTempFolder.isDirectory()){
            throw new IllegalStateException("TEMP ROOT FOLDER PATH NOT EXISTS!");
        }
    }

    public Map<String, String> transferFileToPersistPath(UUID srcFileUuid, String attemptDestPath, String attemptFileName){

        // 추후 스토리지 마운트 경로를 DB에 저장해야 한다. 지금은 String으로 관리
        File tempFilePath = new File(FROSTBITE_TEMP_LOCATION + srcFileUuid);

        String destFileName = attemptFileName;
        String destFilePath = attemptDestPath;

        File destDirectory = new File(destFilePath);

        if(destDirectory.exists()){
            if(destDirectory.isFile()) {
                throw new GeneralCustomException("Temporal Folder Path is File");
            }
        } else {
            boolean dirCreateSucceed = destDirectory.mkdirs();
            if(!dirCreateSucceed){
                throw new GeneralCustomException("Temporal Folder path creation failed");
            }
        }
        try(InputStream is = new FileInputStream(tempFilePath)) {
            // 우선 카피, 임시파일 삭제는 후속 워크플로우 단위작업으로 제어
            Files.copy(is, new File(destFilePath + "/" + destFileName).toPath());
            HashMap<String, String> destInfo = new HashMap<>();

            destInfo.put("persistPath", PERSIST_PATH);
            destInfo.put("destFilePath", destFilePath);
            destInfo.put("destFileName", destFileName);

            return destInfo;

        } catch(IOException IOE){
            log.error(IOE.getMessage());
            // multipartFile 은 request 종료시 cleanup 대상. 따라서 transferTo 실패했다면 굳이 명시적으로 삭제할 필요 없음
            throw new GeneralCustomException(IOE.getMessage());
        }
    }
    public String resolvePersistFilePath(UUID destFileUuid, String attemptFileName){
      return PERSIST_PATH + destFileUuid.toString().replace("-" + attemptFileName, "").replace("-", "/");
    }
    public void deleteTempFile(UUID fileUuid) throws IOException{
        File tempFilePath = new File(FROSTBITE_TEMP_LOCATION + fileUuid.toString());
        try{
            Files.deleteIfExists(tempFilePath.toPath());
        }catch(IOException e){
            throw e;
        }
    }
    public void deleteFile(String path) throws IOException{
        File tempFilePath = new File(path);
        try{
            Files.deleteIfExists(tempFilePath.toPath());
        }catch(IOException e){
            throw e;
        }
    }
    /** MultipartFile Spring 임시폴더 경로에서 Application 임시 폴더로 이동*/
    public TempFileMetaInfoDraft reallocateTempFile(MultipartFile multipartFile) throws GeneralCustomException {
        // yml 임시폴더겨올 + UUID를 통한 임시파일
        UUID applicationTempFileUuid = UUID.randomUUID();
        File tempFile = new File(resolveApplicationTempFilePathByUuid(applicationTempFileUuid).toString());

        String originalFileName = multipartFile.getOriginalFilename();
        long fileSize = multipartFile.getSize();

        if(fileSize == 0L){
            throw new GeneralCustomException("FILE SIZE IS 0");
        }

        if(tempFile.exists()) {
            // UUID 충돌 회피
            applicationTempFileUuid = UUID.randomUUID();
            tempFile  = new File(FROSTBITE_TEMP_LOCATION + applicationTempFileUuid.toString());
            if(tempFile.exists()){
                throw new GeneralCustomException("UUID PATH DUPLICATED MORE THAN TWICE");
            }
        }
        try {
            // 임시파일 경로로 이동
            multipartFile.transferTo(tempFile);
            return new TempFileMetaInfoDraft(originalFileName, fileSize, applicationTempFileUuid);
        }catch(IOException IOE){
            log.error(IOE.getMessage());
            // multipartFile 은 request 종료시 cleanup 대상. 따라서 transferTo 실패했다면 굳이 명시적으로 삭제할 필요 없음
            throw new GeneralCustomException(IOE.getMessage());
        }
    }

    public Path resolveApplicationTempFilePathByUuid(UUID tempFileUuid){
        return Path.of(FROSTBITE_TEMP_LOCATION + tempFileUuid.toString());
    }

}
