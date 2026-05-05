package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
/** FILEACL은 공유 / DENY 경우에 활용, 일반적 경우 폴더권한으로 처리 필요*/
public class FileAcl {
    
    /** 접근제어 식별자 */
    private Long aclId;

    /** 피접근제어 대상 (user_id) */
    private Integer userId;

    /** 접근제어 파일 식별자 */
    private Long fileId;

    /** 접근제어 종류 (ALLOW || DENY) */
    private Integer effectType;

    /** 피접근제어 종류 (팀 || 사용자) */
    private Integer subjectType;

    /** 피접근제어 식별자 */
    private Integer subjectId;

    /** 제약 권한 (READ, CREATE, DELETE, SHARE 등 ENUM) */
    private Integer permission;

    /** 공유로 생성된 제약 여부 */
    private Short isShared;

    /** 접근제어 생성시각 */
    private LocalDateTime createTime;

    /** 제약 생성자 (user_id) */
    private Integer createUser;

    /** 삭제여부 */
    private Short isDelete;
}
