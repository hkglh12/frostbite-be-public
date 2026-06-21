package com.uruz.frostbite.enums.workflow;

import lombok.Getter;


@Getter
public enum UnitTaskRequestStatus {
    READY,
    PROCESSING,
    SUCCEEDED,
    FAILED_RETRYABLE,
    FAILED_FINAL,
    SKIPPED
}
