package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.dto.file.TempFileMetaInfoDraft;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    public String resolvePersistedFilePath(String destUuidPath){
      return PERSIST_PATH + destUuidPath;
    }
    public void deleteTempFile(String uuidString) throws IOException{
        File tempFilePath = new File(FROSTBITE_TEMP_LOCATION + uuidString);
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
    public TempFileMetaInfoDraft reallocateTempFile(MultipartFile tempFile) throws GeneralCustomException {
        String uuidString = UUID.randomUUID().toString();
        File tempFilePath = new File(FROSTBITE_TEMP_LOCATION + uuidString);

        String originalFileName = tempFile.getOriginalFilename();
        String extension = null;

        try {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch(IndexOutOfBoundsException IOFB) {
            throw new GeneralCustomException("FILE EXTENSION NOT EXISTS");
        }

        long fileSize = tempFile.getSize();

        if(fileSize == 0L || StringUtils.isEmpty(extension)){
            throw new GeneralCustomException("FILE SIZE IS 0 OR FILE EXTENSION NOT EXISTS");
        }

        if(tempFilePath.exists()) {
            uuidString = UUID.randomUUID().toString();
            tempFilePath  = new File(FROSTBITE_TEMP_LOCATION + uuidString);
            if(tempFilePath.exists()){
                throw new GeneralCustomException("UUID PATH DUPLICATED MORE THAN TWICE");
            }
        }
        try {
            tempFile.transferTo(tempFilePath);
            return new TempFileMetaInfoDraft(originalFileName, extension, fileSize, UUID.fromString(uuidString));
        }catch(IOException IOE){
            log.error(IOE.getMessage());
            // multipartFile 은 request 종료시 cleanup 대상. 따라서 transferTo 실패했다면 굳이 명시적으로 삭제할 필요 없음
            throw new GeneralCustomException(IOE.getMessage());
        }
        /*
        String folderPath = uuidString.substring(0, uuidString.lastIndexOf("-")).replace('-', File.separatorChar);

        File tempFolder = new File(FROSTBITE_TEMP_LOCATION+folderPath);

         */
    }

}
