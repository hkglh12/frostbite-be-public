package com.uruz.frostbite.integration;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.FolderCreateRequest;
import com.uruz.frostbite.dto.folder.FolderMoveRequest;
import com.uruz.frostbite.dto.folder.SimpleFolderResponse;
import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.enums.*;
import com.uruz.frostbite.enums.user.AccountType;
import com.uruz.frostbite.service.FolderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
    JdbcTemplate jdbcTemplate;

    @Transactional
    @Rollback
    @Test
    @DisplayName("개인 ROOT 폴더 생성 테스트, default ACL이 함꼐 생성되어야 한다.")
    public void createPrivateRootFolderShouldCreateDefaultAcl(){
        Integer userId = givenCreateUser();
        Map<String, Object> parentFolder = givenEntryPrivateFolder(userId);

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
    @DisplayName("개인 Entry 폴더 생성 테스트, default ACL이 함꼐 생성되어야 한다.")
    public void createEntryPrivateFolderShouldCreateDefaultAcl(){
        // GIVEN -> 사용자 생성 -> EntryFolder 생성
        // 사용자 생성 비즈니스 로직이 이미 폴더생성과 엮여있음 -> 폴더 단위테스트에 부적합. 따라서 JdbcTemplate로 직접 insert

        Integer userId = givenCreateUser();
        Map<String, Object> folder = givenEntryPrivateFolder(userId);

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
                .as("개인 폴더 PermissinoBitMask는 * 권한이어야 한다.").isEqualTo(PermissionBitMask.of(PermissionBitMask.allMasks()));
        assertThat(( (Number)folderAcl.get("subject_type")).intValue() )
                .as("개인 폴더 ACL은 subject_type USER로 지정되어야 한다.").isEqualTo(AclSubjectType.USER.getValue());
        assertThat(folderAcl.get("subject_team_id"))
                .as("개인 폴더 ACL은 subject_team_id NULL이어야 한다.").isNull();
    }
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

    private Map<String, Object> givenEntryPrivateFolder(Integer userId){
        folderService.createEntryPrivateFolder("이준훈테스트의 개인폴더", userId);
        List<Map<String, Object>> folders = jdbcTemplate.queryForList(
                """
                SELECT folder_id, resource_owner_id, resource_scope_user_id, resource_scope_team_id
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

}
