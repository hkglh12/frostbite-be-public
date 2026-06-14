# File 도메인 설계

## 주 목적
- 파일은 버전관리가 되어야 한다.
- 파일은 미리보기가 될 수 있어야 하며, 이는 폴더의 정책을 따른다.
  - 단, 폴더의 FolderPreviewPolicy에 따라 분기
- 파일은 검색엔진과 동기화 될 수 있어야 한다.
  - 단, 폴더의 FolderPreviewPolicy에 따라 분기
- 파일의 리스팅/조회/다운로드/수정 권한은 원칙적으로 폴더 ACL을 따라간다
  - 일부 파일은 별도의 명시적 파일 ACL을 통해 제어할 수 있다.
- 동일 폴더경로, 동일 파일 명이면 파일 버전 변경으로 간주한다.
- 물리적 저장 storage 경로는 언제든지 바뀔 수 있다고 가정하고 개발한다.

## Phase1 단계 목표
- 파일의 1kb 암호화 저장 / 복호화 조회
  - 파일 선두 1KB 암호화는 magic header를 훼손하므로,
    서버 복호화 경로 외에서는 파일 타입 식별이 불가능해진다.
  - 추후, Phase2 혹은 Phase1.5에서 전문암호화를 구현하고 encV2로 관리한다. 기존의 1KB 암호화는 encV1로 지정하고, 추후 마이그레이션한다.
- 파일의 Human-expectable 하지 않은 경로에 저장 (uuid 경로 저장)
- 파일의 메타정보 DB 관리
- Async를 사용한 tomcat request 스레드와의 분리
- Async의 적절한 정책을 통해 서버 폭주 Async 방지
    - 구조 고민해볼것
      - DB에 요청 저장, 별도 스케쥴러를 만들어서 스케쥴 동기방식 파일 처리?
      - 톰캣 스레드가 열릴 떄마다 async 정책대로 async 스레드 생성?
- 파일의 버전관리
  - 동일 부모폴더 pk 소속으로 동일 파일명이 등록되면 (equals()) 덮어쓰기로 간주한다.
  - 이때, file_tb의 해당 row의 현재 값을 모두 file_tb_ver로 이동시키고, 현재 업로드된 파일을 file_tb에 저장한다.
  - 이를 통해, file_tb에 있다면 -> 현재 버전, file_tb_ver 에 있다면 버전관리된 파일로 간주한다.
- 물리적 저장 storage는 언제든지 바뀔 수 있다고 가정하고 개발한다.

## Phase1 이후 고도화 개발 건
- ELS 동기화
- 파일 미리보기
  - 파일 유형별 분기 처리 필요
- ELS 전역검색

* 상기 고도화 개발 건에 의해 폴더 스키마 변경이 발생할 수 있다.
  * FolderPreviewPolicy.ENUM (ALLOW, DENY, SEARCH_META_ONLY)
  * ALLOW -> 프리뷰허용, DENY -> 프리뷰 및 ELS 동기화거부, SEARCH_META_ONLY -> 메타동기화만 허용
* 상기 고도화 개발 건 개발 시, file_workflow_status_tb 테이블 및 엔티티 변경 및 파일 미리보기 테이블 신설이 필요하다.

## 파일 저장 구조
- 파일은 RandomUUID 를 활용하여 저장한다.
  - UUID의 - 를 기준으로 split, 마지막 element를 제외한 uuid 문자열을 폴더경로로, 전체문자열 + 확장자로 파일명을 정한다
- 파일 메타정보 저장 중 물리경로 저장 시, 파일명과 경로, 마운트path를 별도로 관리한다.

## File Entity 군 설계

### File Entity 설계

- file_tb
```
file_id bigserial primary key,
folder_id  bigint,
file_uuid UUID,
original_file_name varchar(1024),
file_size bigint,
file_extension varchar(32),
mime_type varchar(256),
storage_mount_point_id int,
storage_file_path varchar(1024),
storage_file_name varchar(1024),
checksum_sha256 varchar(1024),
create_user_id int,
create_time timestamp,
modify_user_id int,
modify_time timestamp,
delete_user_id int,
delete_time timestamp,
status int
```
- 이때 status는 FileStatus.ENUM(ACTIVE, SOFTDELETED) 으로 표현한다.
- 이떄 파일이 영구삭제 되었다면, file_tb_del 테이블로 이동되어야 한다.

