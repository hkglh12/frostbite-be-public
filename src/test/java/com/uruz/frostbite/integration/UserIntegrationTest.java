package com.uruz.frostbite.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.dto.UserJoinRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── 테스트 픽스처 ──────────────────────────────────────────────────────────
    private static final String TEST_NAME     = "test-1-1";
    private static final String TEST_EMAIL    = "test-1-1@test.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_PHONE    = "01012345678";
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
