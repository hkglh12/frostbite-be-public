package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.WorkflowStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileWorkflowStatus {

    private Long fileId;
    private LocalDateTime fileRegistStartTime;
    private WorkflowStatus fileRegistStatus;
    private LocalDateTime fileRegistCompleteTime;
    private String message;

    public static FileWorkflowStatus createWaiting(Long fileId) {
        FileWorkflowStatus status = new FileWorkflowStatus();
        status.fileId = fileId;
        status.fileRegistStatus = WorkflowStatus.WAITING;
        return status;
    }

    public void markReady() {
        this.fileRegistStatus = WorkflowStatus.READY;
        this.fileRegistStartTime = LocalDateTime.now();
    }

    public void startRegister() {
        this.fileRegistStatus = WorkflowStatus.PROCESSING;
        this.fileRegistStartTime = LocalDateTime.now();
    }

    public void completeRegister() {
        this.fileRegistStatus = WorkflowStatus.SUCCESS;
        this.fileRegistCompleteTime = LocalDateTime.now();
    }

    public void failRetryable(String message) {
        this.fileRegistStatus = WorkflowStatus.FAILED_RETRYABLE;
        this.message = message;
    }

    public void failFinal(String message) {
        this.fileRegistStatus = WorkflowStatus.FAILED_FINAL;
        this.message = message;
    }
}