package com.uruz.frostbite.support;

import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclSubjectType;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.team.TeamStatus;
import com.uruz.frostbite.enums.user.UserStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

public final class TestDataQuerySupport {

    private final JdbcTemplate jdbcTemplate;

    public TestDataQuerySupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FolderRow> findFoldersByParent(Long parentFolderId, FolderType folderType) {
        return jdbcTemplate.query(
                """
                SELECT folder_id, uuid, folder_type, parent_folder_id,
                       resource_scope_team_id, resource_scope_user_id, resource_owner_id,
                       
                FROM folder_tb
                WHERE parent_folder_id = ? AND folder_type = ?
                ORDER BY folder_id
                """,
                (rs, rowNum) -> new FolderRow(
                        rs.getLong("folder_id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        FolderType.fromValue(rs.getInt("folder_type")),
                        nullableLong(rs, "parent_folder_id"),
                        nullableInteger(rs, "resource_scope_team_id"),
                        nullableInteger(rs, "resource_scope_user_id"),
                        nullableInteger(rs, "resource_owner_id"),
                        rs.getString("folder_id_path")
                ),
                parentFolderId,
                folderType.getValue()
        );
    }

    public List<FolderRow> findEntryFoldersByUser(Integer userId) {
        return findEntryFolders("resource_scope_user_id", userId, FolderType.PRIVATE);
    }

    public List<FolderRow> findEntryFoldersByTeam(Integer teamId) {
        return findEntryFolders("resource_scope_team_id", teamId, FolderType.TEAM);
    }

    public List<AclRow> findAclsByFolder(Long folderId) {
        return jdbcTemplate.query(
                """
                SELECT acl_id, folder_id, subject_type, effect_type,
                       subject_team_id, subject_user_id, permission_bit_mask
                FROM folder_acl_tb
                WHERE folder_id = ?
                ORDER BY acl_id
                """,
                (rs, rowNum) -> new AclRow(
                        rs.getLong("acl_id"),
                        rs.getLong("folder_id"),
                        AclSubjectType.fromValue(rs.getInt("subject_type")),
                        AclEffectType.fromValue(rs.getInt("effect_type")),
                        nullableInteger(rs, "subject_team_id"),
                        nullableInteger(rs, "subject_user_id"),
                        nullableInteger(rs, "permission_bit_mask")
                ),
                folderId
        );
    }

    public TeamRow findTeam(UUID teamUuid) {
        return jdbcTemplate.queryForObject(
                """
                SELECT team_id, uuid, name, status, create_user_id, modify_user_id
                FROM team_tb
                WHERE uuid = ?
                """,
                (rs, rowNum) -> new TeamRow(
                        rs.getInt("team_id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        TeamStatus.fromValue(rs.getInt("status")),
                        nullableInteger(rs, "create_user_id"),
                        nullableInteger(rs, "modify_user_id")
                ),
                teamUuid.toString()
        );
    }

    public UserRow findUserByEmail(String email) {
        return jdbcTemplate.queryForObject(
                """
                SELECT user_id, uuid, name, email, phone_number, status
                FROM user_tb
                WHERE email = ?
                """,
                (rs, rowNum) -> new UserRow(
                        rs.getInt("user_id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        UserStatus.fromValue(rs.getInt("status"))
                ),
                email
        );
    }

    public int countTeamRelations(Integer userId, Integer teamId) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rel_user_team_tb WHERE user_id = ? AND team_id = ?",
                Integer.class,
                userId,
                teamId
        );
    }

    private List<FolderRow> findEntryFolders(String scopeColumn, Integer scopeId, FolderType folderType) {
        String sql = """
                SELECT folder_id, uuid, name, folder_type, parent_folder_id,
                       resource_scope_team_id, resource_scope_user_id, resource_owner_id,
                       folder_id_path
                FROM folder_tb
                WHERE %s = ? AND folder_type = ? AND parent_folder_id IS NULL
                ORDER BY folder_id
                """.formatted(scopeColumn);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new FolderRow(
                        rs.getLong("folder_id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        FolderType.fromValue(rs.getInt("folder_type")),
                        nullableLong(rs, "parent_folder_id"),
                        nullableInteger(rs, "resource_scope_team_id"),
                        nullableInteger(rs, "resource_scope_user_id"),
                        nullableInteger(rs, "resource_owner_id"),
                        rs.getString("folder_id_path")
                ),
                scopeId,
                folderType.getValue()
        );
    }

    private static Integer nullableInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Long nullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    public record FolderRow(
            Long id,
            UUID uuid,
            String name,
            FolderType type,
            Long parentId,
            Integer scopeTeamId,
            Integer scopeUserId,
            Integer ownerId,
            String idPath
    ) {
    }

    public record AclRow(
            Long id,
            Long folderId,
            AclSubjectType subjectType,
            AclEffectType effectType,
            Integer subjectTeamId,
            Integer subjectUserId,
            Integer permissionBitMask
    ) {
    }

    public record TeamRow(
            Integer id,
            UUID uuid,
            String name,
            TeamStatus status,
            Integer createUserId,
            Integer modifyUserId
    ) {
    }

    public record UserRow(
            Integer id,
            UUID uuid,
            String name,
            String email,
            String phoneNumber,
            UserStatus status
    ) {
    }
}
