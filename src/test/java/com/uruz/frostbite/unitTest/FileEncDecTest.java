package com.uruz.frostbite.unitTest;


import com.uruz.frostbite.enums.EncryptFormatVersion;
import com.uruz.frostbite.service.DirectFileHandleComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.CipherOutputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("파일 암호화/복호화 테스트")
@ActiveProfiles("LOCAL-DEV-WIN")
@SpringBootTest(properties = {
        "spring.config.import=file:./application-secret.properties"
})
public class FileEncDecTest {

    private final DirectFileHandleComponent directFileHandleComponent;

    @Value("${frostbite.multipart.temp.location}")
    private String FROSTBITE_TEMP_LOCATION;

    @Autowired
    public FileEncDecTest(DirectFileHandleComponent directFileHandleComponent) {
        this.directFileHandleComponent = directFileHandleComponent;
    }

    @Test
    @DisplayName("ENC TEST")
    public void fileShouldEncrypted() throws Exception{
        UUID srcFileUuid = UUID.randomUUID();
        UUID destFileUuid = UUID.randomUUID();
        String[] uuids = destFileUuid.toString().split("-");
        String attemptFileName = uuids[4];
        String attemptDestPath = directFileHandleComponent.resolvePersistFilePath(destFileUuid, attemptFileName);
        String testString = "테스트텍스트입니다.";
        try{
            createTestInputFile(srcFileUuid, testString);
        } catch(Exception e){
            throw  e;
        }

        File destFile = new File(attemptDestPath+"/"+attemptFileName);

        try{
            directFileHandleComponent.encryptFileToPersistPath(srcFileUuid,
                    attemptDestPath, attemptFileName, EncryptFormatVersion.AES256GCM);

            try(FileInputStream fileInputStream1 = new FileInputStream((destFile));
                BufferedInputStream bf = new BufferedInputStream(fileInputStream1)){

                byte[] encByte = bf.readAllBytes();
                System.out.println(new String(encByte, StandardCharsets.UTF_8));
                // 혹은 HEX 출력
                /*
                System.out.println(
                        new String(encByte, 0, 4, StandardCharsets.US_ASCII)
                );

                System.out.printf("Version : %02X%n", bytes[4]);
                System.out.printf("Algo    : %02X%n", bytes[5]);
                 */
                //assertThat(encString).isNotEqualTo(testString.getBytes());
                UUID decryptedUUID = UUID.randomUUID();
                String[] decUuids = decryptedUUID.toString().split("-");
                String attemptDecFileName = decUuids[4];
                String attemptDecryptedPath = directFileHandleComponent.resolvePersistFilePath(decryptedUUID, attemptDecFileName);
                String attemptDecryptedFullPath = attemptDecryptedPath + "/" + attemptDecFileName;
                File attemptDecryptedFile = new File(attemptDecryptedFullPath);
                directFileHandleComponent.decryptToFile(destFile,attemptDecryptedFile);
                InputStream in = new FileInputStream(attemptDecryptedFullPath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String text = reader.readLine();

                assertThat(text).isEqualTo(testString);

                reader.close();
                in.close();
            } catch(Exception internalE){
                throw internalE;
            }

        } catch(Exception e){
            throw e;
        }




    }

    private File createTestInputFile(UUID srcFileUuid, String testString) throws Exception{
        String[] uuids = srcFileUuid.toString().split("-");
        String attemptFileName = uuids[4];
        File tempFilePath = new File(FROSTBITE_TEMP_LOCATION + srcFileUuid);
        tempFilePath.createNewFile();
        //Files.write(file.toPath(), "TEST".getBytes(StandardCharsets.UTF_8));

        try(FileOutputStream fileOut = new FileOutputStream(tempFilePath);
            //BufferedWriter writer = Files.newBufferedWriter(tempFilePath.toPath());
            ){
            //writer.write(testString);
            fileOut.write(testString.getBytes(StandardCharsets.UTF_8));
        }catch(FileNotFoundException fne){

            throw fne;
        } catch(IOException ioe){
            throw ioe;
        }

        return tempFilePath;
    }

}
