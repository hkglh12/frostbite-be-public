## Catch-up WBS
  - 약 3주간 프로젝트 진행이 일시 중단되었고, 재개 시점에 작업 맥락 복구를 위해 README.md로부터 WBS를 분리 작성
  - 분석 일자(260712) 기준 누락된 작업, 완성작업을 식별
  - 2026-09-19 기준 백엔드 및 `frostbite-fe` 실제 코드를 재점검하여 현황 갱신
  - 각 Phase별로 해야할 일들을 명시
    - 기능 위주로 명시
    - 도메인/Table 의 경우, 기능 변경에 따라 변경이 잦으므로 별도로 명시하지 않음 

### 상태 표기
  - `[X]`: 구현 완료
  - `[~]`: 핵심 흐름은 구현되었으나 권한 검증, 실패 처리 또는 테스트 보강 필요
  - `[ ]`: 미구현
  - `[Deferred]`: 후속 단계로 명시적으로 연기

## 고찰
  - 파일 등록 흐름 및 워크플로우 개념을 우선 확립하기 위해 Vertical Slice 방식으로 개발 접근 방식 전환
  - 2026-06 말부터 약 3주간 실무 프로젝트 긴급 대응, 야근 및 주말근무 증가로 인해 사이드프로젝트 진행이 일시 중단
  - 2026-07-12 기준 프로젝트를 재개하며, 중단 전후의 작업 맥락을 복구하기 위해 README의 작업 목록을 WBS로 분리
  - 중단 기간 이후 현재 상태를 재점검하고, 완료/진행/대기/Deferred 항목을 재분류
  - 현재 진행 중인 파일 등록 Workflow를 마무리한 뒤, User/Team/Folder/ACL/File의 누락 기능을 Horizontal Completion 방식으로 보강 예정
  

# Phase 별 작업 
## Phase1 RoadMap

- 개요 : ECM 전반 기능 작성
  - 사용자 API
    - [X] [26-04-18] 사용자 Create API
    - [X] [26-04-18] 사용자 비밀번호 암호화
    - [X] [26-05-04] 사용자 Self GET API
    - [X] [26-05-04] 사용자 Self Remove API 
    - [X] [26-08-09] 사용자 Self 비밀번호 변경 API
  - 로그인 기능
    - [X] [26-04-19] Spring Security Filter Chain 설정
    - [X] [26-04-23] AuthenticationFilter, 로그인 이벤트 리스닝, Security Role 등 로그인 기능 전반
  - 팀 API
    - [X] [26-05-04] 팀 Create API
    - [X] [26-05-04] 사용자 - 가입 팀 Listing API
    - [X] [26-05-04] 팀 상세정보 Read API
    - [X] [26-05-04] 팀 상세정보 Update API
    - [X] [26-09-19 확인] 팀 Delete API
  

  - 폴더 기능
    - 특이사항
      - ACL 도메인 도입으로 인한 Folder 도메인 리펙터링이 수반되었음 [~26-05-24]
    - [X] [26-05-04] 엔트리 폴더 개념 도입, 엔트리 폴더 생성 (개인/팀)
    - [X] [26-05-05] ROOT 폴더 생성 (개인/팀)
    - [X] [26-05-13] 무한스크롤방식 폴더 리스팅 (개인/팀)
    - [X] [26-05-13] 페이지네이션 베이스 폴더 리스팅 (개인/팀)
    - [X] [26-05-14] SUB 생성 API (공통)
    - [X] [26-05-14] SUB 폴더 조회 API (공통)
    - [X] [26-05-14] 폴더 Detail 정보 조회 (공통)
    - [X] [26-05-14] 폴더 메타정보 수정 API (공통)
    - [X] [26-05-14] 폴더 이동 API (공통)
    - [X] [26-05-14] 폴더 Soft Delete (공통)
    - [ ] [Deferred] 폴더 이름 변경 이력
    - [ ] [Deferred] 폴더/파일 정렬 조건 정리
    - [ ] [Deferred] 폴더 ZIP 다운로드
  

