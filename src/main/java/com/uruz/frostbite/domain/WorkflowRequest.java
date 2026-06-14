package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.WorkflowStatus;

import java.time.LocalDateTime;

public class WorkflowRequest {

    /** 워크플로우 요청 식별자 */
    private Long workflowRequestId;

    /** 워크플로우명 */
    private String workflowName;

    /** 대상 도메인(file, folder 등) */
    private String targetDomain;

    /** 대상 식별자 */
    private Long targetId;

    /** 오류 메시지 */
    private String errorMessage;

    /** 워크플로우 상태 */
    private WorkflowStatus status;

    /** 생성 시각 */
    private LocalDateTime createTime;

    /** 생성자 */
    private Integer createUserId;

    /** 시작 시각 */
    private LocalDateTime startTime;

    /** 종료 시각 */
    private LocalDateTime endTime;
}
