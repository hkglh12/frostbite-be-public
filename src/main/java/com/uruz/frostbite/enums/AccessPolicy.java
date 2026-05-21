package com.uruz.frostbite.enums;

public enum AccessPolicy {
    USER(1),
    TEAM(2),
    TEAM_MEMBER_SPECIFIC(2);

    private final int value;

    AccessPolicy(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AccessPolicy fromValue(int value) {
        for (AccessPolicy status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AclEffectType value: " + value);
    }
}