- ACL 및 권한제어
  - 특이사항
    - Permission Bitmask 개념 및 ACL 도메인 재설계 [~26-06-03]
    - 적용방안 변경 및 Validating 초안 별도작성 [~26-06-07]
    - 프로젝트 접근 방향이 수평개발로부터 수직개발로 변경됨. [26-06-14]
  - [X] [26-05-24] ACL 도메인 설계 및 폴더 ACL 적용 생성
  - [~] [26-09-19 확인] ACL Resolver 및 PermissionBitmask 판정 로직
    - 개인 폴더 소유자, 팀 소속, 관리자 우회, 사용자 DENY/ALLOW 판정 구현
    - 폴더 생성 시 기본 ACL 생성 및 하위 폴더 ACL 승계 구현
    - 폴더/파일 API 전반의 Resolver 적용은 미완료
  - [ ] ACL 관리 API (폴더별 ACL 조회/등록/수정/삭제)
    - Controller endpoint는 존재하나 현재 응답만 반환하는 stub 상태
  
  - 파일 CRUD
    - 특이사항
      - 최초 설계 단계에서 파일에 대한 권한을 폴더로부터 승계받는 개념으로 설계함. 이에따라 ACL 도메인 설계가 선행되었음
      - 프로젝트 접근방안이 수평개발로부터 수직개발로 변경됨에 따라 파일 업로드 개발 시작
      - 파일 도메인 설계 [26-06-14]
      - 워크플로우/유닛태스크 개념 도입 및 설계[26-06-20]
    - [X] [26-06-14] 임시파일 업로드
    - [X] [26-09-19 확인] 파일 등록 API
      - 이 시점, Workflow/UnitTask 개념 도입
    - [X] [26-06-28] 비동기 실행환경 적용
    - [X] [26-06-28] 비문 암호화를 위한 암호키 관리 정책 수립
      - [X] [26-06-28] 시크릿 분리 및 로컬 실행 가이드 정리 
      - 특이사항 : 해당 시점 2026-06-28 이후 프로젝트 일시 중단
    - [X] [26-09-19 확인] Workflow를 통한 파일 업로드
      - [X] 임시파일 업로드
      - [X] 파일 메타 등록
      - [X] Workflow Request 생성
      - [X] UnitTask
        - [X] @Deprecated File Copy UnitTask 대체
        - [X] [2026-07-18] Stream 활용, 임시파일로부터 암호화 + 스토리지 경로로 이동
          - [X] [2026-07-24] Happy path test 완료
        - [X] 임시파일 삭제 UnitTask
      - [X] Workflow / UnitTask Success 전환 및 File status Active 전환
    - [ ] [] 파일 업로드 시 폴더 ACL 권한 검증 제어
      - [ ] 임시파일 등록 전 논리 폴더 ACL권한 제어
      - [ ] 파일 등록 시 폴더 ACL 권한 제어
    - [~] [26-09-19 확인] 파일 다운로드 및 복호화 응답
      - 다운로드 동작 구현 완료, 다운로드 권한 검증 미완료
    - [~] [26-09-19 확인] 파일 수정
      - 파일명 수정 및 상위 폴더 이동 구현 완료, 권한 검증 미완료
    - [~] [26-09-19 확인] 파일 메타 조회
      - 메타 조회 구현 완료, 권한 검증 미완료
    - [~] [26-09-19 확인] 파일 Soft Delete
      - 삭제 처리 구현 완료, 권한 검증 미완료
    - [ ] [] 파일 버전관리
    - [ ] SSE/EVENT PUBLISHER를 통한 업로드 완료 안내
    - [ ] [] 파일 hard delete 스케줄러

  - Workflow/UnitTask
    - 특이사항
      - File로 부터 파생된 시스템 전반을 관통하는 동작방식
      - PHASE 1에서는 재시도 기능 제외
    - [X] [26-06-21] WorkflowRequest / UnitTaskRequest 구조
    - [X] [26-06-21] Storage copy UnitTask
    - [X] [26-06-28] commit 이후 async workflow 실행
    - [X] [26-09-19 확인] 임시파일 삭제 UnitTask 구현체 추가
    - [~] [26-09-19 확인] UnitTaskRequest 제어 보강
      - [X] 각 UnitTask 동작 실패 시 실패 상태 기록 및 보상 처리
      - [ ] 재시도 정책 및 임시파일 삭제 실패 정책 확정
    - [X] [26-07-24] 파일 암호화 Happy path 검증
    - [X] [26-09-19 확인] AES-256-GCM 파일 전문 암호화 및 다운로드 시 복호화

  - 공유 기능
    - [ ] [] URL 기반 공유 기능 개발
    - [ ] 공유받은 폴더/파일 조회 메뉴
    - [ ] 팀 기반 폴더/파일 공유
    - [ ] 사용자 기반 폴더/파일  공유
    - 

## 식별된 PHASE2 기능
  - Preview (미리보기) 및 ELS 동기화 기능 
    - 미리보기 -> 정책 수립 필요 (전문 암호화한것을 복호화해두면 의미없음. 어떻게 미리보기 할 것 인지)
    - ELS 동기화 -> folder type과 연계해서 전문동기화, 미리보기허용, 모두 불허 정책 유지한채로 개발
    - Streaming 처리
  - 관리자 통계
  - Socket 기반 채팅 / 코멘트
  - notion 형 Knowledge Base
  - 감사로그 추가

## Deferred 식별 (26-07-12 기준)

- 회원가입
  
  - [ ] [Deferred] 사용자 Self 정보 수정 (Update)
  - [ ] [Deferred] 테스트 코드 작성 for User API
    - Deferred 사유 : TestCode 학습을 06월 후반부에 시작했음. 추후 Phase1 후반부에 전체 테스크 코드 커버리지 100% 달성 예정

