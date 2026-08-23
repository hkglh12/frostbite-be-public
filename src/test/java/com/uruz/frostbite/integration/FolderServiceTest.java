package com.uruz.frostbite.integration;

import com.uruz.frostbite.dto.folder.FolderCreateRequest;
import com.uruz.frostbite.dto.folder.SimpleFolder;
import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclSubjectType;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.service.FolderService;
import com.uruz.frostbite.support.IntegrationTestSupport;
import com.uruz.frostbite.support.TestDataFixture.TestFolder;
import com.uruz.frostbite.support.TestDataFixture.TestTeam;
import com.uruz.frostbite.support.TestDataFixture.TestUser;
import com.uruz.frostbite.support.TestDataQuerySupport.AclRow;
import com.uruz.frostbite.support.TestDataQuerySupport.FolderRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FolderService 통합 테스트")
class FolderServiceTest extends IntegrationTestSupport {

    private static final int ALL_PERMISSIONS = PermissionBitMask.of(PermissionBitMask.allMasks());

    @Autowired
    private FolderService folderService;

    @Nested
    @DisplayName("엔트리 폴더 생성")
    class CreateFolderContentFolder {

        @Test
        @DisplayName("개인 엔트리 폴더와 사용자 기본 ACL을 함께 생성한다")
        void createsPrivateEntryFolderAndUserAcl() {
            // given
            TestUser user = fixture.createUser();

            // when
            folderService.createPrivateEntryFolder("개인 엔트리 폴더", user.id());

            // then
            FolderRow folder = only(query.findEntryFoldersByUser(user.id()));
            assertPrivateFolder(folder, null, user.id());
            assertUserAllowAllAcl(only(query.findAclsByFolder(folder.id())), folder.id(), user.id());
        }

        @Test
        @DisplayName("팀 엔트리 폴더와 팀 기본 ACL을 함께 생성한다")
        void createsTeamEntryFolderAndTeamAcl() {
            // given
            TestUser creator = fixture.createUser();
            TestTeam team = fixture.createTeam(creator.id());

            // when
            folderService.createTeamEntryFolder("팀 엔트리 폴더", team.id(), creator.id());

            // then
            FolderRow folder = only(query.findEntryFoldersByTeam(team.id()));
            assertTeamFolder(folder, null, team.id(), creator.id());
            assertTeamAllowAllAcl(only(query.findAclsByFolder(folder.id())), folder.id(), team.id());
        }
    }

    @Nested
    @DisplayName("루트 폴더 생성")
    class CreateRootFolder {

        @Test
        @DisplayName("개인 엔트리 폴더 아래에 개인 루트 폴더와 기본 ACL을 생성한다")
        void createsPrivateRootFolderAndDefaultAcl() {
            // given
            TestUser user = fixture.createUser();
            TestFolder entryFolder = fixture.createPrivateEntryFolder(user.id());

            // when
            SimpleFolder response = folderService.createPrivateRootFolder(user.id(), "개인 루트 폴더");

            // then
            FolderRow rootFolder = only(query.findFoldersByParent(entryFolder.id(), FolderType.PRIVATE));
            assertThat(response.getUuid()).isEqualTo(rootFolder.uuid());
            assertPrivateFolder(rootFolder, entryFolder.id(), user.id());
            assertThat(rootFolder.idPath()).contains("/" + entryFolder.id() + "/", "/" + rootFolder.id() + "/");
            assertUserAllowAllAcl(only(query.findAclsByFolder(rootFolder.id())), rootFolder.id(), user.id());
        }

        @Test
        @DisplayName("팀 엔트리 폴더 아래에 팀 루트 폴더와 기본 ACL을 생성한다")
        void createsTeamRootFolderAndDefaultAcl() {
            // given
            TestUser creator = fixture.createUser();
            TestTeam team = fixture.createTeam(creator.id());
            TestFolder entryFolder = fixture.createTeamEntryFolder(team.id(), creator.id());
            TeamRootFolderCreateRequest request = new TeamRootFolderCreateRequest();
            request.setName("팀 루트 폴더");
            request.setTeamUuid(team.uuid());

            // when
            SimpleFolder response = folderService.createTeamRootFolder(creator.id(), request);

            // then
            FolderRow rootFolder = only(query.findFoldersByParent(entryFolder.id(), FolderType.TEAM));
            assertThat(response.getUuid()).isEqualTo(rootFolder.uuid());
            assertTeamFolder(rootFolder, entryFolder.id(), team.id(), creator.id());
            assertThat(rootFolder.idPath()).contains("/" + entryFolder.id() + "/", "/" + rootFolder.id() + "/");
            assertTeamAllowAllAcl(only(query.findAclsByFolder(rootFolder.id())), rootFolder.id(), team.id());
        }
    }

    @Nested
    @DisplayName("서브 폴더 생성")
    class CreateSubFolder {

