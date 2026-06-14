package com.uruz.frostbite.enums;

public enum FileTempStatus {
    READY(1),
    ACTIVE(2),
    EXPIRED(3),
    HARD_DELETED(4);

    private final int value;

    FileTempStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FileTempStatus fromValue(int value) {
        for (FileTempStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FileStatus value: " + value);
    }
}