### file_tb_del

```
file_id bigserial primary key,
original_file_name varchar(1024),
file_size bigint,
file_extension varchar(32),
mime_type varchar(256),
folder_id  bigint,
storage_mount_point_id int,
storage_file_path varchar(1024),
storage_file_name varchar(1024),
checksum_sha256 varchar(1024),
create_user_id int,
create_time timestamp,
modify_user_id int,
modify_time timestamp,
delete_user_id int,
delete_time timestamp,
status int
```
- DEL 정보는 Phase2에서 구현

### File Status 1:1 entity 설계
- 파일 등록 및 후속처리는 Async 혹은 스케쥴 단위로 수행되어야 한다. 
- 파일 상태 정보와 파일 엔티티 메타정보는 서로 다른 상태값을 관리한다는 시각으로 바라보고, 파일 상태 정보를 별도 테이블로 관리한다.

- file_workflow_status_tb
```
file_id bigint primary key,
temp_file_name varchar(2048),
file_regist_start_time timestamp,
file_regist_status int,
file_regist_progress int,
file_regist_allow_retry int,
file_regist_complete_time timestamp,
message jsonb
```
- file_workflow_status_tb 는 file_tb 와 1:1 매핑, Foreign primary key.
- 파일 후속 처리 상태값이 추가되어야 할 경우 이 파일만 변경하면 되고, 메타정보 전체 출력시 view로 출력한다.
- *_status는 원칙적으로 (실행전 : 0, 진행중 : 1, 완료 : 2, 최초시도실패 : 3, 재시도실패 : 4) 을 가진다.
  - file_*_status.ENUM
    - (READY, 실행전, 0), (PROCESSING, 진행중, 1), (SUCCESS, 완료, 2), (FAILED_RETRYABLE, 최초시도실패, 3), (FAILED_FINAL, 재시도실패, 4)
- *_progress 는 퍼센티지 저장을 고려하나, 물리적으로 퍼센티지 처리가 불가능하다면 0 -> 100 으로 한번에 점프도 허용한다.
- temp_file_name은 "application이 관리할 임시 경로 에 존재하는 파일명" 을 대전제로 함. 따라서 file UUID 정보를 저장한다.
  - 단, 파일 등록이 끝나는 시점에서 file_tb에는 경로정보가 저장이 되어야 한다.
### File version entity 설계

- file_ver_tb
```
file_ver_id bigserial primary key,
file_id bigint,
original_file_name varchar(1024),
file_size bigint,
file_extension varchar(32),
mime_type varchar(256),
folder_id  bigint,
storage_mount_point_id int,
storage_file_path varchar(1024),
storage_file_name varchar(1024),
checksum_sha256 varchar(1024),
version_number int,
create_user_id int,
create_time timestamp,
modify_user_id int,
modify_time timestamp,
delete_user_id int,
delete_time timestamp,
status int
```
- 동일 fileId 가 여러개 있을 수 있으니, file_id PK 제약을 풀고 file_ver_id를 생성, PK로 간주한다.
* file_tb와의 관계
  - file_tb는 현재 버전 스냅샷 테이블
  - file_tb_ver 는 이전 버전 스냅샷 테이블
  - 두 테이블의 컬럼 중복은 버전이력 보존과 현재 파일 조회 최적화 + 삭제 시점 메타정보 보관 및 조회 효율 증대를 위해 의도적 비정규화를 유도
    
### storage mountpoint 테이블 (file_tb 파생)
- 파일 저장 물리 경로의 마운트포인트는 언제든지 바뀔 수 있다고 가정한다.
- 이에따라 마운트포인트가 pk로 관리되어야 한다

- storage_mount_point_tb
```
storage_mount_point_id bigserial primary key,
storage_name varchar(1024),
storage_end_point varchar(1024),
storage_type varchar(256),
storage_extension_info jsonb,
description varchar(1024),
```

- 사용자가 직접 수정 불가능한 엔지니어 레벨 테이블이므로 6메타 정보 (생성/수정/삭제 시각/유저명) 컬럼은 만들지 않는다.

### fileACL

