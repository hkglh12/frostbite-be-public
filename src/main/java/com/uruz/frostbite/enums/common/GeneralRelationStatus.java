package com.uruz.frostbite.enums.common;

public enum GeneralRelationStatus {
    AWAITS(0),
    ACTIVE(1),
    DELETED(3);

    private final int value;

    GeneralRelationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GeneralRelationStatus fromValue(int value) {
        for (GeneralRelationStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown GeneralRelationStatus value: " + value);
    }
}
