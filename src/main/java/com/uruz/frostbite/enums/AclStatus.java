package com.uruz.frostbite.enums;

public enum AclStatus {
    ACTIVE(1),
    DEACTIVATED(2),
    DELETED(3);

    private final int value;

    AclStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AclStatus fromValue(int value) {
        for (AclStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AclStatus value: " + value);
    }
}
