package com.uruz.frostbite.integration;

import com.uruz.frostbite.dto.team.TeamCreateRequest;
import com.uruz.frostbite.dto.team.TeamDetailResponse;
import com.uruz.frostbite.dto.team.TeamModifyRequest;
import com.uruz.frostbite.enums.team.TeamStatus;
import com.uruz.frostbite.service.TeamService;
import com.uruz.frostbite.support.IntegrationTestSupport;
import com.uruz.frostbite.support.TestDataFixture.TestTeam;
import com.uruz.frostbite.support.TestDataFixture.TestUser;
import com.uruz.frostbite.support.TestDataQuerySupport.TeamRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TeamService 통합 테스트")
class TeamServiceTest extends IntegrationTestSupport {

    @Autowired
    private TeamService teamService;

    @Test
    @DisplayName("팀 생성 유스케이스는 팀, 생성자 관계, 팀 엔트리 폴더를 함께 생성한다")
    void createTeamCreatesRequiredAggregateState() {
        // given
        TestUser creator = fixture.createUser();
        TeamCreateRequest request = new TeamCreateRequest();
        request.setName("새로운 팀");

        // when
        TeamDetailResponse response = teamService.createTeam(creator.id(), request);

        // then
        TeamRow team = query.findTeam(response.getUuid());
        assertThat(team.name()).isEqualTo(request.getName());
        assertThat(team.status()).isEqualTo(TeamStatus.ACTIVE);
        assertThat(team.createUserId()).isEqualTo(creator.id());
        assertThat(query.countTeamRelations(creator.id(), team.id())).isOne();
        assertThat(query.findEntryFoldersByTeam(team.id())).hasSize(1);

        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getCreateUserName()).isEqualTo(creator.name());
        assertThat(response.getCreateTime()).isNotNull();
    }

    @Test
    @DisplayName("팀 상세 조회는 저장된 팀과 생성자 정보를 반환한다")
    void getTeamDetailReturnsPersistedTeam() {
        // given
        TestUser creator = fixture.createUser();
        TestTeam team = fixture.createTeam(creator.id());

        // when
        TeamDetailResponse response = teamService.getTeamDetail(team.uuid());

        // then
        assertThat(response.getUuid()).isEqualTo(team.uuid());
        assertThat(response.getName()).isEqualTo(team.name());
        assertThat(response.getCreateUserName()).isEqualTo(creator.name());
        assertThat(response.getStatus()).isEqualTo(TeamStatus.ACTIVE);
    }

    @Test
    @DisplayName("팀 이름 수정은 응답과 DB 상태에 반영된다")
    void modifyTeamUpdatesName() {
        // given
        TestUser creator = fixture.createUser();
        TestTeam team = fixture.createTeam(creator.id());
        TeamModifyRequest request = TeamModifyRequest.builder()
                .uuid(team.uuid())
                .name("수정된 팀 이름")
                .build();

        // when
        TeamDetailResponse response = teamService.modifyTeam(request, creator.id());

        // then
        TeamRow persisted = query.findTeam(team.uuid());
        assertThat(persisted.name()).isEqualTo(request.getName());
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getModifyUserName()).isEqualTo(creator.name());
        assertThat(response.getModifyTime()).isNotNull();
    }

    @Test
    @DisplayName("팀 삭제는 레코드를 제거하지 않고 상태를 DELETED로 변경한다")
    void softDeleteTeamChangesStatus() {
        // given
        TestUser creator = fixture.createUser();
        TestTeam team = fixture.createTeam(creator.id());

        // when
        teamService.softDeleteTeam(team.uuid(), creator.id());

        // then
        assertThat(query.findTeam(team.uuid()).status()).isEqualTo(TeamStatus.DELETED);
    }
}
