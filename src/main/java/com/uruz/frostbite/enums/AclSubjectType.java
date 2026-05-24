package com.uruz.frostbite.enums;

/** ACL 접근 허가 대상 종류 (팀 || 개인) */
public enum AclSubjectType {
    // FolderType과 같은 값을 가지지만, 목적이 다르므로 분리
    USER(1),
    TEAM(2);

    private final int value;

    AclSubjectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AclSubjectType fromValue(int value) {
        for (AclSubjectType status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AclSubjectType value: " + value);
    }
}
