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
  - UUID의 - 를 기준으로 split, 마지막 문자열 + 확장자로 파일명을 정한다
  - 단, DB에 메타정보를 저장할 때에는 file_name은 마지막문자열만, 확장자는 file_extension에 저장한다.
  - Splited UUID[] 의 마지막 문자열을 제외한 UUID 문자열이 폴더경로가 된다.
- 파일 메타정보 저장 중 물리경로 저장 시, 파일명과 경로, 마운트path를 별도로 관리한다.

## File Entity 군 설계

### File Entity 설계

- file_tb
```
file_id bigserial primary key,
original_file_name varchar(1024),
file_size bigint,
file_extension varchar(32),
mime_type varchar(256),
folder_id  bigint,
storage_mount_point_pk int,
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
file_id bigint primary key,
original_file_name varchar(1024),
file_size bigint,
file_extension varchar(32),
mime_type varchar(256),
folder_id  bigint,
storage_mount_point_pk int,
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

### File Status 1:1 entity 설계
- 파일 등록 및 후속처리는 Async 혹은 스케쥴 단위로 수행되어야 한다. 
- 파일 상태 정보와 파일 엔티티 메타정보는 서로 다른 상태값을 관리한다는 시각으로 바라보고, 파일 상태 정보를 별도 테이블로 관리한다.

- file_workflow_status_tb
```
file_id bigint primary key,
file_regist_start_time timestamp,
file_regist_temp_path varchar(2048),
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

### File version entity 설계

- file_ver_tb
```
file_ver_id bigserial,
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

- storage_info_tb
```
storage_mount_point_id bigserial,
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

- 이때, Phase1 기준 하나의 팀폴더는 하나의 팀과 매핑된다. 그 폴더 관리 권한이 있는 팀이 거절당하는것은 어불성설. 따라서 EFFECT_TYPE.USER 고정
- SUBJECT_TYPE, EFFECT_TYPE은 폴더 table에서 사용하는 .java 파일 공동사용
- EFFECT_TYPE.DENY 는 "무조건적 거부" 다. 즉, Permission 검사가 아니라, 아예 접근을 방어한다.
- 단, REL_USER_TEAM_TB 의 role이 ^MANAGER (MANAGER, LEADER) 이상일 경우 FolderACL과 동일하게 ACL을 무시하고 동작한다.
  - UX적 인식오류 가능성 있음. 비즈니스 로직에서 지정 자체를 막아야 할 수도 있다.


- file_acl_tb
```
file_acl_id bigserial primary key,
file_id bigint,
subject_type smallint,
effect_type smallint,
subject_user_id integer,
create_user_id int,
create_time timestamp,
modify_user_id int,
modify_time timestamp,
delete_user_id int,
delete_time timestamp,
status int

```

## 비즈니스 흐름 처리

### 파일 업로드 흐름

- 대 전제로, 비즈니스 내에서 파일 흐름을 처리하기 전까지, 서버에 파일이 등록되는 과정은 아래로 모두 동일하다.

  1. 사용자 파일 업로드 -> HTTP multipart 업로드 (S3Object는 일단무시), 서버 임시경로에 저장
  2. Random UUID 생성, -로 split, 가장 마지막 문자열을 파일명으로, 그 외 문자열을 폴더경로로 지정
  3. file_tb에 파일 정보 등록, file_workflow_status_tb 에 해당 fileId로 row 생성, 모든 status = 0, *_time = null, 파일 임시경로 file_workflow_status_tb 에 저장
  4. 후속 워크플로우 처리를 시작하기 위해 아래 둘중 하나의 방법을 사용한다. 
     - 단, 어느 방법을 선택해도 적절한 방안으로 이 시점에서 파일 업로드 tomcat Thread는 처리 흐름을 "요청완료" 로 종료하고 FE로 리턴한다.
        a. DB 등록 정보를 기반으로 schedule, 동기 방식으로 워크플로우 처리
        b. @Async 활용, 비동기로 처리 + SSE를 체결해 상태정보를 업데이트해주면서 처리한다. 이경우 적절한 Async Policy 선택 및 적용이 필요하다.
        c. 별도의 workflow 처리 용 WAS를 띄우고, 해당 WAS 호출을 Async로 처리한다.
        * 어떤 방안을 선택할지는 실제 개발 단계에서 결정 예정, 결정시 나머지 선택지를 지울 예정

  5. 이후, 워크플로우를 모두 수행한 뒤 file_workflow_status 에 해당 정보를 업데이트 한다.

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
      - WAS엣 필터링 하는건 몹시도 비효율적, 비권장. DB가 제일 잘하는걸 왜 WAS에서 처리하려 하는가.
      - 대신 ACL PK 자체를 리스트로 넘기기 보단, JAVA에서 특정 키워드 형태로 입력한다.
        - 예시
          - ACCESSIBLE_SUBJECT (접근가능한 객체를 명시)
              - 사용자 PK7, 폴더가 개인폴더면 [ USER:7 ]
              - 사용자PK7, 팀PK 3, 폴더가 팀폴더면 [USER:7, TEAM:3] 
              - 사용자PK7, 팀PK3, 특정사용자만 접근허가라면 [USER:7]  
          - DENIED_SUbJECT (명시적 거부당한 객체를 명시) 
            - 개인폴더는 명시적거부 불가능 
            - 팀폴더, 사용자PK7, 거부당한사용자 PK3, 팀PK3 
              - ACCESSIBLE_SUBJECT에 [USER:7, TEAM:3] 있어야함 
              - DENIED_SUBJECT에 [USER:3]
