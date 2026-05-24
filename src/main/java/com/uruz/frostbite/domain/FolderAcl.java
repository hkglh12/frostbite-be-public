package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclStatus;
import com.uruz.frostbite.enums.AclSubjectType;
import com.uruz.frostbite.enums.PermissionBitMask;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
// bit mask 직접 접근 방지
public class FolderAcl {

    /** 접근제어 식별자 */
    private Long aclId;

    /** 접근제어 보호대상 Object (폴더) 식별자 */
    private Long folderId;

    /** 접근제어 주체 종류 (팀 || 사용자) */
    private AclSubjectType subjectType;

    /** 접근제어 종류 (ALLOW || DENY) */
    private AclEffectType effectType;

    /** 접근제어 주체 식별자 */
    // 각 subjectType 별로 subjectType=team -> teamId , subjectType=user -> userId 값을 넣어야함.
    private Integer subjectTeamId;
    private Integer subjectUserId;

    /** 제약 권한 (READ, CREATE, DELETE, SHARE 등 ENUM) 비트연산 처리 */
    private Integer permissionBitMask;

    /** 삭제여부 */
    private AclStatus status;

    /** 접근제어 생성시각 */
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    private LocalDateTime deleteTime;

    /** 제약 생성자 (user_id) */
    private Integer createUserId;
    private Integer modifyUserId;
    private Integer deleteUserId;

    public static FolderAcl createUserAllow(
            Long folderId,
            Integer subjectUserId,
            Integer createUserId,
            PermissionBitMask... permissions
    ) {
        FolderAcl acl = new FolderAcl();
        acl.folderId = folderId;
        acl.effectType = AclEffectType.ALLOW;
        acl.subjectType = AclSubjectType.USER;
        acl.subjectUserId = subjectUserId;
        acl.subjectTeamId = null;
        acl.permissionBitMask = PermissionBitMask.of(permissions);
        acl.createUserId = createUserId;
        acl.createTime = LocalDateTime.now();
        acl.status = AclStatus.ACTIVE;
        return acl;
    }

    /** 비트마스크 검사 메서드 */
    public boolean hasPermission(PermissionBitMask permission) {
        return PermissionBitMask.has(permissionBitMask, permission);
    }

    public boolean hasAllPermissions(PermissionBitMask... permissions) {
        return PermissionBitMask.hasAll(permissionBitMask, permissions);
    }

    public boolean hasAnyPermission(PermissionBitMask... permissions) {
        return PermissionBitMask.hasAny(permissionBitMask, permissions);
    }

}
