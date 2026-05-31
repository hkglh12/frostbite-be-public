package com.uruz.frostbite.integration;

import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.dto.folder.FolderCreateRequest;
import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.enums.*;
import com.uruz.frostbite.enums.team.TeamStatus;
import com.uruz.frostbite.enums.user.AccountType;
import com.uruz.frostbite.service.FolderService;
import com.uruz.frostbite.service.resolver.TeamResolverService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FolderService 서비스 레이어 통합 테스트

 * [트랜잭션 전략]
 * @Transactional 선언으로 각 테스트 메서드 종료 후 자동 롤백.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("FolderService 단위 테스트")
class FolderServiceTest {

    @Autowired
    private FolderService folderService;
    @Autowired
    private TeamResolverService teamResolverService;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional
    @Rollback
    @Test
    @DisplayName("개인 Entry 폴더 생성 테스트, default ACL이 함꼐 생성되어야 한다.")
    public void createPrivateEntryFolderShouldCreateUserAllowACL(){
        // GIVEN -> 사용자 생성 -> EntryFolder 생성
        // 사용자 생성 비즈니스 로직이 이미 폴더생성과 엮여있음 -> 폴더 단위테스트에 부적합. 따라서 JdbcTemplate로 직접 insert

        Integer userId = givenCreateUser();
        Map<String, Object> folder = givenPrivateEntryFolder(userId);

        Long folderId = ((Number) folder.get("folder_id")).longValue();

        assertThat(folderId)
                .as("Folder ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number) folder.get("resource_scope_user_id") ).intValue() )
                .as("개인폴더 scopeUserId는 사용자PK와 같아야 한다.").isEqualTo(userId);
        assertThat(( (Number) folder.get("resource_owner_id") ).intValue() )
                .as("개인폴더 resource_owner_id는 사용자PK와 같아야 한다.").isEqualTo(userId);
        assertThat(folder.get("resource_scope_team_id"))
                .as("개인 폴더의 resourceScopeTeamId는 NULL이어야 한다.").isNull();

        List<Map<String, Object>> folderAcls = jdbcTemplate.queryForList(
                """
                SELECT acl_id, permission_bit_mask, folder_id, effect_type, subject_type, subject_user_id, subject_team_id
                FROM folder_acl_tb
                WHERE folder_id = ?
                  AND subject_user_id = ?
                  AND effect_type = ?
                """,
                folderId,
                userId,
                AclEffectType.ALLOW.getValue()
        );

        assertThat(folderAcls)
                .as("개인 엔트리 폴더 ACL은 정확히 1개 생성되어야 한다.")
                .hasSize(1);

        Map<String, Object> folderAcl = folderAcls.getFirst();

        assertThat(folderAcl.get("acl_id"))
                .as("ACL ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number)folderAcl.get("permission_bit_mask")).intValue() )
                .as("개인 엔트리 폴더 PermissinoBitMask는 * 권한이어야 한다.").isEqualTo(PermissionBitMask.of(PermissionBitMask.allMasks()));
        assertThat(( (Number)folderAcl.get("subject_type")).intValue() )
                .as("개인 엔트리 폴더 ACL은 subject_type USER로 지정되어야 한다.").isEqualTo(AclSubjectType.USER.getValue());
        assertThat(folderAcl.get("subject_team_id"))
                .as("개인 엔트리 폴더 ACL은 subject_team_id NULL이어야 한다.").isNull();
    }

    @Transactional
    @Rollback
    @Test
    @DisplayName("개인 ROOT 폴더 생성 테스트, default ACL이 함꼐 생성되어야 한다.")
    public void createPrivateRootFolderShouldCreateDefaultAcl(){
        Integer userId = givenCreateUser();
        Map<String, Object> parentFolder = givenPrivateEntryFolder(userId);

        Long parentFolderId = ((Number) parentFolder.get("folder_id")).longValue();

        folderService.createPrivateRootFolder(userId, "단위테스트-이준훈Private-ROOT");

        List<Map<String, Object>> rootFolders = jdbcTemplate.queryForList(
                """
                    SELECT folder_id, parent_folder_id, resource_scope_team_id, resource_scope_user_id,
                    resource_owner_id, folder_id_path, folder_type
                    FROM folder_tb
                    WHERE parent_folder_id = ?
                    AND folder_type = ?
                    AND resource_scope_user_id = ?
                        """,
                parentFolderId, FolderType.PRIVATE.getValue(), userId
        );

        assertThat(rootFolders).hasSize(1);
        Map<String, Object> rootFolder = rootFolders.getFirst();
        Long folderId = ((Number) rootFolder.get("folder_id")).longValue();

        assertThat(folderId)
                .as("Folder ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number) rootFolder.get("resource_scope_user_id") ).intValue() )
                .as("개인폴더 scopeUserId는 사용자PK와 같아야 한다.").isEqualTo(userId);
        assertThat(( (Number) rootFolder.get("resource_owner_id") ).intValue() )
                .as("개인폴더 resource_owner_id는 사용자PK와 같아야 한다.").isEqualTo(userId);
        assertThat(rootFolder.get("resource_scope_team_id"))
                .as("개인 폴더의 resourceScopeTeamId는 NULL이어야 한다.").isNull();
        assertThat((String)rootFolder.get("folder_id_path"))
                .as("폴더 경로는 자동 생성되어야 한다")
                .isNotNull()
                .contains("/"+parentFolderId+"/")
                .contains("/"+folderId+"/");

        List<Map<String, Object>> rootFolderAcls = jdbcTemplate.queryForList(
                """
                SELECT acl_id, permission_bit_mask, folder_id, effect_type, subject_type, subject_user_id, subject_team_id
                FROM folder_acl_tb
                WHERE folder_id = ?
                  AND subject_user_id = ?
                  AND effect_type = ?
                """,
                folderId,
                userId,
                AclEffectType.ALLOW.getValue()
        );

        assertThat(rootFolderAcls)
                .as("개인 ROOT 폴더 ACL은 정확히 1개 생성되어야 한다.")
                .hasSize(1);

        Map<String, Object> rootFolderAcl = rootFolderAcls.getFirst();

        // where절에서 검증되긴 했지만.. 연습이니까 그냥 써본다
        assertThat(((Number)rootFolderAcl.get("folder_id")).longValue())
                .as("ACL folder_id는 자동생성 시 인자로 넘긴 folder_id를 가져야 한다.").isEqualTo(folderId);
        assertThat(rootFolderAcl.get("acl_id"))
                .as("ACL ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number)rootFolderAcl.get("permission_bit_mask")).intValue() )
                .as("개인 폴더 PermissinoBitMask는 * 권한이어야 한다.").isEqualTo(PermissionBitMask.of(PermissionBitMask.allMasks()));
        assertThat(( (Number)rootFolderAcl.get("subject_type")).intValue() )
                .as("개인 폴더 ACL은 subject_type USER로 지정되어야 한다.").isEqualTo(AclSubjectType.USER.getValue());
        assertThat(rootFolderAcl.get("subject_team_id"))
                .as("개인 폴더 ACL은 subject_team_id NULL이어야 한다.").isNull();

    }

    @Transactional
    @Rollback
    @Test
    @DisplayName("개인 SUB 폴더 생성 테스트, default ACL이 함꼐 생성되어야 한다.")
    public void createPrivateSubFolderShouldCreatInheritedAcl(){
        Integer userId = givenCreateUser();
        Map<String, Object> entryFolder = givenPrivateEntryFolder(userId);

        Long entryFolderId = ((Number) entryFolder.get("folder_id")).longValue();

        folderService.createPrivateRootFolder(userId, "단위테스트-이준훈Private-ROOT");

        List<Map<String, Object>> rootFolders = jdbcTemplate.queryForList(
                """
                    /** ENTRY FOLDER SELECT*/
                    SELECT folder_id, parent_folder_id, resource_scope_team_id, resource_scope_user_id,
                    resource_owner_id, folder_id_path, folder_type, uuid
                    FROM folder_tb
                    WHERE parent_folder_id = ?
                    AND folder_type = ?
                    AND resource_scope_user_id = ?
                        """,
                entryFolderId, FolderType.PRIVATE.getValue(), userId
        );

        assertThat(rootFolders).hasSize(1);
        Map<String, Object> rootFolder = rootFolders.getFirst();
        Long rootFolderId = ((Number) rootFolder.get("folder_id")).longValue();

        List<Map<String, Object>> rootFolderAcls = jdbcTemplate.queryForList(
                """
                /** ROOT FOLDER ACL SELECT*/
                SELECT acl_id, permission_bit_mask, folder_id, effect_type, subject_type, subject_user_id, subject_team_id
                FROM folder_acl_tb
                WHERE folder_id = ?
                  AND subject_user_id = ?
                  AND effect_type = ?
                """,
                rootFolderId,
                userId,
                AclEffectType.ALLOW.getValue()
        );

        Map<String, Object> rootFolderAcl = rootFolderAcls.getFirst();
        FolderCreateRequest subFolderCreateRequest = new FolderCreateRequest();
        subFolderCreateRequest.setName("TEST PRIVATE SUBFOLDER");
        subFolderCreateRequest.setParentFolderUuid(UUID.fromString(rootFolder.get("uuid").toString()));
        folderService.createSubFolder(userId, subFolderCreateRequest);

        List<Map<String, Object>> subFolders = jdbcTemplate.queryForList(
                """
                    /** SUB Folder SELECT */
                    SELECT folder_id, parent_folder_id, resource_scope_team_id, resource_scope_user_id,
                    resource_owner_id, folder_id_path, folder_type, uuid
                    FROM folder_tb
                    WHERE parent_folder_id = ?
                    AND folder_type = ?
                    AND resource_scope_user_id = ?
                        """,
                rootFolderId, FolderType.PRIVATE.getValue(), userId
        );

        Map<String, Object> subFolder = subFolders.getFirst();
        Long subFolderId = ((Number) subFolder.get("folder_id")).longValue();

        assertThat(subFolderId)
                .as("Folder ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number) subFolder.get("resource_scope_user_id") ).intValue() )
                .as("개인폴더 scopeUserId는 사용자PK와 같아야 한다.").isEqualTo(userId);
        assertThat(( (Number) subFolder.get("resource_owner_id") ).intValue() )
                .as("개인폴더 resource_owner_id는 사용자PK와 같아야 한다.").isEqualTo(userId);
        assertThat(subFolder.get("resource_scope_team_id"))
                .as("개인 폴더의 resourceScopeTeamId는 NULL이어야 한다.").isNull();
        assertThat((String)subFolder.get("folder_id_path"))
                .as("폴더 경로는 자동 생성되어야 한다")
                .isNotNull()
                .contains("/"+rootFolderId+"/")
                .contains("/"+subFolderId+"/");
        assertThat(( (Number) subFolder.get("parent_folder_id")).longValue() )
                .as("sub 폴더 ID의 부모폴더 아이디는 제출한 아이디와 같아야한다")
                .isEqualTo(rootFolderId);

        List<Map<String, Object>> subFolderAcls = jdbcTemplate.queryForList(
                """
                SELECT acl_id, permission_bit_mask, folder_id, effect_type, subject_type, subject_user_id, subject_team_id
                FROM folder_acl_tb
                WHERE folder_id = ?
                  AND subject_user_id = ?
                """,
                subFolderId,
                userId
        );

        Map<String, Object> subFolderAcl = subFolderAcls.getFirst();

        assertThat(subFolderAcl)
                .containsEntry("permission_bit_mask", rootFolderAcl.get("permission_bit_mask"))
                .as("비트마스크는 부모 ACL을 승계해야 한다.")
                .containsEntry("effect_type", rootFolderAcl.get("effect_type"))
                .as("ACL 종류는 동일해야 한다.")
                .containsEntry("subject_type", rootFolderAcl.get("subject_type"))
                .as("ACL 적용대상 종류는 동일해야 한다.")
                .containsEntry("subject_user_id", rootFolderAcl.get("subject_user_id"))
                .as("ACL 적용 대상 USER는 동일해야 한다.")
                .containsEntry("subject_team_id", rootFolderAcl.get("subject_team_id"))
                .as("ACL 적용 대상 TEAM은 동일해야 한다.");

    }







    /** TEAM */

    @Transactional
    @Rollback
    @Test
    @DisplayName("팀 Entry 폴더 생성 테스트, default ACL이 함꼐 생성되어야 한다.")
    public void createEntryTeamFolderShouldCreateTeamAllAllowACL(){
        // GIVEN -> 사용자 생성 -> EntryFolder 생성
        // 사용자 생성 비즈니스 로직이 이미 폴더생성과 엮여있음 -> 폴더 단위테스트에 부적합. 따라서 JdbcTemplate로 직접 insert

        Integer userId = givenCreateUser();
        Integer teamId = givenCreateTeam(userId);
        Map<String, Object> folder = givenTeamEntryFolder(teamId, userId);

        Long folderId = ((Number) folder.get("folder_id")).longValue();

        assertThat(folderId)
                .as("Folder ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number) folder.get("resource_scope_team_id") ).intValue() )
                .as("팀 폴더 scopeTeamId는 team PK와 같아야 한다.").isEqualTo(teamId);
        assertThat(( (Number) folder.get("resource_owner_id") ).intValue() )
                .as("팀 entry 폴더 resource_owner_id는 최초에는 생성자 PK와 같아야 한다.").isEqualTo(userId);
        assertThat(folder.get("resource_scope_user_id"))
                .as("팀 폴더의 resourceScopeUserId는 NULL이어야 한다.").isNull();

        List<Map<String, Object>> folderAcls = jdbcTemplate.queryForList(
                """
                SELECT acl_id, permission_bit_mask, folder_id, effect_type, subject_type, subject_user_id, subject_team_id
                FROM folder_acl_tb
                WHERE folder_id = ?
                  AND subject_team_id = ?
                  AND effect_type = ?
                """,
                folderId,
                teamId,
                AclEffectType.ALLOW.getValue()
        );

        assertThat(folderAcls)
                .as("팀 엔트리 폴더 ACL은 정확히 1개 생성되어야 한다.")
                .hasSize(1);

        Map<String, Object> folderAcl = folderAcls.getFirst();

        assertThat(folderAcl.get("acl_id"))
                .as("ACL ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number)folderAcl.get("permission_bit_mask")).intValue() )
                .as("팀 엔트리 폴더 PermissinoBitMask는 * 권한이어야 한다.").isEqualTo(PermissionBitMask.of(PermissionBitMask.allMasks()));
        assertThat(( (Number)folderAcl.get("subject_type")).intValue() )
                .as("팀 엔트리 폴더 ACL은 subject_type TEAM으로 지정되어야 한다.").isEqualTo(AclSubjectType.TEAM.getValue());
        assertThat(folderAcl.get("subject_user_id"))
                .as("팀 엔트리 폴더 ACL은 subject_user_id가 NULL이어야 한다.").isNull();
    }

    @Transactional
    @Rollback
    @Test
    @DisplayName("팀 ROOT 폴더 생성 테스트, default ACL이 함꼐 생성되어야 한다.")
    public void createTeamRootFolderShouldCreateDefaultAcl(){
        Integer userId = givenCreateUser();
        Integer teamId = givenCreateTeam(userId);
        Map<String, Object> parentFolder = givenTeamEntryFolder(teamId, userId);

        Long parentFolderId = ((Number) parentFolder.get("folder_id")).longValue();

        TeamRootFolderCreateRequest request = new TeamRootFolderCreateRequest();
        request.setName("팀 ROOT TEST");
        Team team = teamResolverService.getTeambyTeamId(teamId);
        request.setTeamUuid(team.getUuid());
        folderService.createTeamRootFolder(userId, request);

        List<Map<String, Object>> rootFolders = jdbcTemplate.queryForList(
                """
                    /** TEAM ROOT SELECT IN TEST */
                    SELECT folder_id, parent_folder_id, resource_scope_team_id, resource_scope_user_id,
                    resource_owner_id, folder_id_path, folder_type
                    FROM folder_tb
                    WHERE parent_folder_id = ?
                    AND folder_type = ?
                    AND resource_scope_team_id = ?
                        """,
                parentFolderId, FolderType.TEAM.getValue(), teamId
        );

        assertThat(rootFolders).hasSize(1);
        Map<String, Object> rootFolder = rootFolders.getFirst();
        Long folderId = ((Number) rootFolder.get("folder_id")).longValue();

        assertThat(folderId)
                .as("Folder ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number) rootFolder.get("resource_scope_team_id") ).intValue() )
                .as("팀 root폴더 scopeTeamId는 팀PK와 같아야 한다.").isEqualTo(teamId);
        assertThat(( (Number) rootFolder.get("resource_owner_id") ).intValue() )
                .as("팀 Root폴더 resource_owner_id는 생성자 PK와  같아야 한다.").isEqualTo(userId);
        assertThat(rootFolder.get("resource_scope_user_id"))
                .as("팀 폴더의 resource_scope_user_id는 NULL이어야 한다.").isNull();
        assertThat((String)rootFolder.get("folder_id_path"))
                .as("폴더 경로는 자동 생성되어야 한다")
                .isNotNull()
                .contains("/"+parentFolderId+"/")
                .contains("/"+folderId+"/");

        List<Map<String, Object>> rootFolderAcls = jdbcTemplate.queryForList(
                """
                /** TEAM ROOT FOLDER ACL SELECT IN TEST */
                SELECT acl_id, permission_bit_mask, folder_id, effect_type, subject_type, subject_user_id, subject_team_id
                FROM folder_acl_tb
                WHERE folder_id = ?
                  AND subject_team_id = ?
                  AND effect_type = ?
                """,
                folderId,
                team.getTeamId(),
                AclEffectType.ALLOW.getValue()
        );

        assertThat(rootFolderAcls)
                .as("팀 ROOT 폴더 default ACL은 정확히 1개 생성되어야 한다.")
                .hasSize(1);

        Map<String, Object> rootFolderAcl = rootFolderAcls.getFirst();

        // where절에서 검증되긴 했지만.. 연습이니까 그냥 써본다
        assertThat(((Number)rootFolderAcl.get("folder_id")).longValue())
                .as("ACL folder_id는 자동생성 시 인자로 넘긴 folder_id를 가져야 한다.").isEqualTo(folderId);
        assertThat(rootFolderAcl.get("acl_id"))
                .as("ACL ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number)rootFolderAcl.get("permission_bit_mask")).intValue() )
                .as("팀 root폴더 default acl PermissinoBitMask는 * 권한이어야 한다.").isEqualTo(PermissionBitMask.of(PermissionBitMask.allMasks()));
        assertThat(( (Number)rootFolderAcl.get("subject_type")).intValue() )
                .as("팀 root폴더 ACL은 subject_type team로 지정되어야 한다.").isEqualTo(AclSubjectType.TEAM.getValue());
        assertThat(rootFolderAcl.get("subject_user_id"))
                .as("팀 ROOT폴더 ACL은 subject_user_id NULL이어야 한다.").isNull();

    }

    @Transactional
    @Rollback
    @Test
    @DisplayName("팀 Sub 폴더 생성 테스트, default ACL이 함꼐 생성되어야 한다.")
    public void createTeamSubFolderShouldCreateDefaultAcl(){
        Integer userId = givenCreateUser();
        Integer teamId = givenCreateTeam(userId);
        Map<String, Object> entryFolder = givenTeamEntryFolder(teamId, userId);

        Long entryFolderId = ((Number) entryFolder.get("folder_id")).longValue();

        TeamRootFolderCreateRequest request = new TeamRootFolderCreateRequest();
        request.setName("팀 ROOT TEST");
        Team team = teamResolverService.getTeambyTeamId(teamId);
        request.setTeamUuid(team.getUuid());
        folderService.createTeamRootFolder(userId, request);

        List<Map<String, Object>> rootFolders = jdbcTemplate.queryForList(
                """
                    /** TEAM ROOT SELECT IN TEST */
                    SELECT folder_id, parent_folder_id, resource_scope_team_id, resource_scope_user_id,
                    resource_owner_id, folder_id_path, folder_type, uuid
                    FROM folder_tb
                    WHERE parent_folder_id = ?
                    AND folder_type = ?
                    AND resource_scope_team_id = ?
                        """,
                entryFolderId, FolderType.TEAM.getValue(), teamId
        );

        assertThat(rootFolders).hasSize(1);
        Map<String, Object> rootFolder = rootFolders.getFirst();
        Long rootFolderId = ((Number) rootFolder.get("folder_id")).longValue();

        List<Map<String, Object>> rootFolderAcls = jdbcTemplate.queryForList(
                """
                /** TEAM ROOT FOLDER ACL SELECT IN TEST */
                SELECT acl_id, permission_bit_mask, folder_id, effect_type, subject_type, subject_user_id, subject_team_id
                FROM folder_acl_tb
                WHERE folder_id = ?
                  AND subject_team_id = ?
                  AND effect_type = ?
                """,
                rootFolderId,
                team.getTeamId(),
                AclEffectType.ALLOW.getValue()
        );

        Map<String, Object> rootFolderAcl = rootFolderAcls.getFirst();

        FolderCreateRequest subFolderCreateRequest = new FolderCreateRequest();
        subFolderCreateRequest.setName("TEST TEAM SUBFOLDER");
        subFolderCreateRequest.setParentFolderUuid(UUID.fromString(rootFolder.get("uuid").toString()));
        folderService.createSubFolder(userId, subFolderCreateRequest);

        List<Map<String, Object>> subFolders = jdbcTemplate.queryForList(
                """
                    /** SUB Folder SELECT */
                    SELECT folder_id, parent_folder_id, resource_scope_team_id, resource_scope_user_id,
                    resource_owner_id, folder_id_path, folder_type, uuid
                    FROM folder_tb
                    WHERE parent_folder_id = ?
                    AND folder_type = ?
                    AND resource_scope_team_id = ?
                        """,
                rootFolderId, FolderType.TEAM.getValue(), teamId
        );

        Map<String, Object> subFolder = subFolders.getFirst();
        Long subFolderId = ((Number) subFolder.get("folder_id")).longValue();

        assertThat(subFolderId)
                .as("Folder ID는 자동생성되어야 한다.").isNotNull();
        assertThat(( (Number) subFolder.get("resource_scope_team_id") ).intValue() )
                .as("팀 폴더의 scope_team_id는 소속 팀 PK와 같아야 한다.").isEqualTo(teamId);
        assertThat(( (Number) subFolder.get("resource_owner_id") ).intValue() )
                .as("팀 폴더의 resource_owner_id는 생성자 PK와 같아야 한다.").isEqualTo(userId);
        assertThat(subFolder.get("resource_scope_user_id"))
                .as("팀 폴더의 scope_user_id 는  NULL이어야 한다.").isNull();
        assertThat((String)subFolder.get("folder_id_path"))
                .as("폴더 경로는 자동 생성되어야 한다")
                .isNotNull()
                .contains("/"+rootFolderId+"/")
                .contains("/"+subFolderId+"/");
        assertThat(( (Number) subFolder.get("parent_folder_id")).longValue() )
                .as("sub 폴더 ID의 부모폴더 아이디는 제출한 아이디와 같아야한다")
                .isEqualTo(rootFolderId);

        List<Map<String, Object>> subFolderAcls = jdbcTemplate.queryForList(
                """
                SELECT acl_id, permission_bit_mask, folder_id, effect_type, subject_type, subject_user_id, subject_team_id
                FROM folder_acl_tb
                WHERE folder_id = ?
                  AND subject_team_id = ?
                """,
                subFolderId,
                teamId
        );

        Map<String, Object> subFolderAcl = subFolderAcls.getFirst();

        assertThat(subFolderAcl)
                .containsEntry("permission_bit_mask", rootFolderAcl.get("permission_bit_mask"))
                .as("비트마스크는 부모 ACL을 승계해야 한다.")
                .containsEntry("effect_type", rootFolderAcl.get("effect_type"))
                .as("ACL 종류는 동일해야 한다.")
                .containsEntry("subject_type", rootFolderAcl.get("subject_type"))
                .as("ACL 적용대상 종류는 동일해야 한다.")
                .containsEntry("subject_user_id", rootFolderAcl.get("subject_user_id"))
                .as("ACL 적용 대상 USER는 동일해야 한다.")
                .containsEntry("subject_team_id", rootFolderAcl.get("subject_team_id"))
                .as("ACL 적용 대상 TEAM은 동일해야 한다.");


    }

    /** Fixtures */

    private Integer givenCreateUser(){
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "entry-folder-" + suffix + "@test.com";
        String phone = "010" + String.valueOf(System.nanoTime()).substring(0, 8);

        Integer userId = jdbcTemplate.queryForObject(
                """
                        INSERT INTO user_tb (email, name, phone_number, uuid, account_type)
                        VALUES (?,?,?,?,?)
                        returning user_id
                        """,
                Integer.class,
                email,
                "이준훈테스트",
                phone,
                UUID.randomUUID(),
                AccountType.USER.getValue()
        );

        return userId;
    }

    private Integer givenCreateTeam(Integer userId){
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "entry-folder-" + suffix + "@test.com";
        String phone = "010" + String.valueOf(System.nanoTime()).substring(0, 8);

        Integer teamId = jdbcTemplate.queryForObject(
                """
                INSERT INTO  team_tb (name, create_user_id, uuid, status)
                VALUES (?,?,?,?)
                returning team_id
                        """,
                Integer.class,
                "이준훈 팀 테스트",
                userId,
                UUID.randomUUID(),
                TeamStatus.ACTIVE.getValue()
        );

        return teamId;
    }

    private Map<String, Object> givenPrivateEntryFolder(Integer userId){
        folderService.createPrivateEntryFolder("이준훈테스트의 개인폴더", userId);
        List<Map<String, Object>> folders = jdbcTemplate.queryForList(
                """
                SELECT folder_id, resource_owner_id, resource_scope_user_id, resource_scope_team_id, uuid
                FROM folder_tb
                WHERE resource_owner_id = ?
                  AND folder_type = ?
                  AND parent_folder_id IS NULL
                """,
                userId,
                FolderType.PRIVATE.getValue()
        );
        assertThat(folders)
                .as("개인 엔트리 폴더는 정확히 1개 생성되어야 한다.")
                .hasSize(1);
        return folders.getFirst();
    }

    private Map<String, Object> givenTeamEntryFolder(Integer teamId, Integer createUserId){
        folderService.createTeamEntryFolder("이준훈테스트의 팀 폴더", teamId, createUserId);
        List<Map<String, Object>> folders = jdbcTemplate.queryForList(
                """
                SELECT folder_id, resource_owner_id, resource_scope_user_id, resource_scope_team_id, uuid
                FROM folder_tb
                WHERE resource_scope_team_id = ?
                  AND resource_owner_id = ?
                  AND folder_type = ?
                  AND parent_folder_id IS NULL
                """,
                teamId,
                createUserId,
                FolderType.TEAM.getValue()
        );
        assertThat(folders)
                .as("팀 엔트리 폴더는 정확히 1개 생성되어야 한다.")
                .hasSize(1);
        return folders.getFirst();
    }


}
