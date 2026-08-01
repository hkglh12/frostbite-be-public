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

    // 추후 생성자 생성까지 고민해볼것. (현재는 Endpoint 클래스 개념이 되버려서, 주입받을게 없어서 생성자도 없음.
    // 그러나, 테스트코드에서 굳이 SpringBootTest까지 이어붙일필요가 없으니
    // 아래코드 처럼 작성해서, 테스트코드에서 생성자 직접 호출 시 SpringBootTest까지 필요없게됨.
    // 반대로 Spring Context Initializing 과정에서는 스프링이 만들면서 @Value 생성까지 마치고올라옴
    /*
    public DirectFileHandleComponent(
            @Value("${frostbite.multipart.temp.location}") String tempLocation,
            @Value("${frostbite.persist.location}") String persistLocation
    ) {
        this.tempLocation = tempLocation;
        this.persistLocation = persistLocation;
    }

     */


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
    /** File 복호화 */
    @Deprecated(forRemoval = true)
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

    // 구성 변경.
    // OutputStream을 주입받아서 사용할 경우 -> Output Path 폴더가 없으면 OutputStream이 열리지 않음.
    // 이러면 비즈니스 코드에서 폴더의 생성을 보장해야하는데, Encrypt의 경우 경로 생성까지 암호화 흐름이 책임지게 되어있음
    // 이를 따라서, 복호화 역시 폴더 생성까지 책임지도록 외부 진입 메서드 생성, 경로 생성하고 들어오도록 변경
    // 또한, Http Response 와 InternalFile Out 을 구분짓도록 변경
    //  -> AES-GCM은 암호문 변조여부를 파일 끝까지 읽고나서 확인 가능한 구조.
    // 따라서, GCM Tag 검사 시 변조 관련 오류 처리를 따로 해야함
    //  http ->
    //      - 평문 Streaming 시, 막판 tag 문제 발견되면 이미 평문이 일부 내려간 상태일 수 있음
    //          -> 이건 Streaming + AES-GCM 방식을 쓰면 생기는 리스크인데, 현시점(개발단계) 에서는 수용. 추후 DiskIO가 두번이 일어나더라도 내부에서 복호화 끝내고 내려줘야할수도 있음.
    //          - 결국 다운로드 실패를 발생시켜야 하는데, 이미 평문 일부가 내려갔으면 브라우저가 어떻게 동작할지 모름.
    //              - 중간에 끊기면 차라리 다행인데 평문이 복호화 된 만큼 저장될수도. -> 이경우 FE에서 모든 Blob을 내려받은 뒤에 저장하게 하면 될수도. 그러나 대용량 파일이면..또 머리아파짐
    //                  - 일단 다운로드 실패로 인지시키려면 -> DB에 있는 평문 파일 크기를 setContentLength() 를 통해 헤더로 내려줘서 중간에 끊기면 에러로 인식시키기라도 해야함
    //      - httpResponse가 committed() 된 다음이면 사실상 일부 평문 저장된걸 수용/허용 해야하는 상황.. 복호화와 태그검사는 별개로 취급되기 때문
    //          -> 차라리 Internal 복호화를 한 뒤 내려주는거도 방법임
    //TODO  FileOut -> Exception catch, 보상패턴으로 catch에서 삭제

    public void decryptToFile(File encryptedFile, File attemptDecFile) throws Exception{
        if(!attemptDecFile.getParentFile().exists()) {
            attemptDecFile.getParentFile().mkdirs();
        } else if (!attemptDecFile.getParentFile().isDirectory()){
            // 이 케이스는 현실적으로 불가능. 파일을 UUID 기반 Fixed Length로 처리하니까.
        }
        try(FileOutputStream fileOutputStream = new FileOutputStream(attemptDecFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)){
            decryptFileInternal(encryptedFile, bufferedOutputStream);
        }
    }
    public void decryptFileInternal(File encryptedFile, OutputStream outputStream) throws Exception{
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
