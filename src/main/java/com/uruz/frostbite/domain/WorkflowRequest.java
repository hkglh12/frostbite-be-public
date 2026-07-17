package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.workflow.Workflow;
import com.uruz.frostbite.enums.workflow.WorkflowDomainType;
import com.uruz.frostbite.enums.workflow.WorkflowRequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class WorkflowRequest {

        /**
         * Workflow 요청 PK
         */
        private Long workflowRequestId;

        /**
         * 수행할 Workflow 종류
         *
         * 예)
         * WF_FILE_PROVISION_NORMAL
         * WF_FILE_PROVISION_INTERNAL
         */
        private Workflow workflowName;

        /**
         * Workflow 대상 도메인
         *
         * FILE
         * FOLDER
         */
        private WorkflowDomainType workflowDomainType;

        /** Payload (workflow 호출 시 필요한 정보 집합) */
        // DB에 저장 자체는 JSONB, Java에서 받을때는 String으로 처리
        // 사용시점에 ObjectMapper로 read -> Map 변환 필요
        private String requestPayload;

        /**
         * Workflow 대상 PK
         *
         * targetDomain 이 FILE 인 경우
         * file_id
         *
         * targetDomain 이 FOLDER 인 경우
         * folder_id
         */
        private Long targetId;

        /**
         * Workflow 실패 시 에러 메시지
         */
        private String errorMessage;

        /**
         * Workflow 전체 수행 상태
         */
        private WorkflowRequestStatus status;

        /**
         * Workflow 생성 시각
         */
        private LocalDateTime createTime;

        /**
         * Workflow 생성 사용자
         */
        private Integer createUserId;

        /**
         * Workflow 실제 수행 시작 시각
         */
        private LocalDateTime startTime;

        /**
         * Workflow 종료 시각
         *
         * 성공/실패 여부와 무관하게
         * 종료 시점 기록
         */
        private LocalDateTime endTime;

    public static WorkflowRequest createWaiting(Workflow workflowName, WorkflowDomainType targetType,
                                                Long targetId, String requestPayloadString, int createUserId, LocalDateTime current) {
        return WorkflowRequest.builder()
                .workflowName(workflowName)
                .workflowDomainType(targetType)
                .targetId(targetId)
                .requestPayload(requestPayloadString)
                .status(WorkflowRequestStatus.WAITING)
                .createTime(current)
                .createUserId(createUserId)
                .build();
    }
}
