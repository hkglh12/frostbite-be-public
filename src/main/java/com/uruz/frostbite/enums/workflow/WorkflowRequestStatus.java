package com.uruz.frostbite.enums.workflow;

public enum WorkflowRequestStatus {
    // DB에만 적재되어있음
    WAITING,
    // async workerThrad에 배정, workflow 요청 식별 완료
    READY,
    // payload parsing 성공 + 워크플로우 실행중
    PROCESSING,
    // 워크플로우 정상종료
    SUCCEEDED,
    // 워크플로우 실패, 재시도 가능
    FAILED_RETRYABLE,
    // 재시도 가능 횟수까지 모두 사용, 완전 실패.
    FAILED_FINAL
}
