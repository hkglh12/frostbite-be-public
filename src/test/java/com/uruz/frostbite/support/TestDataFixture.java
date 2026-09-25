package com.uruz.frostbite.support;

import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclStatus;
import com.uruz.frostbite.enums.AclSubjectType;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.enums.common.GeneralRelationStatus;
import com.uruz.frostbite.enums.team.TeamRole;
import com.uruz.frostbite.enums.team.TeamStatus;
import com.uruz.frostbite.enums.user.AccountType;
import com.uruz.frostbite.enums.user.UserStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public final class TestDataFixture {

    private final JdbcTemplate jdbcTemplate;

    public TestDataFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TestUser selectExistsUser(String email){
        Map<String, Object> userMap = jdbcTemplate.queryForMap(
                """
                        SELECT name, email, user_id, uuid, status
                        FROM user_tb
                        WHERE email = ?
                        """,
                email);

        return new TestUser((Integer)userMap.get("user_id"), (UUID)userMap.get("uuid"), (String)userMap.get("name"), (String)userMap.get("email"));


    }
    public TestUser createUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UUID uuid = UUID.randomUUID();
        String name = "test-user-" + suffix;
        String email = "test-" + suffix + "@example.com";
        String phoneNumber = String.format("010%08d", Math.floorMod(uuid.hashCode(), 100_000_000));

        Integer userId = jdbcTemplate.queryForObject(
                """
                INSERT INTO user_tb (
                    name, email, password, phone_number,  create_date, uuid, account_type, status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING user_id
                """,
                Integer.class,
                name,
                email,
                "encoded-password",
                phoneNumber,
                LocalDate.of(1990, 1, 1),
                LocalDate.now(),
                uuid,
                AccountType.USER.getValue(),
                UserStatus.ACTIVE.getValue()
        );

        return new TestUser(userId, uuid, name, email);
    }

    public TestTeam createTeam(Integer createUserId) {
        UUID uuid = UUID.randomUUID();
        String name = "test-team-" + uuid.toString().substring(0, 8);

        Integer teamId = jdbcTemplate.queryForObject(
                """
                INSERT INTO team_tb (name, create_time, create_user_id, uuid, status)
                VALUES (?, ?, ?, ?, ?)
                RETURNING team_id
                """,
                Integer.class,
                name,
                LocalDateTime.now(),
                createUserId,
                uuid,
                TeamStatus.ACTIVE.getValue()
        );

        return new TestTeam(teamId, uuid, name, createUserId);
    }

    public void createTeamRelation(Integer userId, Integer teamId, TeamRole role) {
        jdbcTemplate.update(
                """
                INSERT INTO rel_user_team_tb (user_id, team_id, create_user_id, join_time, role, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                userId,
                teamId,
                userId,
                LocalDateTime.now(),
                role.getValue(),
                GeneralRelationStatus.ACTIVE.getValue()
        );
    }

    public TestFolder createPrivateEntryFolder(Integer userId) {
        return createFolder(FolderType.PRIVATE, null, null, userId, userId, userId);
    }

    public TestFolder createTeamEntryFolder(Integer teamId, Integer createUserId) {
        return createFolder(FolderType.TEAM, null, teamId, null, createUserId, createUserId);
    }

    public TestFolder createPrivateFolder(Long parentFolderId, Integer userId) {
        return createFolder(FolderType.PRIVATE, parentFolderId, null, userId, userId, userId);
    }

    public TestFolder createTeamFolder(Long parentFolderId, Integer teamId, Integer createUserId) {
        return createFolder(FolderType.TEAM, parentFolderId, teamId, null, createUserId, createUserId);
    }

    public Long createUserAllowAcl(Long folderId, Integer userId, PermissionBitMask... permissions) {
        return createAcl(
                folderId,
                AclSubjectType.USER,
                null,
                userId,
                AclEffectType.ALLOW,
                PermissionBitMask.of(permissions),
                userId
        );
    }

    public Long createTeamAllowAcl(Long folderId, Integer teamId, Integer createUserId, PermissionBitMask... permissions) {
        return createAcl(
                folderId,
                AclSubjectType.TEAM,
                teamId,
                null,
                AclEffectType.ALLOW,
                PermissionBitMask.of(permissions),
                createUserId
        );
    }

    public Long createUserDenyAcl(Long folderId, Integer userId) {
        return createAcl(
                folderId,
                AclSubjectType.USER,
                null,
                userId,
                AclEffectType.DENY,
                null,
                userId
        );
    }

    private TestFolder createFolder(
            FolderType folderType,
            Long parentFolderId,
            Integer scopeTeamId,
            Integer scopeUserId,
            Integer ownerId,
            Integer createUserId
    ) {
        Long folderId = jdbcTemplate.queryForObject("SELECT nextval('folder_tb_folder_id_seq')", Long.class);
        UUID uuid = UUID.randomUUID();
        String name = "test-folder-" + uuid.toString().substring(0, 8);



        jdbcTemplate.update(
                """
                INSERT INTO folder_tb (
                    uuid, folder_id, folder_name, folder_type, parent_folder_id,
                    resource_scope_team_id, resource_scope_user_id, resource_owner_id,
                    render_sequence,
                    status, create_user_id, create_time
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                uuid,
                folderId,
                name,
                folderType.getValue(),
                parentFolderId,
                scopeTeamId,
                scopeUserId,
                ownerId,
                0,
                FolderStatus.ACTIVE.getValue(),
                createUserId,
                LocalDateTime.now()
        );

        return new TestFolder(folderId, uuid, name, folderType, parentFolderId, scopeTeamId, scopeUserId, ownerId);
    }

    private Long createAcl(
            Long folderId,
            AclSubjectType subjectType,
            Integer subjectTeamId,
            Integer subjectUserId,
            AclEffectType effectType,
            Integer permissionBitMask,
            Integer createUserId
    ) {
        Long aclId = jdbcTemplate.queryForObject("SELECT nextval('folder_acl_tb_acl_id_seq')", Long.class);
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO folder_acl_tb (
                    acl_id, folder_id, subject_type, effect_type, subject_team_id, subject_user_id,
                    permission_bit_mask, status, create_time, create_user_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING acl_id
                """,
                Long.class,
                aclId,
                folderId,
                subjectType.getValue(),
                effectType.getValue(),
                subjectTeamId,
                subjectUserId,
                permissionBitMask,
                AclStatus.ACTIVE.getValue(),
                LocalDateTime.now(),
                createUserId
        );
    }

    public record TestUser(Integer id, UUID uuid, String name, String email) {
    }

    public record TestTeam(Integer id, UUID uuid, String name, Integer createUserId) {
    }

    public record TestFolder(
            Long id,
            UUID uuid,
            String name,
            FolderType type,
            Long parentId,
            Integer scopeTeamId,
            Integer scopeUserId,
            Integer ownerId
    ) {
    }
}
