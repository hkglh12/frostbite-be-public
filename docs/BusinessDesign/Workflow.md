## Workflow , UnitTask 정의

- 작업의 흐름, 즉 단위 작업의 뭉치로 특정 순서를 지켜서 수행되어야 하는 작업 뭉치를 워크플로우라고 명칭한다.
- 워크플로우의 각각 단위 작업을 유닛태스크 (단위작업)이라 명칭한다.
- 친숙한 명칭을 위해 workflow 라는 이름으로 개발을 시작했으나, 개발하다보니 PipeLine에 더 가까운것 같다. PipeLine으로 인식하면 좋을듯.


## 개정
- 최초 workflow는 file에만 적용되는 개념이었음
  - 사고를 확장해, 폴더이동도 동기방식이 아니라 "요청" 후 "접근금지" 상태로 전환하고, 대용량 작업을 수행할 수 있겠다 는 판단이 들었음
- 이에 따라, workflow를 종속개념이 아닌 별도 도메인 엔티티로 격상
- 단, 폴더단위 워크플로우는 Phase3에 추가하고 Phase1에서는 파일을 대상으로 In Was Code 식으로 개발
  - Phase2에서는 Was code에서 분리해서 엔진방식 || none WEB JAR 방식 || Python 단위모듈 호출 방식으로 전환

* Phase3에서 중간값 보존 후 재시작 / 재요청 지원

# Phase1
- 각 단위작업을 수행하는 워크플로우 @Service 개발, 추상화 한 Interface Implement로 다형성 제공
- 최초 Spring initialize 시 위 @Service를 전역 Immutable Map에 삽입, 파라미터로 메서드 호출할 수 있도록 개발

## Unit Task 관리
- 유닛태스크는 Phase1에서 In code 베이스로 개발한다.
- 각 동작명을 ENUM에 선언해두고, 그 ENUM 값을 가지고 유닛태스크 수행 메서드를 @Service로 개발한다.

- Phase2에서는 유닛태스크 관리를 코드 밖으로 꺼내야함.
  - 단, 유닛태스크 명이 변경되거나 기준점이 변경되어서는 안됨. DB Update를 요구하게 되면 -> 히스토리성 테이블이 update 발생. 이부분 방지해야함
## Workflow 관리
- Workflow 이름명은 Phase1에서 In code 베이스로 개발한다.
- 단일 워크플로우 내에 수행해야 하는 유닛태스크를 모아둔 Array를 선언하고, 불변 값으로 선언해둔다 (ENUM 권장)
- 

- Phase2에서는 워크플로우 관리를 코드 밖으로 꺼내야 함.
  - 단, 워크플로우 명이 변경되거나 기준점이 변경되어서는 안됨. DB Update를 요구하게 되면 -> 히스토리성 테이블이 update 발생. 이부분 방지해야함

## 워크플로우 요청이력 및 상태관리
- 워크플로우 요청 이력을 관리한다.

- workflow_request_tb
```
workflow_request_id bigserial primary key,
workflow_name varchar(1024),
workflow_domain_type varchar(128),  -- 파일 || 폴더
request_payload jsonb, -- 워크플로우 동작 시 요구 정보
target_id bigint,
error_message text,
status varchar(128),
create_time timestamp,
create_user_id integer,
start_time timestamp,
end_time timestamp,
```

- 변경점 (2026.06.21) request_payload 추가
  - 기존 @Async 메서드체이닝으로 워크플로우를 처리할때는 메모리에 모든 데이터가 있었으므로 문제가 없었음
  - UnitTask / Workflow로 실행주체를 나누고 UnitTaskExecutor를 만들어서 각 유닛태스크를 Proxy 패턴으로 호출하게 된 시점에서
    workflowId만 인자로 넘겨서 워크플로우를 호출하다 보니, 컨텍스트정보가 모두 소실됨.
    - 특히, copy_storage unit task 실행 시, file 과 fileTemp를 연결시킬 고리가 없었음.
  - 이를 해결하기 위해, request_payload를 생성, workflow 삽입 시에 unitTask가 필요로 할 모든 정보를 삽입해두는 방향으로 처리

## 유닛 태스크 실행 이력 및 상태관리
- 유닛태스크 실행 이력을 관리한다.
- unit_task_request_tb
```
unit_task_request_id bigserial primary key,
workflow_request_id bigint, -- ref : workflow_request_tb
unit_task_name varchar(1024),
task_order int, -- 해당 유닛태스크가 워크플로우 내 몇번째 작업이었는지. (유지보수관리용 : 어디쯤 실패한건지 보기 위해 코드를 까보지 않도록 + 유닛 동일 워크플로우 내 재사용 관리회도록)
workflow_domain_type varchar(128),  -- 파일 || 폴더 <- UnitTask는 DB만 보고도 어느 도메인에 대한 작업이었는가 가 추적이 되길 바래서 넣음
target_id bigint,
error_message text,
status varchar(128), -- succeed, failed_retriable, failed_final,
allow_retry smallint,
retry_count int,
create_time timestamp,
create_user_id integer,
start_time timestamp,
end_time timestamp,
```