- 파일은 기본적으로 폴더의 권한을 따라간다. 폴더에 권한이 있다면 파일 역시 조작 가능.
- 단, 아래의 명시적 케이스에 대응하기 위해 fileACL을 별도로 관리한다.
  - 나 빼고 모든 팀원 건드릴 수 없음 -> SUBJECT_TYPE.ALLOW, SUBJECT_USER_ID = ME
  - 특정 인원 뺴고 건드릴 수 있음 -> [SUBJECT_TYPE.DENY, SUBJECT_USER_ID = SOMEONE 세트, ...]
  - 이 경우, DENY가 우선한다. 따라서, DENY와 ALLOW에 동시에 한 사용자가 들어가는것을 비즈니스 로직에서 막아야 한다.
  - 조회 시, SUBJECT_TYPE.ALLOW 가 있는데, SUBJECT_USER_ID = ME 가 아니거나, SUBJECT_TYPE.DENY, SUBJECT_USER_ID = ME 가 있다면
    조회를 거절해야 한다.

- 이때, Phase1 기준 하나의 팀폴더는 하나의 팀과 매핑된다. 그 폴더 관리 권한이 있는 팀이 거절당하는것은 어불성설. SUBJECT 타입은 USER 고정.
- SUBJECT_TYPE, EFFECT_TYPE은 폴더 table에서 사용하는 .java 파일 공동사용
- EFFECT_TYPE.DENY 는 "무조건적 거부" 다. 즉, Permission 검사가 아니라, 아예 접근을 방어한다.
- 단, REL_USER_TEAM_TB 의 role이 ^MANAGER (MANAGER, LEADER) 이상일 경우 FolderACL과 동일하게 ACL을 무시하고 동작한다.
  - UX적 인식오류 가능성 있음. 비즈니스 로직에서 지정 자체를 막아야 할 수도 있다.


- file_acl_tb
```
file_acl_id bigserial primary key,
subject_type smallint,
effect_type smallint,
subject_user_id integer,
file_id bigint,
create_user_id int,
create_time timestamp,
modify_user_id int,
modify_time timestamp,
delete_user_id int,
delete_time timestamp,
status int

```
- file_acl은 folder_acl과 별개로 " 이 파일을 읽을 수 있게 해준다 / 읽을 수 없게 한다 " 관리 성격이 강하다.
- 따라서, permissionBit를 별도로 관리하지 않는다.

## 비즈니스 흐름 처리

### 파일 업로드 흐름

- 대 전제로, 비즈니스 내에서 파일 흐름을 처리하기 전까지, 서버에 파일이 등록되는 과정은 아래로 모두 동일하다.

  1. 사용자 파일 업로드 -> HTTP multipart 업로드 (S3Object는 일단무시), 서버 임시경로에 저장
    - 임시 경로 저장 시 UUID를 생성, 해당 임시파일의 파일명으로 저장한다.
    - java.io.tempdir (multipart 임시경로) 로 부터 어플리케이션이 관리할 경로로 transferTo()까지 호출한다. (multipart request 종료시 cleanup 방지)
    - 해당 시점에, 생성된 UUID, 추출한 fileName, fileSize, extension을 임시파일 테이블에 저장하고, FE로 UUID만 리턴한다.
  2. 사용자는 임시 파일 등록 API에서 리턴받은 UUID로 파일 등록 API를 호출한다.
     - 이 시점에서 임시파일 테이블로부터 "파일사이즈, 원본파일명, extension" 만을 추출해 file_tb로 옮긴다.
        (서버 내 파일경로를 의미하는 storage_file_name, storage_file_path는 레지스트 워크플로우에서 변경, mime_type 등록 역시 레지스트 워크플로우에서 추출 후 등록
        (http 요청 내 값을 신뢰하기보단 파일에서 직접 추출한 값을 신뢰하기로 결정))
     - file_workflow_status_tb에 동일 fileId (1:1) 로 row 생성, 모든 status = 0, *time = null로 저장한다.
     - 이 시점에서 파일 업로드 tomcat Thread는 처리 흐름을 "요청완료" 로 종료하고 FE로 리턴한다.
  3. 파일 유형에 따라 추가 후처리 Workflow를 수행한다. (레지스트, 미리보기 생성, ELS 동기화 등)
     
     - 이후 어플리케이션은 @Async로 워크플로우를 호출한다.
       - 레지스트 워크플로우는 모든 파일 유형에 대해 동일하게 동작하도록 한다.
         - 여기서 레지스트 워크플로우는 "암호화 후 영속성 저장 경로로 이동" 을 의미한다.
       - 기타 workflow는 db에 선언해둔 워크플로우 json을 보고, 반복호출 후, 완료 시 SSE 등으로 FE에 완료 노티를 한다.
       *  추후 Phase2 고도화 방안으로 별도 worker node를 생성하고 Queuing 도 고려 해볼것
     - 모든 단계의 workflow 단위 작업에서, 완료될 경우 수행한 워크플로우의 상태정보를 각 테이블 (file_tb, file_workflow_status_tb) 에 갱신한다.
  

