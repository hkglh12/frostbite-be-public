package com.uruz.frostbite.enums;

public enum UserStatus {
    AWAITS(0),
    ACTIVE(1),
    LOCKED(2),
    DELETED(3);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UserStatus fromValue(int value) {
        for (UserStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus value: " + value);
    }
}
