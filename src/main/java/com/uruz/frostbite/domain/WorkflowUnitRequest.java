package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.WorkflowStatus;

import java.time.LocalDateTime;

public class WorkflowUnitRequest {
    /** 워크플로우 단위유닛 요청 식별자 */
    private Long workflowUnitRequestId;

    /** 워크플로우 요청 식별자 */
    private Long workflowRequestId;

    /** 단위유닛명 */
    private String workUnitName;

    /** 워크플로우 내 실행 순서 */
    private Integer unitSequenceInWorkflow;

    /** 오류 메시지 */
    private String errorMessage;

    /** 단위유닛 상태 */
    private WorkflowStatus status;

    /** 재시도 허용 여부 */
    private Short allowRetry;

    /** 재시도 횟수 */
    private Integer retryCount;

    /** 생성 시각 */
    private LocalDateTime createTime;

    /** 생성자 */
    private Integer createUserId;

    /** 시작 시각 */
    private LocalDateTime startTime;

    /** 종료 시각 */
    private LocalDateTime endTime;
}
