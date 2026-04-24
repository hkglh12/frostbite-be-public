package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclStatus;
import com.uruz.frostbite.enums.AclSubjectType;
import com.uruz.frostbite.enums.PermissionBitMask;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FolderAcl {

    /** 접근제어 식별자 */
    private Long aclId;

    /** 접근제어 보호대상 Object (폴더) 식별자 */
    private Long folderId;

    /** 접근제어 종류 (ALLOW || DENY) */
    private AclEffectType effectType;

    /** 접근제어 주체 종류 (팀 || 사용자) */
    private AclSubjectType subjectType;

    /** 접근제어 주체 식별자 */
    private Integer subjectId;

    /** 제약 권한 (READ, CREATE, DELETE, SHARE 등 ENUM) */
    private PermissionBitMask permissionBitMask;

    /** 공유로 생성된 제약 여부 */
    private boolean isShared;

    /** 접근제어 생성시각 */
    private LocalDateTime createTime;

    /** 제약 생성자 (user_id) */
    private Integer createUser;

    /** 삭제여부 */
    private AclStatus status;
}
