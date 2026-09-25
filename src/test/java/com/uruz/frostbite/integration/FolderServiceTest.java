package com.uruz.frostbite.integration;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.common.response.CursorBaseListResponse;
import com.uruz.frostbite.dto.common.response.OffsetBaseListResponse;
import com.uruz.frostbite.dto.entry.FolderContents;
import com.uruz.frostbite.dto.folder.FolderCreateRequest;
import com.uruz.frostbite.dto.folder.SimpleFolder;
import com.uruz.frostbite.dto.folder.SimpleFolderTreeNode;
import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.enums.*;
import com.uruz.frostbite.service.DirectFileHandleComponent;
import com.uruz.frostbite.service.FolderService;
import com.uruz.frostbite.service.UserService;
import com.uruz.frostbite.service.resolver.UserResolverService;
import com.uruz.frostbite.support.IntegrationTestSupport;
import com.uruz.frostbite.support.TestDataFixture.TestFolder;
import com.uruz.frostbite.support.TestDataFixture.TestTeam;
import com.uruz.frostbite.support.TestDataFixture.TestUser;
import com.uruz.frostbite.support.TestDataQuerySupport.AclRow;
import com.uruz.frostbite.support.TestDataQuerySupport.FolderRow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FolderService 통합 테스트")
@ActiveProfiles("LOCAL-DEV-WIN")
@SpringBootTest(properties = {
        "spring.config.import=file:./application-secret.properties"
})
// 물리파일 안건드리는데 디렉트컴포넌트 거는거도 좀.. -> @MockitoBean 으로 처리하자
//@SpringBootTest(properties = {"spring.config.import=file:./application-secret.properties"})
//@TestPropertySource(locations = "classpath:application-test.properties")
class FolderServiceTest extends IntegrationTestSupport {

    private static final int ALL_PERMISSIONS = PermissionBitMask.of(PermissionBitMask.allMasks());

    @Autowired
    private FolderService folderService;

    @MockitoBean
    DirectFileHandleComponent directFileHandleComponent;

    private static final String USER_EMAIL = "junhoon.lee@konantech.com";



    // JDBC Template 및 QA 작성 전반에 익숙해지기 위해 JdbcTemplate 수동으로 작성한다.
    @DisplayName("사용자 userId와 folderName을 제출하면 엔트리폴더가 자동으로 찾아져서 엔트리 폴더 아래에 사용자 폴더가 생성되어야 한다.")
    @Commit
    @Test
    public void userIdAndFolderNameIsDoForRootFolderCreationUnderOwnEntryFolder(){

        // 사용자 정보 조회 (UserService 메서드 장애에 휘둘리지 않게, 사용자 정보를 직접 조회해온다.

        Integer userId = jdbcTemplate.queryForObject("""
                SELECT user_id FROM user_tb WHERE email = ?
                """,  Integer.class, USER_EMAIL
        );

        String folderName = "ROOT_FOLDER_" + UUID.randomUUID();
        folderService.createPrivateRootFolder(userId, folderName);

        Folder createdFolder = jdbcTemplate.queryForObject("""
                SELECT
                    uuid, folder_id, folder_name, folder_type, resource_scope_user_id, parent_folder_id, create_user_id
                FROM
                    folder_tb
                WHERE
                    folder_name = ?
                    
                
                """,
                // 단순 타입은 *.class
                // 객체 매핑은 RowMapper 사용
                // MyBatis TypeHandler는 JdbcTemplate에서 적용되지 않으므로 Enum은 직접 변환
                folderTestRowMapper, folderName);

        assertThat(createdFolder).isNotNull();

        Folder parentEntryFolder = jdbcTemplate.queryForObject("""
                    SELECT
                        uuid, folder_id, folder_name, folder_type, resource_scope_user_id, parent_folder_id, create_user_id
                    FROM
                        folder_tb
                    WHERE
                        folder_id = ?
                """,  folderTestRowMapper, createdFolder.getParentFolderId()
        );


        assertThat(parentEntryFolder).isNotNull();

        Assertions.assertAll(
                () -> assertThat(createdFolder.getCreateUserId()).isEqualTo(userId),
                () -> assertThat(createdFolder.getResourceScopeUserId()).isEqualTo(userId),
                () -> assertThat(createdFolder.getFolderName()).isEqualTo(folderName),
                () -> assertThat(createdFolder.getFolderType()).isEqualTo(FolderType.PRIVATE),
                () -> assertThat(parentEntryFolder.getParentFolderId()).isNull(),
                () -> assertThat(parentEntryFolder.getResourceScopeUserId()).isEqualTo(userId),
                () -> assertThat(parentEntryFolder.getFolderType()).isEqualTo(FolderType.PRIVATE)
        );
    }

