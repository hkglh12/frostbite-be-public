## Workflow 도메인

- 최초 Workflow는 file에 귀속되는 file_workflow 였으나, 폴더를 대상으로도 workflow를 수행할 수 있으므로
  별도 엔티티로 격상
- file_workflow_status_tb 는 파일 관점에서, 파일의 상태정보를 관리하는것이며 workflow table은 "워크플로우 입장에서의 기록", "진실의 원장" 역할을 수행한다.

- 워크플로우는 워크플로우 흐름 자체르 구성하는 단위 유닛작업들의 래퍼 개념이다.

### 워크플로우의 관리 (관리테이블 별도 고려 )

- workflow_request_tb
```
workflow_request_id bigserial primary key,
workflow_name varchar(1024),
target_domain varchar(128),
target_id bigint,
error_message text,
status smallint,
create_time timestamp,
create_user_id integer,
start_time timestamp,
end_time timestamp,
```

- 각 유형별 워크플로우를 실행함에 따라, 각각의 워크플로우의 목적에 맞는 결과를 획득할 수 있다.
    - 워크플로우가 파일 대상뿐 아니라 폴더 대상일 수 있으므로, 확장성을 고려하여 설계한다.
        - 예를 들면, target_domain = file 의 경우, 파일 등록, 파일 섬네일 생성 등등을 등록할 수 있다.
        - 예를 들면, target_domain = folder의 경우 폴더이동, 대용량 폴더 배치삭제 등을 예로 들 수 있다.
- 이 워크플로우 대상은 "진실의 원장" 역할을 해, 실제로 어떤 "워크플로우 실행 요청이 언제 들어갔으며, 언제 완료되었음" 을 워크플로우 관점에서 관리한다.
    - 즉, 일례로 file_workflow_status_tb 는 파일의 관점에서 workflow 실행 이력을 남기는것이며, 워크플로우 기록 테이블은 시스템 관점에서 진실의 원장 역할을 수행한다.
    - 워크플로우 재시도 시, 이전의 워크플로우 기록 row를 수정하는게 아니라, 새로운 workflow 요청목록을 생성해서 해당 버전으로 처리하는, 감사/원장 역할을 수행한다.

### 워크플로우 단위유닛

- workflow_unit_request_tb
```
workflow_unit_request_id bigserial primary key,
workflow_request_id bigint,
work_unit_name varchar(1024),
unit_sequence_in_workflow int,
error_message text,
status smallint,
allow_retry smallint,
retry_count int,
create_time timestamp,
create_user_id integer,
start_time timestamp,
end_time timestamp,
```


### 워크플로우 실패 및  종료

- 워크플로우 단위유닛이 실패할경우,
  - workflow_unit_request_tb status = FAILED_RETRYABLE, workflow_request_tb status = FAILED_RETRYABLE로 전환한다.
  - 이후 fallback second 1분을 지정하고, fallback seconds 만료 시 
    - retry_count++ -> 재시도 ->  3회까지 재시도 후 -> 해당 시도에도 실패할경우 allow_retry 를 0으로 전환, status를 FAILED_FINAL로 전환한다.
  - FAILED_FINAL 까지 떨어진 경우 workflow의 상태도 FAILED_FINAL로 전환한다.

- 수행 결과를 SSE가 체결되어있으면 FE에 리턴하고, 체결되있지 않다면 서버에서 메시지를 삼킨다.
- 사용자는 추후 메타정보 조회시에 확인이 가능하며, 사용자가 명시적으로 재시도를 요청할 수 있다.


# 중요!
- 추후, Workflow name을 enum으로 관리해서 FE의 ENUM 인자값만으로 워크플로우 휘하 유닛들을 지정할 수 있게 해야한다
- 추후, workflow Unit name을 ENUM으로 관리해서 FE의 ENUM 인자값만으로 워크플로우 유닛이 일괄 등록될 수 있게 해야한다.
- 추후, Workflow / Workflow Unit 을 '설정'할 수 있는 DB 테이블을 생성해야한다.