package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.dto.encrypt.EncryptedFileMagicHeader;
import com.uruz.frostbite.dto.file.TempFileMetaInfoDraft;
import com.uruz.frostbite.enums.EncryptFormatVersion;
import com.uruz.frostbite.utils.CipherUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class DirectFileHandleComponent {
    @Value("${frostbite.multipart.temp.location}")
    private String FROSTBITE_TEMP_LOCATION;

    private static final int BUFFER_SIZE = 8192;

    @Value("${frostbite.secret.encryption.master-key}")
    private String masterAesKeyString;
    private SecretKey masterAesKey;
    private String PERSIST_PATH = "C:/Users/hkglh/Projects/volumes/permenant/";

    @PostConstruct
    public void inspectInitialFolder() throws IllegalStateException{
        File frostbiteRootTempFolder = new File(FROSTBITE_TEMP_LOCATION);
        if(!frostbiteRootTempFolder.exists() || !frostbiteRootTempFolder.isDirectory()){
            throw new IllegalStateException("TEMP ROOT FOLDER PATH NOT EXISTS!");
        }
        if(StringUtils.isEmpty(this.masterAesKeyString)){
            throw new IllegalStateException("MASTER AES KEY IS NULL");
        }
        byte[] keyBytes = Base64.getDecoder().decode(masterAesKeyString);
        if(keyBytes.length != 32){
            throw new IllegalStateException("AES-256 KEY length must be 32");
        }
        this.masterAesKey = new SecretKeySpec(keyBytes, "AES");
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
        try {
            File destFile = new File(destFilePath + "/" + destFileName);
            // 우선 카피, 임시파일 삭제는 후속 워크플로우 단위작업으로 제어
            Files.copy(
                    tempFilePath.toPath(),
                    destFile.toPath()
            );
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

    /** Stream 방식으로 암호화와 동시에 Storage 경로에 파일 저장 */
    public Map<String, String> encryptFileToPersistPath(UUID srcFileUuid, String attemptDestPath,
                                                        String attemptFileName, EncryptFormatVersion encryptFormatVersion) throws Exception{
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
        File destFile = new File(destFilePath + "/" + destFileName+".processing");
        File completedFile = new File(destFilePath + "/" + destFileName);
        byte[] ivByte = CipherUtils.createNewIv(encryptFormatVersion.getIvByteLength());
        AlgorithmParameterSpec spec = CipherUtils.createParameterSpec(encryptFormatVersion, ivByte);
        Cipher cipher = null;

        try{
            cipher = Cipher.getInstance(encryptFormatVersion.getCipherTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, masterAesKey, spec);
        } catch(Exception e){
            // TODO 실패시 작업 개발 필요
            e.printStackTrace();
            throw e;
        }

        try (InputStream inputStream = new FileInputStream(tempFilePath);
                OutputStream outputStream = new FileOutputStream(destFile);
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream)){
            // 1. 평문 헤더 기록
            CipherUtils.writeEncHeader(dataOutputStream, encryptFormatVersion, ivByte);

            // 2. 헤더 이후부터 암호화 본문 기록
            try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, read);
                }
            } catch (Exception e){
                // 실패시 작업 필요
                e.printStackTrace();
                throw e;
            }
        }
        destFile.renameTo(completedFile);
        HashMap<String, String> destInfo = new HashMap<>();

        destInfo.put("persistPath", PERSIST_PATH);
        destInfo.put("destFilePath", destFilePath);
        destInfo.put("destFileName", destFileName);

        return destInfo;
    }

    /*
        * OutputStream으로 File Decrypt 후 바로 Streaming
            - OutputStream이 ResponseStream이면 -> 파일 바로 다운로드 (AES-256-GCM 은 GCM Tag (File 마지막)을 읽기 전까지 위변조여부/실패여부를 판단하기 어려움
            - 그러나, 파일을 Stream으로 복호화 하고 -> 다시 Stream으로 전달할경우 DiskIO *2. 뭐가 더 효율적인지는 고민해봐야겠으나, 현 시점 DiskIO 2번이 더 비효율적으로 판단됨.
            - 또한 임시파일 제거까지 신경써야 되게 됨
            - 서버 스토리지에 있는 암호문이 변조위험이 낮다고 판단, 트레이드오프 상 Disk IO 절약이 더 나을것으로 판단.
         * OutputStream이 FileStream이면 -> 파일에 복호화해서 바로 스트리밍
            - 이 경우, 서버 내 임시파일은 호출자가 처리함을 원칙으로 둠
         * 인자로 받은 Stream은 호출자가 flush 하고 닫음을 원칙으로 함
     */
    /** File 복호화*/
    public void decryptFileToStream(File encryptedFile, OutputStream outputStream) throws Exception{
        try(FileInputStream fileInputStream = new FileInputStream(encryptedFile);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream)){
            EncryptedFileMagicHeader header = CipherUtils.readEncryptedFileMagicHeader(dataInputStream);

            AlgorithmParameterSpec parameterSpec = CipherUtils.createParameterSpec(header.getFormatVersion(), header.getIvBytes());
            Cipher cipher = Cipher.getInstance(header.getFormatVersion().getCipherTransformation());

            cipher.init(Cipher.DECRYPT_MODE, masterAesKey, parameterSpec);

            // CipherInputStream 으로 dataInputStream 인자 사용 : 파일 읽어서 -> CihperInputStream이 read 하는 순간 복호화, outputStream에 write
            // dataInputStream 과 별개로 CipherOutputStream 으로 outputStream 인자 사용
            //  : 파일을 읽어서(.read()) 암호문 획득 -> CipherOutputStream에 암호문 전달 -> 내부에서 복호화 -> outputStream에 복호화한 평문 write
            try(CipherInputStream cipherInputStream = new CipherInputStream(dataInputStream, cipher)){
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = cipherInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }catch(Exception e){
                // TODO 실패시 작업 필요
                e.printStackTrace();
                throw e;
            }
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
