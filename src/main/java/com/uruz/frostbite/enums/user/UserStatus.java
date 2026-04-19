package com.uruz.frostbite.enums.user;

public enum UserStatus {
    AWAITS(0),
    ACTIVE(1),
    LOCKED(2),
    EXPIRED(3),
    DELETED(4);

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
