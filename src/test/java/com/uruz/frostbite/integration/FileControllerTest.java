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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${frostbite.multipart.temp.location}")
    private String tempLocation;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("파일 등록 요청 시 암호화 UnitTask까지 정상 수행한다")
    void registerFileShouldExecuteEncryptionWorkflow() throws Exception {

        /**
         아래 내지는 별도 캐스팅 필요가 없는애들이라면,
         (USER) 는 UUID <-> String casoint 문제가 있음
         User user = jdbcTemplate.queryForObject("""
         SELECT email, account_type, uuid, user_id, status
         FROM user_tb
         WHERE email = ?
         """,
         BeanPropertyRowMapper.newInstance(User.class),
         "hkglh12@gmail.com");
         * */
        User user = jdbcTemplate.queryForObject(
                """
                SELECT email, account_type, uuid, user_id, status
                FROM user_tb
                WHERE email = ?
                """,
                (rs, rowNum) -> {
                    User userInAnnon = new User();
                    userInAnnon.setEmail(rs.getString("email"));
                    userInAnnon.setAccountType(AccountType.fromValue(rs.getInt("account_type")));
                    userInAnnon.setUuid(UUID.fromString(rs.getString("uuid")));
                    userInAnnon.setUserId(rs.getInt("user_id"));
                    userInAnnon.setStatus(UserStatus.fromValue(rs.getInt("status")));
                    return userInAnnon;
                },
                "hkglh12@gmail.com"
        );



        CustomUserDetails principal = CustomUserDetails.fromUser(user);

        KeyHolder keyHolder = new GeneratedKeyHolder();


        FileCreateRequest request = new FileCreateRequest();
        request.setTempFileUuid(UUID.fromString("1a1a27db-d8ef-45fc-9ec1-40301e64c227"));
        request.setFolderUuid(UUID.fromString("e6acf976-7c57-4a50-88c2-55028e9e6414"));
        request.setFileName("TESTFILE");
        request.setDescription("테스트 파일입니다");
        request.setWorkflowName(Workflow.WF_FILE_REGISTER_ONLY);


        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                        INSERT INTO file_temp_tb(
                        target_file_uuid, original_file_name, file_size, create_user_id, create_time, expire_time, status)
                        VALUES (?,?,?,?,?,?,?)
                    """, PreparedStatement.RETURN_GENERATED_KEYS
            );
            ps.setObject(1,request.getTempFileUuid());
            ps.setString(2, request.getFileName());
            ps.setLong(3, 28L);
            ps.setInt(4, user.getUserId());
            ps.setObject(5, LocalDateTime.now());
            ps.setObject(6, LocalDateTime.now().plus(1, ChronoUnit.HOURS));
            ps.setInt(7, FileStatus.ACTIVE.getValue());

            return ps;
        }, keyHolder);


        FileTemp fileTemp = FileTemp.createPreparedTempFile(
            request.getTempFileUuid(), "TESFILE", 28L,
                user.getUserId(), 360L
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        MvcResult result = mockMvc.perform(
                        post("/v1/file/e6acf976-7c57-4a50-88c2-55028e9e6414")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                )
                .andExpect(status().isOk())
                .andReturn();
    }

}
