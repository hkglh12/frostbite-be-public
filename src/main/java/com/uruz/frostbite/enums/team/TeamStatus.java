package com.uruz.frostbite.enums.team;

public enum TeamStatus {
    AWAITS(0),
    ACTIVE(1),
    LOCKED(2),
    DELETED(3);

    private final int value;

    TeamStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TeamStatus fromValue(int value) {
        for (TeamStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TeamStatus value: " + value);
    }
}
