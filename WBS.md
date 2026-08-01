## Catch-up WBS
  - 약 3주간 프로젝트 진행이 일시 중단되었고, 재개 시점에 작업 맥락 복구를 위해 README.md로부터 WBS를 분리 작성
  - 분석 일자(260712) 기준 누락된 작업, 완성작업을 식별
  - 각 Phase별로 해야할 일들을 명시
    - 기능 위주로 명시
    - 도메인/Table 의 경우, 기능 변경에 따라 변경이 잦으므로 별도로 명시하지 않음 

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
  - 로그인 기능
    - [X] [26-04-19] Spring Security Filter Chain 설정
    - [X] [26-04-23] AuthenticationFilter, 로그인 이벤트 리스닝, Security Role 등 로그인 기능 전반
  - 팀 API
    - [X] [26-05-04] 팀 Create API
    - [X] [26-05-04] 사용자 - 가입 팀 Listing API
    - [X] [26-05-04] 팀 상세정보 Read API
    - [X] [26-05-04] 팀 상세정보 Update API
  

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
  - [ ] ACL 제어 (폴더별 접근권한 및 PermissionBitmask 제어)
  
  - 파일 CRUD
    - 특이사항
      - 최초 설계 단계에서 파일에 대한 권한을 폴더로부터 승계받는 개념으로 설계함. 이에따라 ACL 도메인 설계가 선행되었음
      - 프로젝트 접근방안이 수평개발로부터 수직개발로 변경됨에 따라 파일 업로드 개발 시작
      - 파일 도메인 설계 [26-06-14]
      - 워크플로우/유닛태스크 개념 도입 및 설계[26-06-20]
    - [X] [26-06-14] 임시파일 업로드
    - [~] [] 파일 등록
      - 이 시점, Workflow/UnitTask 개념 도입
    - [X] [26-06-28] 비동기 실행환경 적용
    - [X] [26-06-28] 비문 암호화를 위한 암호키 관리 정책 수립
      - [X] [26-06-28] 시크릿 분리 및 로컬 실행 가이드 정리 
      - 특이사항 : 해당 시점 2026-06-28 이후 프로젝트 일시 중단
    - [ ] [] Workflow를 통한 파일 업로드
      - [X] 임시파일 업로드
      - [ ] 파일 메타 등록
      - [X] Workflow Request 생성
      - [ ] UnitTask
        - [ ] @Deprecated File Copy UnitTask
        - [X] [2026-07-18] Stream 활용, 임시파일로부터 암호화 + 스토리지 경로로 이동
          - [X] [2026-07-24] Happy path test 완료
      - [X] Workflow / UnitTask Success 전환 및 File status Active 전환
    - [ ] [] 파일 업로드 시 폴더 ACL 권한 검증 제어
      - [ ] 임시파일 등록 전 논리 폴더 ACL권한 제어
      - [ ] 파일 등록 시 폴더 ACL 권한 제어
    - [ ] [] 파일 다운로드
    - [ ] [] 파일 수정
    - [ ] [] 파일 메타 조회
    - [ ] [] 파일 삭제
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
    - [~] [] 임시파일 삭제 UnitTask 구현체 추가
    - [ ] UnitTaskRequest 제어 보강
      - [ ] 각 UnitTask 동작 실패시 실패 기록 개발
    - [ ] [] 파일 암호화 (1kB)
    - [ ] [] 파일 전문 암호화

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
  - [ ] [Deferred] 폴더 엔트리 (파일 및 폴더) 조회 기능
  - [ ] [Deferred] 폴더 전반 테스트 코드
    - Deferred 사유 : TestCode 학습을 06월 후반부에 시작했음. 추후 Phase1 후반부에 전체 테스크 코드 커버리지 100% 달성 예정
    
- 파일 기능
  - [ ] [Deferred] 파일 전반 테스트 코드
    - Deferred 사유 : TestCode 학습을 06월 후반부에 시작했음. 추후 Phase1 후반부에 전체 테스크 코드 커버리지 100% 달성 예정
  - [ ] [Deferred] : 업로드 테스트
    - [ ] 파일 등록 실패 케이스
    - [ ] 파일 등록 권한 실패 케이스
    - [ ] 파일 등록 Workflow 
- ACL 기능
  - [ ] [Deferred] ACL 전반 테스트 코드
    - Deferred 사유 : TestCode 학습을 06월 후반부에 시작했음. 추후 Phase1 후반부에 전체 테스크 코드 커버리지 100% 달성 예정