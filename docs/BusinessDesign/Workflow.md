## Workflow , UnitTask 정의

- 작업의 흐름, 즉 단위 작업의 뭉치로 특정 순서를 지켜서 수행되어야 하는 작업 뭉치를 워크플로우라고 명칭한다.
- 워크플로우의 각각 단위 작업을 유닛태스크 (단위작업)이라 명칭한다.

## 개정
- 최초 workflow는 file에만 적용되는 개념이었음
  - 사고를 확장해, 폴더이동도 동기방식이 아니라 "요청" 후 "접근금지" 상태로 전환하고, 대용량 작업을 수행할 수 있겠다 는 판단이 들었음
- 이에 따라, workflow를 종속개념이 아닌 별도 도메인 엔티티로 격상
- 단, 폴더단위 워크플로우는 Phase3에 추가하고 Phase1에서는 파일을 대상으로 In Was Code 식으로 개발
  - Phase2에서는 Was code에서 분리해서 엔진방식 || none WEB JAR 방식 || Python 단위모듈 호출 방식으로 전환


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
target_domain varchar(128),  -- 파일 || 폴더
target_id bigint,
error_message text,
status smallint,
create_time timestamp,
create_user_id integer,
start_time timestamp,
end_time timestamp,
```

## 유닛 태스크 실행 이력 및 상태관리
- 유닛태스크 실행 이력을 관리한다.
- unit_task_request_tb
```
unit_task_request_id bigserial primary key,
workflow_request_id bigint, -- ref : workflow_request_tb
unit_task_name varchar(1024),
task_order int, -- 해당 유닛태스크가 워크플로우 내 몇번째 작업이었는지. (유지보수관리용 : 어디쯤 실패한건지 보기 위해 코드를 까보지 않도록 + 유닛 동일 워크플로우 내 재사용 관리회도록)
target_domain varchar(128),  -- 파일 || 폴더
target_id bigint,
error_message text,
status smallint, -- succeed, failed_retriable, failed_final,
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
    -> BE에서 파일 DB 등록 -> 이후, AsyncOrchestrator(가칭) 의 <CompletableFuture> orchestroWorkflow@Async 메서드 (가칭) 호출
    -> AsyncOrchestrator 워크플로우 설정 조회 -> workflow request 요청 삽입, 첫번째 UnitTask @Service의 processWorkflow()(가칭) 호출
    -> 각 UnitTask는 DB에 자기 자신 Unit Task 시작시간과 종료시간, 상태를 기록한다. 또한 유닛태스크는 파일전용 / 폴더전용이 구분될 수 있으므로,
        각 유닛태스크는 본인이 작업한 엔티티의 상태값 (예 : file_workflow_status_tb) 를 변경한다.
    -> UnitTask가 작업 완료로 return하면 CompletableFuture를 통해 콜백으로 AsyncOrchestrator가 리턴받는다 
    -> 다음 유닛태스크 작업을 조회한뒤 다시 unitTask를 호출한다. 단, 두가지 방식을 고려해 볼 수 있다.
        - 직전 작업이 성공했다면 -> 문제없이 다음 유닛태스크 호출
        - 직전 작업이 실패했다면 -> FAILED_RETRYABLE : 다시 해당 메서드 호출 || 혹은 지연호출?  / FAILED_FINAL : 해당 워크플로우 정지.
    -> 위 과정을 통해 워크플로우 내 호출할 UnitTask가 없다면, 워크플로우 종료로 선언한다.
    
- 워크플로우 재호청이 접수될 경우, request row 재활용 하지 말고 새로 생성해서 처리한다.
  
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