- 로그인 기능
  - [ ] [Deferred] 사용자 계정 세션 JWT 베이스 전환

- 팀 API
  - [ ] [Deferred] 팀 수정 권한 감사 기능
  - [ ] [Deferred] 팀 초대 기능 (JWT)
    - Deferred 사유 : JWT 기능 필요, Redis 연결 필요. 추후 Session JWT 전환 시 한번에 처리 예정
  - [ ] [Deferred] 팀 가입 기능 (JWT)
    - Deferred 사유 : JWT 기능 필요, Redis 연결 필요. 추후 Session JWT 전환 시 한번에 처리 예정
  - [ ] [Deferred] 팀 소속원 리스팅 기능
  - [ ] [Deferred] 팀 소속원 상세 조회 기능
  - [ ] [Deferred] 팀 강퇴 기능
  - [ ] [Deferred] 팀 역할 승격 기능
  - [ ] [Deferred] 팀 자진 탈퇴 기능
  - [ ] [Deferred] 팀 테스트 코드 작성
    - Deferred 사유 : TestCode 학습을 06월 후반부에 시작했음. 추후 Phase1 후반부에 전체 테스크 코드 커버리지 100% 달성 예정

- 폴더 기능
  - [ ] [Deferred] 폴더 조회/생성/수정/Move 시 ACL 제어
    - Deferred 사유 : ACL 기능이 완성되지 않음
    - 자동 생성 단계에서만 폴더 ACL 적용이 적용됨
  - [X] [26-09-19 확인] 폴더 엔트리 (파일 및 폴더) 조회 기능
  - [~] [26-09-19 확인] 폴더 전반 테스트 코드
    - 주요 Service 통합 테스트가 존재하며 ACL/실패/경계 케이스 보강 필요
    
- 파일 기능
  - [~] [26-09-19 확인] 파일 전반 테스트 코드
    - 암복호화 및 Controller 통합 테스트가 존재하며 권한/실패 케이스 보강 필요
  - [~] 업로드 테스트
    - [X] 파일 등록 성공 케이스
    - [ ] 파일 등록 실패 케이스
    - [ ] 파일 등록 권한 실패 케이스
    - [X] 파일 등록 Workflow Happy path
- ACL 기능
  - [~] [26-09-19 확인] ACL 테스트 코드
    - PermissionBitMask, ACL Service, AccessControlResolver 테스트 존재
    - 실제 Folder/File API에 ACL을 연결한 통합 테스트는 미완료

# Frontend 연동 현황 (`frostbite-fe`, 2026-09-19 확인)

- 공통 API 환경
  - [X] Backend base URL `http://localhost:9410` 연결
  - [X] Session cookie 전송 (`credentials: include`)
  - [X] Cookie 기반 CSRF 토큰 헤더 처리
  - [X] 401 공통 세션 만료 처리
- 인증/사용자
  - [X] 로그인, 회원가입, 내 정보 조회
  - [X] 비밀번호 변경, 회원 탈퇴
  - [~] 로그아웃 API 함수는 구현되어 있으나 Main 화면 로그아웃은 FE 상태만 제거함
- 팀
  - [X] 팀 목록, 상세, 생성, 수정, 삭제 연결
  - [ ] 팀 초대, 가입, 소속원 및 역할 관리
- 폴더
  - [X] 개인/팀 폴더 트리 및 폴더 엔트리 조회
  - [X] ROOT/SUB 폴더 생성
  - [X] 폴더 상세, 이름 변경, 이동, 삭제
- 파일
  - [X] 다중 파일 임시 업로드 후 파일 등록 Workflow 요청
  - [X] 파일 메타 조회, 이름 변경, 이동, 삭제
  - [X] 파일 다운로드 연결
  - [ ] 업로드 Workflow 진행 상태 및 완료 알림 UI (SSE 미연결)
- FE 후속 보강
  - [ ] Main 화면 로그아웃 시 `/v1/logout` 호출
  - [ ] ACL 관리 UI
  - [ ] 파일 버전관리, 공유, 미리보기/검색 UI

# 2026-09-19 기준 우선 잔여 작업

1. Folder/File 모든 조회 및 변경 API에 ACL Resolver와 PermissionBitmask 검증 적용
2. ACL 관리 API stub 구현 및 ACL 통합 테스트 추가
3. 업로드/다운로드 실패 케이스와 Workflow 보상/재시도 정책 보강
4. 팀 초대/가입/멤버/역할 기능 구현
5. SSE 업로드 완료 알림, 파일 버전관리, 공유, hard delete 스케줄러 구현
6. FE 로그아웃 API 연결 및 신규 백엔드 기능에 맞춘 UI 연결

## 검증 메모 (2026-09-19)

- 백엔드 테스트 실행 시 PostgreSQL `localhost:9413` 연결 거부로 Spring Context 로딩에 실패함
- 따라서 현재 테스트 실패는 assertion 실패가 아니라 로컬 테스트 DB 미기동에 따른 환경 실패로 분류함
