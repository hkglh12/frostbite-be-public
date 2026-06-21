package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.WorkflowStatus;
import com.uruz.frostbite.enums.workflow.UnitTask;
import com.uruz.frostbite.enums.workflow.UnitTaskRequestStatus;
import com.uruz.frostbite.enums.workflow.WorkflowDomainType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitTaskRequest {

    /**
     * UnitTask 요청 PK
     */
    private Long unitTaskRequestId;

    /**
     * 상위 Workflow 요청 PK
     */
    private Long workflowRequestId;

    /**
     * 수행할 UnitTask 종류
     *
     * 예)
     * UT_FILE_ENCRYPT
     * UT_FILE_COPY_STORAGE
     */
    private UnitTask unitTaskName;

    /**
     * Workflow 내 수행 순서
     *
     * 운영 시 장애 분석을 위해 보관한다.
     *
     * 예)
     * 1 : 암호화
     * 2 : 영속 스토리지 이동
     * 3 : 활용본 생성
     * 4 : 검색 동기화
     */
    private Integer taskOrder;

    private WorkflowDomainType workflowDomainType;

    /**
     * UnitTask 수행 대상 PK
     */
    private Long targetId;

    /**
     * UnitTask 실패 시 에러 메시지
     */
    private String errorMessage;

    /**
     * UnitTask 수행 상태
     */
    private UnitTaskRequestStatus status;

    /**
     * 재시도 가능 여부
     *
     * 0 : 불가능
     * 1 : 가능
     */
    private Boolean allowRetry;

    /**
     * 재시도 횟수
     */
    private Integer retryCount = 0;

    /**
     * UnitTask 생성 시각
     */
    private LocalDateTime createTime;

    /**
     * UnitTask 생성 사용자
     */
    private Integer createUserId;

    /**
     * UnitTask 실제 수행 시작 시각
     */
    private LocalDateTime startTime;

    /**
     * UnitTask 종료 시각
     *
     * 성공/실패 여부와 무관하게
     * 종료 시점 기록
     */
    private LocalDateTime endTime;

    public static UnitTaskRequest createUnitTask(UnitTask unitTask, long workflowRequestId,
                                            int taskOrder, WorkflowDomainType domainType, long targetId,
                                            int createUserId, LocalDateTime currentTime){
        return UnitTaskRequest.builder()
                .workflowRequestId(workflowRequestId)
                .unitTaskName(unitTask)
                .taskOrder(taskOrder)
                .workflowDomainType(domainType)
                .targetId(targetId)
                .createTime(currentTime)
                .status(UnitTaskRequestStatus.READY)
                .createUserId(createUserId)
                .allowRetry(true)
                .build();

    }
    public boolean isFailedFinal(){
        return this.status == UnitTaskRequestStatus.FAILED_FINAL;
    }

    public boolean isFailedRetryable(){
        return this.status == UnitTaskRequestStatus.FAILED_RETRYABLE && allowRetry;
    }

    public boolean isSucceeded() {
        return this.status == UnitTaskRequestStatus.SUCCEEDED;
    }
}
