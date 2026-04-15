package com.uruz.frostbite.enums;

public enum FolderStatus {
    ACTIVE(1),
    DEACTIVATED(2),
    DELETED(3);

    private final int value;

    FolderStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FolderStatus fromValue(int value) {
        for (FolderStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown folderstatus value: " + value);
    }
}