## Workflow 도메인 흐름

- 워크플로우 요청 흐름은 아래와 같이 동작하도록 한다
  - FE에서 파일 등록 호출 시 -> 파라미터로 적용할 Workflow명 제출
    -> BE에서 파일 DB 등록 -> 이후, fileOrchestrationService 에서 workflowService.registerWorkflow() 호출 로 워크플로우 요청 등록
    -> fileOrchestrationService가 @Async workflowService.processWorkflow() 호출
    -> @Async workflowService.procesWorkflow()에서 While로 -> 직전 작업 조회, 금번 작업 조회, 재시도여부, 판별 후 UnitTask 시작시간 기록
    -> UnitTask 작업 호출 (UnitTask 호출은 @Async 아님에 유의. 이미 워크플로우 전체가 @Async로 빠져나왔으므로 재처리 불필요)
    -> Try/Catch 로 성공, 실패 여부 캐치 후 UnitTask 상태 DB Update
    -> While 빠져나왔으면 Workflow상태 업데이트, EventPublisher 활용 -> SSE 등으로 캐치해서 프론트에 노티파이
    
- 워크플로우 재요청이 접수될 경우, request row 재활용 하지 말고 새로 생성해서 처리한다.
  - 단, 워크플로우 재요청은 Phase3에서 개발 (중간값 보관 개발 필요)
- 위 방식의 특징:
  - workflow 수행 중 서버가 뻗어도, 중단점을 찾을 수 있다. 별도 개발은 필요하지만 문제 생긴 unittask부터 다시 시작할 수 있다.
  - @Async 를 사용할 때, 메모리 관리나 비동기관리전략 (큐 풀 시 거절, 큐 풀 시 호출메서드가 직접수행 등..)이 필요하다.
  - 동작중인 비동기 작업의 관제가 필요하다. -> 이경우 completableFuture를 통해 비동기체이닝, 완료/예외감지를 유용하게 해결 가능. 단, 전반적인 완료 기준은 DB로 잡는다.
  - 이 경우, UnitTask의 진척률을 할 수 없다. 그러나 엔진단위로 전환시에도 마찬가지로, 직접 몇퍼센트 수행했는지 Interrupt 후, DB 적재 방식을 개발하지 않으면
    어차피 알 수 없다. 이 내용은 phase3에서 고도화가 필요하다.

  - workflow, unittask request log를 로그성으로 유지하기 위해, 신규 요청/재요청 시 새로 만든다. 이경우 통계가 좀 고민스러워질 수 있다.
  - CompletableFuture 체이닝( AsyncOrchestrator 최초 조립, CompletableFuture 연속 실행) 이 아니라, 호출 -> 리턴 -> 재호출 구조로 가면 아래 이점이 있음
    - 중도 실패시 재시도 애매함을 제거 가능
    - Task 연결이 최초 체인 생성 시점에 고정되는것을 방지 가능
    - 서버 재시작 후 복구 쉬움 (재시작 시에 AsyncOrchestrator의 최초 조립에 의존적이지 않게되며, 실패 유닛태스크부터 재시작 가능)

## 워크플로우 종류

  - 파일 등록 workflow
    - 이 workflow 는 현재 설계상 removeTempFile UnitTask가 가장 마지막에 배치되어야 함.

## Workflow / UnitTask 상태값 관리

workflow_request.status

WAITING             : DB에 insert 되었으며, Worker Thread 할당 대기중
READY               : Worker Thread 진입 완료
PROCESSING          : Payload 파싱 이후 실제 동작 시작
SUCCEEDED           : 성공적으로 동작 완료
FAILED_RETRYABLE    : 실패했으나, 최대 재시도 횟수 미만으로 시도되었음. 재시도 가능
FAILED_FINAL        : 실패

unit_task_request.status

READY               : WorkflowProcessor에 의해 DB에 기록된 상태 
PROCESSING          : WorkflowProcessor에 의해 unitTaskExecutor.execute() 호출 
SUCCEEDED           : 처리 성공
FAILED_RETRYABLE    : 실패했으나, 최대 재시도 횟수 미만으로 시도되었음. 재시도 가능
FAILED_FINAL        : 실패

* Workflow 흐름 중, 선행작업 실패로 인해 후행작업을 시도조차 못하고 workflow가 종료되었을 경우 UnitTask를 기록하지 않음
  * UnitTaskReqeust 테이블을 특정 UnitTask가 실제로 실행 대상으로 선택되어, 한 번의 수행 시도가 발생했음을 기록하는 실행 이력으로 사용중이기 때문
  * 즉, "실행되었다" 가 "실패" 를 기록하는 테이블이므로, 실행실패와 미실행을 굳이 같이 저장할 필요가 없어보임.