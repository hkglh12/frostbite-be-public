package com.uruz.frostbite.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.dto.user.UserJoinRequest;
import com.uruz.frostbite.enums.AccessPolicy;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * User 가입/조회 통합 테스트
 *
 * 테스트 계정 식별자: test-1-1 (name 필드로 구분)
 *
 * [UUID 설계 결정]
 * User.uuid 는 서비스 레이어에서 UUID.randomUUID() 로 자동 생성된다.
 * "test-1-1" 은 java.util.UUID 포맷(xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)이 아니므로
 * 직접 주입 불가 → 가입 응답에서 uuid 를 추출해 조회 단계에 사용한다.
 *
 * [트랜잭션 전략]
 * @Transactional 선언으로 각 테스트 메서드 종료 후 자동 롤백.
 * webEnvironment=MOCK 환경에서는 MockMvc 호출이 동일 스레드에서 처리되므로
 * 트랜잭션 컨텍스트가 테스트 메서드 전체에 걸쳐 공유된다.
 *
 * [전제 조건]
 * - PostgreSQL (localhost:9413) 및 Docker 컨테이너가 실행 중이어야 한다.
 * - Redis 세션은 application-test.properties 에서 비활성화(store-type=none).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("User 통합 테스트 - test-1-1 계정")
class UserIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;



    private static final String TEST_NAME     = "TestUser_Intg_folder";
    private static final String TEST_EMAIL    = "test-1-1@test.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_PHONE    = "01012345678";
    // 사용자 가입 -> 폴더 생성 -> folderAcl 생성
    @Rollback
    @Test
    @DisplayName("[가입] 사용자 생성 시 개인 엔트리 폴더와 ACL이 함께 생성된다")
    void createUserWithFolderAndAcl(){

        // GIVEN
        UserJoinRequest request = buildTestRequest();

        // WHEN
        userService.createUser(request);

        // THEN
        // 단일값, 단일 Row 조회 -> 단일값 +  queryForObject,
        Integer userId = jdbcTemplate.queryForObject("select user_id from user_tb where name=? and email =?", Integer.class,TEST_NAME, TEST_EMAIL);

        assertThat(userId).as("사용자 가입 PK는 DB Serial로 자동생성되어야 한다.").isNotNull();

        // 단일 값, 다중 Row 조회 -> 단일리턴 + 다중 collection + query
        List<Long> folderIds = jdbcTemplate.query(
                "SELECT folder_id FROM folder_tb WHERE resource_scope_user_id = ? and folder_type = ? and parent_folder_id is null",
                (rs, rowNum) -> rs.getLong("folder_id"),
                userId,
                FolderType.PRIVATE.getValue()
        );

        assertThat(folderIds)
                .as("단일 가입 시 폴더는 1개 생성되어야 한다.")
                .hasSize(1);


        // JDBC Template에는 Typehandler가 동작 안한다. 유의할것. TYpehandler는 Mybatis꺼임
        // 다중 값, 단일 Row 조회 -> MapReturn + queryForMap
        Map<String, Object> folderRow = jdbcTemplate.queryForMap(
                "SELECT folder_id, name, folder_type, resource_scope_user_id, access_policy from folder_tb where resource_scope_user_id = ? and folder_type = ? and parent_folder_id is null",
                userId,
                FolderType.PRIVATE.getValue()
        );

        assertThat(((Number) folderRow.get("resource_scope_user_id")).intValue())
                .as("UserId가 resourceScopeUserId로 들어갔는지 확인")
                .isEqualTo(userId);

        assertThat(((Number) folderRow.get("folder_type")).intValue())
                .as("folderType은 PRIVATE이어야 한다")
                .isEqualTo(FolderType.PRIVATE.getValue());

        assertThat(((Number) folderRow.get("access_policy")).intValue())
                .as("개인 폴더 accessPolicy는 USER여야 한다")
                .isEqualTo(AccessPolicy.USER.getValue());

        Long folderId = ((Number) folderRow.get("folder_id")).longValue();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                        SELECT acl_id, folder_id, subject_type, effect_type, subject_team_id, subject_user_id, permission_bit_mask, status from folder_acl_tb
                        WHERE folder_id = ? and subject_user_id = ?
                    """,
                folderId, userId
        );

        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.getFirst();
        assertThat(((Number) row.get("subject_user_id")).intValue())
                .as("PRIVATE 폴더이므로 subject_user_id는 userId와 같아야 함")
                .isEqualTo(userId);

        assertThat((Number)(row.get("subject_user_id")))
                .as("PRIVATE 폴더이므로 subject_user_id는 userId와 같아야함").isEqualTo(userId);

        Integer permissionBitMask = ((Number) row.get("permission_bit_mask")).intValue();

        assertThat(PermissionBitMask.has(permissionBitMask, PermissionBitMask.BROWSE)).isTrue();
        assertThat(PermissionBitMask.has(permissionBitMask, PermissionBitMask.READ)).isTrue();
        assertThat(PermissionBitMask.has(permissionBitMask, PermissionBitMask.WRITE)).isTrue();
        assertThat(PermissionBitMask.has(permissionBitMask, PermissionBitMask.CONTROL)).isTrue();
        assertThat(PermissionBitMask.has(permissionBitMask, PermissionBitMask.MANAGE)).isTrue();
    }


    // ── 테스트 픽스처 ──────────────────────────────────────────────────────────

    private static final LocalDate TEST_BIRTH = LocalDate.of(1990, 1, 1);

    /** test-1-1 계정 가입 요청 객체 생성 */
    private UserJoinRequest buildTestRequest() {
        UserJoinRequest req = new UserJoinRequest();
        req.setName(TEST_NAME);
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);
        req.setPhoneNumber(TEST_PHONE);
        req.setBirth(TEST_BIRTH);
        return req;
    }

    // ── 테스트 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("[가입] test-1-1 계정 생성 요청이 성공하고 응답에 uuid/name/email 이 포함된다")
    void join_test1_1_returns_created_user_info() throws Exception {
        mockMvc.perform(post("/api/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildTestRequest())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCEED"))
                .andExpect(jsonPath("$.result.name").value(TEST_NAME))
                .andExpect(jsonPath("$.result.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.result.phoneNumber").value(TEST_PHONE))
                .andExpect(jsonPath("$.result.uuid").isNotEmpty());
    }

    @Test
    @DisplayName("[가입 → UUID 조회] test-1-1 계정을 가입 후 반환된 UUID 로 조회하면 동일 정보가 반환된다")
    void join_then_queryByUuid_returns_same_user() throws Exception {

        // Step 1. 가입 요청
        String joinBody = mockMvc.perform(post("/api/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildTestRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Step 2. 가입 응답에서 서버가 생성한 UUID 추출
        String uuid = objectMapper.readTree(joinBody)
                .path("result").path("uuid").asText();

        // Step 3. UUID 로 단건 조회
        mockMvc.perform(get("/api/user/")
                        .param("uuid", uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCEED"))
                .andExpect(jsonPath("$.result.uuid").value(uuid))
                .andExpect(jsonPath("$.result.name").value(TEST_NAME))
                .andExpect(jsonPath("$.result.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.result.phoneNumber").value(TEST_PHONE));
    }
}
