package com.uruz.frostbite.integration;

import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.dto.team.TeamCreateRequest;
import com.uruz.frostbite.dto.team.TeamDetailResponse;
import com.uruz.frostbite.dto.team.TeamModifyRequest;
import com.uruz.frostbite.enums.team.TeamStatus;
import com.uruz.frostbite.service.TeamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TeamService 서비스 레이어 통합 테스트
 *
 * 테스트 계정: hkglh12@gmail.com (user_id=7, uuid=cb548410-c81f-4c82-8ebb-299eaeff8362)
 *
 * [Controller 레이어 인증 정보]
 * Controller의 @AuthenticationPrincipal 파라미터는 CustomUserDetails(details) 또는
 * @AuthenticationPrincipal(expression="uuid") UUID 형태로 사용된다.
 * 서비스 메서드는 UUID만 직접 받으므로 서비스 테스트에서는 UUID를 직접 주입한다.
 * MockMvc 기반의 Controller 레이어 테스트가 필요한 경우 아래의 더미 빌더를 참고하여
 * SecurityMockMvcRequestPostProcessors.user(dummyDetails)로 주입할 것.
 *
 * [트랜잭션 전략]
 * @Transactional 선언으로 각 테스트 메서드 종료 후 자동 롤백.
 * webEnvironment=MOCK 환경에서 MockMvc 없이 서비스를 직접 호출하므로
 * 트랜잭션 컨텍스트가 테스트 메서드 전체에 걸쳐 공유된다.
 * 롤백으로 DB 레코드가 자동 삭제되어 별도의 teardown이 불필요하다.
 *
 * [전제 조건]
 * - PostgreSQL (localhost:9413) 및 Docker 컨테이너가 실행 중이어야 한다.
 * - user_tb에 uuid=cb548410-c81f-4c82-8ebb-299eaeff8362 (user_id=7) 레코드가 존재해야 한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("TeamService 통합 테스트 - hkglh12 계정")
class TeamServiceTest {

    @Autowired
    private TeamService teamService;

    // ── 테스트 픽스처 ──────────────────────────────────────────────────────────
    private static final UUID TEST_USER_UUID =
            UUID.fromString("cb548410-c81f-4c82-8ebb-299eaeff8362");
    private static final String TEST_TEAM_NAME          = "service-test-team";
    private static final String TEST_TEAM_NAME_MODIFIED = "service-test-team-modified";

    private static final Integer TEAM_USER_ID = 0;

    /**
     * Controller 레이어 테스트 시 @AuthenticationPrincipal CustomUserDetails 가 필요한 경우
     * 아래 더미 details를 참고하여 사용한다. (서비스 레이어 테스트에서는 사용하지 않음)
     *
     * <pre>
     *   CustomUserDetails dummyDetails = CustomUserDetails.fromUser(
     *       User.createForTest(
     *           7,                                                     // userId
     *           "hkglh12@gmail.com",                                  // email
     *           "dummyPassword",                                       // password
     *           UUID.fromString("cb548410-c81f-4c82-8ebb-299eaeff8362") // uuid
     *       )
     *   );
     *   mockMvc.perform(get("/v1/api/team/")
     *           .param("uuid", teamUuid.toString())
     *           .with(SecurityMockMvcRequestPostProcessors.user(dummyDetails)));
     * </pre>
     */
    private TeamCreateRequest buildCreateRequest(String name) {
        TeamCreateRequest req = new TeamCreateRequest();
        req.setName(name);
        return req;
    }

    // ── 테스트 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("[createTeam] 팀 생성 시 요청한 이름과 동일한 팀 정보가 반환된다")
    void createTeam_returns_team_with_requested_name() throws Exception {
        TeamCreateRequest request = buildCreateRequest(TEST_TEAM_NAME);

        TeamDetailResponse response = teamService.createTeam(TEAM_USER_ID, request);

        assertNotNull(response,                         "응답이 null이어서는 안 됩니다");
        assertEquals(TEST_TEAM_NAME, response.getName(),"팀 이름이 요청과 일치해야 합니다");
        assertNotNull(response.getUuid(),               "UUID가 생성되어야 합니다");
        assertNotNull(response.getCreateTime(),         "생성시각이 있어야 합니다");
        assertNotNull(response.getCreateUserName(),     "생성자 이름이 있어야 합니다");
        assertEquals(TeamStatus.ACTIVE, response.getStatus(), "초기 상태는 ACTIVE여야 합니다");
    }

    @Test
    @DisplayName("[getTeamDetail] 팀 생성 후 상세 조회 시 동일한 팀 정보가 반환된다")
    void getTeamDetail_returns_same_info_as_created() throws Exception {
        TeamDetailResponse created = teamService.createTeam(TEAM_USER_ID, buildCreateRequest(TEST_TEAM_NAME));

        TeamDetailResponse detail = teamService.getTeamDetail(created.getUuid());

        assertNotNull(detail,                                              "응답이 null이어서는 안 됩니다");
        assertEquals(created.getName(),           detail.getName(),       "팀 이름이 생성 시와 일치해야 합니다");
        assertEquals(created.getUuid(),           detail.getUuid(),       "팀 UUID가 일치해야 합니다");
        assertEquals(created.getStatus(),         detail.getStatus(),     "팀 상태가 일치해야 합니다");
        assertEquals(created.getCreateUserName(), detail.getCreateUserName(), "생성자 이름이 일치해야 합니다");
    }

    @Test
    @DisplayName("[modifyTeam] 팀 이름 수정 요청 시 수정된 이름과 수정 감사정보가 반환된다")
    void modifyTeam_returns_updated_name_and_audit_info() throws Exception {
        TeamDetailResponse created = teamService.createTeam(TEAM_USER_ID, buildCreateRequest(TEST_TEAM_NAME));

        TeamModifyRequest modifyRequest = TeamModifyRequest.builder()
                .uuid(created.getUuid())
                .name(TEST_TEAM_NAME_MODIFIED)
                .build();

        TeamDetailResponse modified = teamService.modifyTeam(modifyRequest, TEAM_USER_ID);

        assertNotNull(modified,                                                "응답이 null이어서는 안 됩니다");
        assertEquals(TEST_TEAM_NAME_MODIFIED, modified.getName(),             "수정된 팀 이름이 반환되어야 합니다");
        assertNotNull(modified.getModifyTime(),                               "수정시각이 설정되어야 합니다");
        assertNotNull(modified.getModifyUserName(),                           "수정자 이름이 있어야 합니다");
        assertEquals(created.getCreateTime(),    modified.getCreateTime(),    "생성시각은 변경되지 않아야 합니다");
        assertEquals(created.getCreateUserName(), modified.getCreateUserName(), "생성자는 변경되지 않아야 합니다");
    }

    @Test
    @DisplayName("[softDeleteTeam] 팀 삭제 요청 시 팀 상태가 DELETED로 변경된다")
    void softDeleteTeam_changes_team_status_to_deleted() throws Exception {
        TeamDetailResponse created = teamService.createTeam(TEAM_USER_ID, buildCreateRequest(TEST_TEAM_NAME));
        assertEquals(TeamStatus.ACTIVE, created.getStatus(), "삭제 전 상태는 ACTIVE여야 합니다");

        teamService.softDeleteTeam(created.getUuid(), TEAM_USER_ID);

        Team deletedTeam = teamService.getTeam(created.getUuid());
        assertNotNull(deletedTeam,                                            "삭제 후에도 레코드는 존재해야 합니다");
        assertEquals(TeamStatus.DELETED, deletedTeam.getStatus(),            "팀 상태가 DELETED여야 합니다");
    }
}
