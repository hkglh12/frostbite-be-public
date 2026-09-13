package com.uruz.frostbite.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.file.FileCreateRequest;
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.user.AccountType;
import com.uruz.frostbite.enums.user.UserStatus;
import com.uruz.frostbite.enums.workflow.Workflow;
import com.uruz.frostbite.security.CustomUserDetails;
import com.uruz.frostbite.service.UserService;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("LOCAL-DEV-WIN")
@SpringBootTest(properties = {
        "spring.config.import=file:./application-secret.properties"
})
@AutoConfigureMockMvc
@DisplayName("파일 등록 Workflow 통합 테스트")
@Slf4j
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${frostbite.multipart.temp.location}")
    private String tempLocation;

    @Autowired
    protected JdbcTemplate jdbcTemplate;


    private static final String performingUserEmail = "hkglh12@gmail.com";

    private static final String performingUserPassword = "konan415!@";

    private static final String performingTargetFolderUuid = "f5ee3679-38f4-4d1d-9aaa-28949022778e";

    @Test
    @DisplayName("파일 정식 등록 흐름을 통해 암호화가 끝난 파일이 스토리지에 등록이 되어야 한다.")
    void formalRegisterFileShouldReturnEncryptedFileAndRegisteredIntoDatabase() throws Exception{
        Cookie sessionCookie = performingLoginSupportSessionCookie();
        Map<String, String> fileRegisterRequest = new HashMap<>();
        fileRegisterRequest.put("tempFileUuid", "c09ae422-5979-439f-b710-fb7a58153f27");
        MvcResult result = mockMvc.perform(
                post("/v1/file/"+performingTargetFolderUuid)
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileRegisterRequest))
                        .with(csrf())
        ).andExpect(status().isOk()).andReturn();

        log.info(result.toString());
    }

    @Test
    @DisplayName("파일 업로드 시 임시파일로 등록되어야 하며, 해당 UUID명의 파일이 임시경로에 존재해야 한다.")
    void uploadFileShouldBeConvertedIntoTemporalFileAndRegistered() throws Exception {

        //MockHttpSession session = performingLoginSupport();
        Cookie sessionCookie = performingLoginSupportSessionCookie();
        Path path = Path.of("C:\\Users\\hkglh\\Projects\\sideProject\\swagger.json");
        MockMultipartFile file = new MockMultipartFile(
                "file", // controller Parameter name
                path.getFileName().toString(), // 파일이름
                MediaType.APPLICATION_OCTET_STREAM_VALUE,   // 미디어 타입
                Files.newInputStream(path)      // 실제 파일 InputStream
        );


        mockMvc.perform(
                multipart("/v1/file/"+performingTargetFolderUuid+"/temporal")
                        .file(file)
                        .cookie(sessionCookie)
                        .with(csrf())
        ).andExpect(status().isOk());

    }

    private Cookie performingLoginSupportSessionCookie() throws Exception{
        Map<String, String> loginRequest = Map.of(
                "email", performingUserEmail,
                "password", performingUserPassword
        );

        MvcResult result = mockMvc.perform(
                post("/v1/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpect(status().isOk()).andReturn();

        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);

        return result.getResponse().getCookie("SESSION");
    }

    // Mock으로 테스트할때 세션이 잘 안붙었음
    private MockHttpSession performingLoginSupport() throws Exception{
        Map<String, String> loginRequest = Map.of(
                "email", performingUserEmail,
                "password", performingUserPassword
        );

        MvcResult result = mockMvc.perform(
            post("/v1/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpect(status().isOk()).andReturn();

        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);

        return session;
    }

}
