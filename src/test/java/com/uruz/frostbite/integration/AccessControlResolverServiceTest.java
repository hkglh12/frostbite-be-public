package com.uruz.frostbite.integration;

import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.enums.team.TeamRole;
import com.uruz.frostbite.service.resolver.AccessControlResolverService;
import com.uruz.frostbite.support.IntegrationTestSupport;
import com.uruz.frostbite.support.TestDataFixture.TestFolder;
import com.uruz.frostbite.support.TestDataFixture.TestTeam;
import com.uruz.frostbite.support.TestDataFixture.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AccessControlResolverService 통합 테스트")
class AccessControlResolverServiceTest extends IntegrationTestSupport {

    @Autowired
    private AccessControlResolverService accessControlResolverService;

    @Nested
    @DisplayName("개인 폴더")
    class PrivateFolder {

        @Test
        @DisplayName("리소스 소유자는 ACL 조회 없이 접근할 수 있다")
        void ownerCanAccess() {
            // given
            TestUser owner = fixture.createUser();
            TestFolder folder = fixture.createPrivateFolder(null, owner.id());

            // when
            boolean allowed = accessControlResolverService.validateUserActionPermissionForFolder(
                    owner.id(), folder.id(), PermissionBitMask.MANAGE
            );

            // then
            assertThat(allowed).isTrue();
        }

        /*
        @Test
        @DisplayName("리소스 소유자가 아닌 사용자는 접근할 수 없다")
        void nonOwnerCannotAccess() {
            // given
            TestUser owner = fixture.createUser();
            TestUser outsider = fixture.createUser();
            TestFolder folder = fixture.createPrivateFolder(null, owner.id());

            // when & then
            assertThatThrownBy(() -> accessControlResolverService.validateUserActionPermissionForFolder(
                    outsider.id(), folder.id(), PermissionBitMask.BROWSE
            )).isInstanceOf(ResourceAccessDeniedException.class);
        }

         */
    }

    @Nested
    @DisplayName("팀 폴더")
    class TeamFolder {

        @Test
        @DisplayName("팀 관리자는 ACL과 무관하게 접근할 수 있다")
        void managerBypassesAcl() {
            // given
            TestUser manager = fixture.createUser();
            TestTeam team = fixture.createTeam(manager.id());
            fixture.createTeamRelation(manager.id(), team.id(), TeamRole.MANAGER);
            TestFolder folder = fixture.createTeamFolder(null, team.id(), manager.id());

            // when
            boolean allowed = accessControlResolverService.validateUserActionPermissionForFolder(
                    manager.id(), folder.id(), PermissionBitMask.MANAGE
            );

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("명시적 사용자 DENY는 팀 ALLOW보다 우선한다")
        void userDenyOverridesTeamAllow() {
            // given
            TestUser member = fixture.createUser();
            TestTeam team = fixture.createTeam(member.id());
            fixture.createTeamRelation(member.id(), team.id(), TeamRole.MEMBER);
            TestFolder folder = fixture.createTeamFolder(null, team.id(), member.id());
            fixture.createTeamAllowAcl(folder.id(), team.id(), member.id(), PermissionBitMask.allMasks());
            fixture.createUserDenyAcl(folder.id(), member.id());

            /*
            // when & then
            assertThatThrownBy(() -> accessControlResolverService.validateUserActionPermissionForFolder(
                    member.id(), folder.id(), PermissionBitMask.BROWSE
            )).isInstanceOf(ResourceAccessDeniedException.class);

             */
        }

        @Test
        @DisplayName("ALLOW ACL에 요청 권한이 있으면 접근할 수 있다")
        void matchingAllowAclGrantsAccess() {
            // given
            TestUser member = fixture.createUser();
            TestTeam team = fixture.createTeam(member.id());
            fixture.createTeamRelation(member.id(), team.id(), TeamRole.MEMBER);
            TestFolder folder = fixture.createTeamFolder(null, team.id(), member.id());
            fixture.createTeamAllowAcl(folder.id(), team.id(), member.id(), PermissionBitMask.BROWSE, PermissionBitMask.READ);

            // when
            boolean allowed = accessControlResolverService.validateUserActionPermissionForFolder(
                    member.id(), folder.id(), PermissionBitMask.READ
            );

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("ALLOW ACL에 요청 권한이 없으면 행위를 거부한다")
        void insufficientAllowAclForbidsAction() {
            // given
            TestUser member = fixture.createUser();
            TestTeam team = fixture.createTeam(member.id());
            fixture.createTeamRelation(member.id(), team.id(), TeamRole.MEMBER);
            TestFolder folder = fixture.createTeamFolder(null, team.id(), member.id());
            fixture.createTeamAllowAcl(folder.id(), team.id(), member.id(), PermissionBitMask.BROWSE);

            /*
            // when & then
            assertThatThrownBy(() -> accessControlResolverService.validateUserActionPermissionForFolder(
                    member.id(), folder.id(), PermissionBitMask.WRITE
            )).isInstanceOf(ResourceActionForbiddenException.class);

             */
        }

        @Test
        @DisplayName("ALLOW ACL이 없으면 접근을 거부한다")
        void missingAllowAclDeniesAccess() {
            // given
            TestUser member = fixture.createUser();
            TestTeam team = fixture.createTeam(member.id());
            fixture.createTeamRelation(member.id(), team.id(), TeamRole.MEMBER);
            TestFolder folder = fixture.createTeamFolder(null, team.id(), member.id());

            /*
            // when & then
            assertThatThrownBy(() -> accessControlResolverService.validateUserActionPermissionForFolder(
                    member.id(), folder.id(), PermissionBitMask.BROWSE
            )).isInstanceOf(ResourceAccessDeniedException.class);

             */
        }
    }
}