    @DisplayName("엔트리폴더를 parentFolderUuid로 제출하면 ROOT 계층의 모든 폴더가 출력되어야 한다")
    @Test
    public void parameteredEntryFolderUuidShouldBeOnlyOneParameterForSelectRootDepthContents(){
        Integer userId = jdbcTemplate.queryForObject("""
                SELECT user_id FROM user_tb WHERE email = ?
                """,  Integer.class, USER_EMAIL
        );

        String folderName = "ROOT_FOLDER_" + UUID.randomUUID();
        folderService.createPrivateRootFolder(userId, folderName);

        Folder createdFolder = jdbcTemplate.queryForObject("""
                SELECT
                    uuid, folder_id, folder_name, folder_type, resource_scope_user_id, parent_folder_id, create_user_id
                FROM
                    folder_tb
                WHERE
                    folder_name = ?
                    
                
                """,
                // 단순 타입은 *.class
                // 객체 매핑은 RowMapper 사용
                // MyBatis TypeHandler는 JdbcTemplate에서 적용되지 않으므로 Enum은 직접 변환
                folderTestRowMapper, folderName);


        Folder parentEntryFolder = jdbcTemplate.queryForObject("""
                    SELECT
                        uuid, folder_id, folder_name, folder_type, resource_scope_user_id, parent_folder_id, create_user_id
                    FROM
                        folder_tb
                    WHERE
                        resource_scope_user_id = ?
                    AND
                        parent_folder_id is null
                """,  folderTestRowMapper, userId
        );

        assertThat(createdFolder).isNotNull();
        assertThat(parentEntryFolder).isNotNull();


        OffsetBaseListResponse<FolderContents> rootDepthContentsResponse = folderService.getPrivateFolderContentsByOffsetBase(userId, parentEntryFolder.getUuid(), 0, 999);

        assertThat(rootDepthContentsResponse.getContents())
                .anySatisfy(content -> {
                    assertThat(content.getUuid())
                            .isEqualTo(createdFolder.getUuid());
                    assertThat(content.getEntryName())
                            .isEqualTo(createdFolder.getFolderName());
                }
            );
    }

    // RowMapper를 별도로 만들어두고 재활용 할 수 있다.
    private final RowMapper<Folder> folderTestRowMapper = (rs, rowNum) -> {
        Folder folder = new Folder();
        folder.setUuid((UUID)rs.getObject("uuid"));
        folder.setFolderId(rs.getLong("folder_id"));
        folder.setFolderName(rs.getString("folder_name"));

        // JdbcTemplate은 MyBatis TypeHandler를 타지 않으므로 직접 변환
        folder.setFolderType(
                FolderType.fromValue(rs.getInt("folder_type"))
        );

        folder.setResourceScopeUserId(
                rs.getObject("resource_scope_user_id", Integer.class)
        );
        folder.setParentFolderId(
                rs.getObject("parent_folder_id", Long.class)
        );
        folder.setCreateUserId(
                rs.getObject("create_user_id", Integer.class)
        );

        return folder;
    };

