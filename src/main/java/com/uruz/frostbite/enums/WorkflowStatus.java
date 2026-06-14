package com.uruz.frostbite.enums;

public enum WorkflowStatus {
    READY(1),
    PROCESSING(2),
    SUCCESS(3),
    FAILED_RETRYABLE(4),
    FAILED_FINAL(5);

    private final int value;

    WorkflowStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static WorkflowStatus fromValue(int value) {
        for (WorkflowStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FileStatus value: " + value);
    }
}
