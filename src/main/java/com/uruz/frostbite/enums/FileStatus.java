package com.uruz.frostbite.enums;

public enum FileStatus {
    UNREGISTERED(1),
    ACTIVE(2),
    SOFT_DELETED(3),
    DELETED(4);

    private final int value;

    FileStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FileStatus fromValue(int value) {
        for (FileStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FileStatus value: " + value);
    }
}