    @Test
    @DisplayName("nextFolderUUID 가 주어지지 않으면 커서베이스는 0번지 폴더부터 LIMIT 개수만큼 적절한 '다음폴더UUID'를 가지고 응답되어야 한다")
    public void nullNextFolderUUIDParameterShouldAnsweredWithProperNextFolderUuidAsLimitedCounts(){
        Integer userId = jdbcTemplate.queryForObject("""
                SELECT user_id FROM user_tb WHERE email = ?
                """,  Integer.class, USER_EMAIL
        );

        Folder parentEntryFolder = jdbcTemplate.queryForObject("""
                    SELECT
                        uuid, folder_id, folder_name, folder_type, resource_scope_user_id, parent_folder_id, create_user_id
                    FROM
                        folder_tb
                    WHERE
                        resource_scope_user_id = ?
                    AND
                        parent_folder_id is null
                """,  folderTestRowMapper, userId
        );

        Folder firstFolder = jdbcTemplate.queryForObject("""
                    SELECT
                        uuid, folder_id, folder_name, folder_type, resource_scope_user_id, parent_folder_id, create_user_id
                    FROM
                        folder_tb
                    WHERE
                        resource_scope_user_id = ?
                    AND
                        parent_folder_id  = ?
                    ORDER BY COALESCE(render_sequence, 2147483647) ASC, folder_id ASC
                    OFFSET 0 LIMIT 1
                """,  folderTestRowMapper, userId, parentEntryFolder.getFolderId()
        );


        Folder nextFolder = jdbcTemplate.query("""
                    SELECT
                        uuid, folder_id, folder_name, folder_type,
                        resource_scope_user_id, parent_folder_id, create_user_id
                    FROM folder_tb
                    WHERE resource_scope_user_id = ?
                      AND parent_folder_id = ?
                    ORDER BY COALESCE(render_sequence, 2147483647) ASC, folder_id ASC
                    OFFSET 1 LIMIT 1
                    """,
                folderTestRowMapper,
                userId,
                parentEntryFolder.getFolderId()
        ).stream().findFirst().orElse(null);


        CursorBaseListResponse<SimpleFolderTreeNode> response = folderService.getPrivateFoldersByCursorBase(userId, parentEntryFolder.getUuid(), null, 1);

        Assertions.assertAll(
                () -> assertThat(response.getContents()).as("한개만 내려와야 합니다.").hasSize(1),

                () -> assertThat(response.getContents().getFirst().getUuid()).as("응답 첫 폴더 UUID와 query로 조회한 첫 폴더UUID가 다릅니다.")
                        .isEqualTo(firstFolder.getUuid()),

                () -> assertThat(response.getPagination())
                        .satisfies(pagination -> {
                            if (pagination.getHasNext()) {
                                assertThat(nextFolder).isNotNull();
                                assertThat(pagination.getNextUuid())
                                        .isEqualTo(nextFolder.getUuid());
                            } else {
                                assertThat(pagination.getNextUuid()).isNull();
                                assertThat(nextFolder).isNull();
                            }
                        })
        );
    }

    @Test
    @DisplayName("nextFolderUUID 가 주어지면 커서베이스는 해당 폴더부터 LIMIT 개수만큼 적절한 '다음폴더UUID'를 가지고 응답되어야 한다")
    public void validNextFolderUUIDParameterShouldAnsweredWithProperNextFolderUuidAsLimitedCounts(){
        Integer userId = jdbcTemplate.queryForObject("""
                SELECT user_id FROM user_tb WHERE email = ?
                """,  Integer.class, USER_EMAIL
        );

        Folder parentEntryFolder = jdbcTemplate.queryForObject("""
                    SELECT
                        uuid, folder_id, folder_name, folder_type, resource_scope_user_id, parent_folder_id, create_user_id
                    FROM
                        folder_tb
                    WHERE
                        resource_scope_user_id = ?
                    AND
                        parent_folder_id is null
                """,  folderTestRowMapper, userId
        );

        Folder startFolder = jdbcTemplate.queryForObject("""
                    SELECT
                        uuid, folder_id, folder_name, folder_type, resource_scope_user_id, parent_folder_id, create_user_id
                    FROM
                        folder_tb
                    WHERE
                        resource_scope_user_id = ?
                    AND
                        parent_folder_id  = ?
                    ORDER BY COALESCE(render_sequence, 2147483647) ASC, folder_id ASC
                    OFFSET 1 LIMIT 1
                """,  folderTestRowMapper, userId, parentEntryFolder.getFolderId()
        );


        Folder nextFolder = jdbcTemplate.query("""
                    SELECT
                        uuid, folder_id, folder_name, folder_type,
                        resource_scope_user_id, parent_folder_id, create_user_id
                    FROM folder_tb
                    WHERE resource_scope_user_id = ?
                      AND parent_folder_id = ?
                    ORDER BY COALESCE(render_sequence, 2147483647) ASC, folder_id ASC
                    OFFSET 2 LIMIT 1
                    """,
                folderTestRowMapper,
                userId,
                parentEntryFolder.getFolderId()
        ).stream().findFirst().orElse(null);

        CursorBaseListResponse<SimpleFolderTreeNode> response = folderService.getPrivateFoldersByCursorBase(userId, parentEntryFolder.getUuid(), startFolder.getUuid(), 1);

        Assertions.assertAll(
                () -> assertThat(response.getContents()).as("한개만 내려와야 합니다.").hasSize(1),

                () -> assertThat(response.getContents().getFirst().getUuid()).as("응답 첫 폴더 UUID와 query로 조회한 첫 폴더UUID가 다릅니다.")
                        .isEqualTo(startFolder.getUuid()),

                () -> assertThat(response.getPagination())
                        .satisfies(pagination -> {
                            if (pagination.getHasNext()) {
                                assertThat(nextFolder).isNotNull();
                                assertThat(pagination.getNextUuid())
                                        .isEqualTo(nextFolder.getUuid());
                            } else {
                                assertThat(pagination.getNextUuid()).isNull();
                                assertThat(nextFolder).isNull();
                            }
                        })
        );
    }

    /*
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

         */
}