        @Test
        @DisplayName("개인 서브 폴더는 부모 ACL을 그대로 상속한다")
        void createsPrivateSubFolderWithInheritedAcl() {
            // given
            TestUser user = fixture.createUser();
            TestFolder parent = fixture.createPrivateFolder(null, user.id());
            fixture.createUserAllowAcl(parent.id(), user.id(), PermissionBitMask.BROWSE, PermissionBitMask.READ);
            FolderCreateRequest request = subFolderRequest(parent, "개인 서브 폴더");

            // when
            SimpleFolder response = folderService.createSubFolder(user.id(), request);

            // then
            FolderRow child = only(query.findFoldersByParent(parent.id(), FolderType.PRIVATE));
            assertThat(response.getUuid()).isEqualTo(child.uuid());
            assertPrivateFolder(child, parent.id(), user.id());
            assertAclInherited(query.findAclsByFolder(parent.id()), query.findAclsByFolder(child.id()), child.id());
        }

        @Test
        @DisplayName("팀 서브 폴더는 부모 ACL을 그대로 상속한다")
        void createsTeamSubFolderWithInheritedAcl() {
            // given
            TestUser creator = fixture.createUser();
            TestTeam team = fixture.createTeam(creator.id());
            TestFolder parent = fixture.createTeamFolder(null, team.id(), creator.id());
            fixture.createTeamAllowAcl(parent.id(), team.id(), creator.id(), PermissionBitMask.BROWSE, PermissionBitMask.WRITE);
            FolderCreateRequest request = subFolderRequest(parent, "팀 서브 폴더");

            // when
            SimpleFolder response = folderService.createSubFolder(creator.id(), request);

            // then
            FolderRow child = only(query.findFoldersByParent(parent.id(), FolderType.TEAM));
            assertThat(response.getUuid()).isEqualTo(child.uuid());
            assertTeamFolder(child, parent.id(), team.id(), creator.id());
            assertAclInherited(query.findAclsByFolder(parent.id()), query.findAclsByFolder(child.id()), child.id());
        }
    }

    private static FolderCreateRequest subFolderRequest(TestFolder parent, String name) {
        FolderCreateRequest request = new FolderCreateRequest();
        request.setFolderName(name);
        request.setParentFolderUuid(parent.uuid());
        return request;
    }

    private static <T> T only(List<T> rows) {
        assertThat(rows).hasSize(1);
        return rows.getFirst();
    }

    private static void assertPrivateFolder(FolderRow folder, Long parentId, Integer userId) {
        assertThat(folder.type()).isEqualTo(FolderType.PRIVATE);
        assertThat(folder.parentId()).isEqualTo(parentId);
        assertThat(folder.scopeUserId()).isEqualTo(userId);
        assertThat(folder.scopeTeamId()).isNull();
        assertThat(folder.ownerId()).isEqualTo(userId);
    }

    private static void assertTeamFolder(FolderRow folder, Long parentId, Integer teamId, Integer ownerId) {
        assertThat(folder.type()).isEqualTo(FolderType.TEAM);
        assertThat(folder.parentId()).isEqualTo(parentId);
        assertThat(folder.scopeTeamId()).isEqualTo(teamId);
        assertThat(folder.scopeUserId()).isNull();
        assertThat(folder.ownerId()).isEqualTo(ownerId);
    }

    private static void assertUserAllowAllAcl(AclRow acl, Long folderId, Integer userId) {
        assertThat(acl.folderId()).isEqualTo(folderId);
        assertThat(acl.subjectType()).isEqualTo(AclSubjectType.USER);
        assertThat(acl.subjectUserId()).isEqualTo(userId);
        assertThat(acl.subjectTeamId()).isNull();
        assertThat(acl.effectType()).isEqualTo(AclEffectType.ALLOW);
        assertThat(acl.permissionBitMask()).isEqualTo(ALL_PERMISSIONS);
    }

    private static void assertTeamAllowAllAcl(AclRow acl, Long folderId, Integer teamId) {
        assertThat(acl.folderId()).isEqualTo(folderId);
        assertThat(acl.subjectType()).isEqualTo(AclSubjectType.TEAM);
        assertThat(acl.subjectTeamId()).isEqualTo(teamId);
        assertThat(acl.subjectUserId()).isNull();
        assertThat(acl.effectType()).isEqualTo(AclEffectType.ALLOW);
        assertThat(acl.permissionBitMask()).isEqualTo(ALL_PERMISSIONS);
    }

    private static void assertAclInherited(List<AclRow> parentAcls, List<AclRow> childAcls, Long childFolderId) {
        AclRow parentAcl = only(parentAcls);
        AclRow childAcl = only(childAcls);

        assertThat(childAcl.folderId()).isEqualTo(childFolderId);
        assertThat(childAcl)
                .usingRecursiveComparison()
                .ignoringFields("id", "folderId")
                .isEqualTo(parentAcl);
    }
}