### 파일 업로드 공통 흐름 : 파일 업로드

- 고도화 건과 무관하게, 파일 등록 흐름은 아래의 절차를 공통적으로 따른다.
  - 이 내용은 파일 업로드 흐름#5 로부터 이어진다.

  1. file_tb 메타정보 조회 -> 임시 파일 ByteStream으로 오픈, byte[1024] -> 첫 배열 AES256 암호화 수행
  2. file_tb.storage_file_path + file_tb.storage_file_name + file_tb.file_extension 에 flush, 이후 byte[] 는 바로 flush
     - 암호화 한 첫 byte[] flush 시, 아래의 포맷값을 지킨채로 저장해야한다.
     - 포맷은 실 개발 직전, 실무 암호화 방식과 특징, 요구 포맷, 비트 표현방법, 바이트/비트 문자열변환 공부 하고 다시 짤것
  3. 처리 완료시, file_regist_status 관련 메타데이터를 업데이트한다.
  4. 후속 워크플로우를 처리한다.
  5. 워크플로우 처리의 종료는 임시파일의 삭제 단계로 마무리 한다.

### 임시파일 핸들링
- multipart file은 Spring으로 올라오면 자동으로 java.io.temp 경로에 저장된다.
  - 이 경우, Spring이 관리하는 임시 경로 성격의 파일이며, 해당 경로 내 파일들은 Multipart request 처리 종료시,
    multipartResolver / container에 의해 제거될 수 있음. 따라서, 별도 관리가 필요함
    - 정확히는 multipart -> multipartRequest로 파싱 (multipartResolver) -> request 처리 -> container/multipartResolver finally에서 cleanup
  - 그러니, 별도 경로로 저장해 관리할 필요가 있음. 그러나, 동일 파일 시스템 내에서는 파일 rename이 경로상 move로 간주되어 처리되므로
    비교적 낮은 cost로 관리 관할을 옮기려면 MultipartFile.transferTo 로 동일 파일시스템 내에 rename 하는게 가장 바람직


### 워크플로우 실패 및  종료

- 워크플로우 실패 시, catch에서 allow_retry=1, _status=3 로 바꾸고 jsonb에 java exception message를 넣어둔다.
- WAS 스케쥴을 통해, file_workflow_status_tb 에 allow_retry = 1 && _status=3 인것들을 찾아서, 재시도를 수행한다. 시작 시 allow_retry=0 으로 업데이트
- 재시도 실패시 *_status=4로  바꾸고, message(jsonb)에 재시도 실패 메시지를 추가해둔다.

- 수행 결과를 SSE가 체결되어있으면 FE에 리턴하고, 체결되있지 않다면 서버에서 메시지를 삼킨다.
- 사용자는 추후 메타정보 조회시에 확인이 가능하며, 사용자가 명시적으로 재시도를 요청할 수 있다.

### 파일 다운로드 흐름

- 고도화 건과 무관하게, 파일 다운로드 흐름은 아래 절차를 공통적으로 따른다

1. WAS URL(다운로드 URL) 호출
2. 권한감사
3. 허용/거부 판별
    - 허용 흐름
      a. 파일 magic정보, header 정보 조회 후 해당 암호화 블록 복호화 후 responseStream에 Flush
    - b. 나머지 byte를 responseStream에 flush

### 고도화 개발 고려 건

- 미리보기의 허용
    - FolderPreviewPolicy에 따라 미리보기 생성 여부와, 파일 확장자/포맷 별 별도 동작을 고려해야 한다.
    - 또한 미리보기 역시 권한감사를 타야하며, 미리보기 파일 생성시 일부 "파일 암호화 저장을 통한 원본 보호" 가 부분적으로 깨짐을 인정해야함
    - 미리보기 조회 시 권한감사 하기 위해서는 nginx의 auth_request / secure_link 공부하고 구현할것
- 검색엔진 동기화
  - 검색엔진 동기화시 파일이 속한 폴더의 메타정보와 파일의 메타정보를 함께 넘겨야 한다. 넘길 대상은 고도화시 고려할것.
  - 검색엔진 동기화시 ACCESSIBLE/DENIED 정보가 함꼐 넘어가야 한다.
      - WAS엣 필터링 하는건 몹시도 비효율적, 비권장. For문 순회해가면서 조회 권한을 필터링하기보단, 데이터 Retrieve 단계에서 걸러내서 가져오는게 훨씬 효율적이라 판단.
      - 대신 ACL PK 자체를 리스트로 넘기기 보단, JAVA에서 특정 키워드 형태로 입력한다.
        - 예시
          - ACCESSIBLE_SUBJECT (접근가능한 객체를 명시)
              - 사용자 PK7, 폴더가 개인폴더면 [ USER:7 ]
              - 사용자PK7, 팀PK 3, 폴더가 팀폴더면 [USER:7, TEAM:3] 
              - 사용자PK7, 팀PK3, 특정사용자만 접근허가라면 [USER:7]  
          - DENIED_SUBJECT (명시적 거부당한 객체를 명시) 
            - 개인폴더는 명시적거부 불가능 
            - 팀폴더, 사용자PK7, 거부당한사용자 PK3, 팀PK3 
              - ACCESSIBLE_SUBJECT에 [USER:7, TEAM:3] 있어야함 
              - DENIED_SUBJECT에 [USER:3]

## 구현 중 어려운 점 인식
  1. 임시파일 처리, 파일 도메인 비즈니스로직을 파일 컨트롤러 -> 파일서비스에서 동시에 처리하고자 함
  2. 파일 후속처리(워크플로우) 서비스를 별도로 열려고 함.
  - 1, 2에 의한 여파 : 워크플로우서비스와 파일서비스의 순환의존발생 (파일 -> 워크플로우 시작 호출 / 워크플로우 -> 파일 처리 상태 보고)
  - 1, 2에 의한 여파 2 : @Lazy 로 _Self 주입 발생 (multipart file 업로드 중 추출 파일 메타 저장 필요) <- 스프링 권장형태 아님, IDE 경고형태

  - 개선방안
    1. MultipartFile 처리를 DirectFileHandleComponent 이관한다.
       - FileService가 DirectFileHandleComponent 의존한다.
       - 진입점은 FileController → FileService → DirectFileHandleComponent 둔다.
       - DirectFileHandleComponent TempFileDraft를 반환한다.
       - 이를 통해 @Lazy _self 주입을 제거한다.
    2. FileController가 FileService, FileWorkflowService 각각 호출
       - 이 경우, 파일 처리 진입점이 단일진입점이 아니게 되나, 별도로 문제로 볼 이유가 없음.
       - 순환의존 제거 가능
    3. 추후 워크플로우 서비스도 DirectFileHandleComponent를 호출한다
      - 실제 저장 경로로 파일 이동등의 처리가 필요함.
  
    * 결론
      * FileController <- FileService <- DirectFileHandleComponent
      * FileController <- FileWorkflowService <- FileService 
      * FileWorkflowService <- DirectFileHandleComponent
  * 정리
    - Intellij IDE도, Spring official도 @Lazy를 통한 자기의존은 우회책으로 보는 경향이 강함
    - 메서드가 파편화 되더라도 분리를 하거나, "비즈니스 유스케이스별"로 (외부통신 || 비동기 || ...) 뭉쳐서 새로운 서비스 객체를 만드는 것을 권고함.

## 구현 중 어려움 점 인식 2
    - 파일 업로드를 물리파일 업로드 || API 호출을 통한 등록 으로 분리하고자 함
    - BE가 임시파일을 전혀 관리하지 않는 방향으로 설계해, FE가 임시파일에서 추출한 데이터를 다시 그대로 던져야 하는 위화감 발생
    - 따라서, temp_file_tb 를 신규로 열고 해당 테이블을 활용하는 방향으로 개선
    - 위 변경 사항에 따라, file_workflow_status_tb 내 temp_file_name 제거, temp_file_tb에서 관리하도록 전환
