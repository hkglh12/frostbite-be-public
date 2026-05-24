# Folder 도메인 설계

## 주 목적
- Folder는 사용자의 파일 집합 상 부모 역할을 수행한다.
- Folder의 접근은 ACL을 기반으로 동작되어야 한다.

## Folder의 주요 구조 및 핵심 원칙

```
엔트리폴더 (Entry folder)
ㄴ 루트폴더 (Root folder)
    ㄴ 서브폴더 (Sub folder)
    ㄴ 서브폴더
        ㄴ 서브폴더
        ...
ㄴ 루트폴더
ㄴ 루트폴더
```
- 엔트리 폴더는 팀 / 사용자 생성 시, 해당 오브젝트의 진입점 폴더로 동작하며, 엔트리폴더에는 파일을 업로드할 수 없다.
- 루트폴더는 엔트리폴더 바로 아래의 폴더로, 파일을 업로드할 수 있다.
- 루트폴더 이하 계층의 폴더는 서브폴더로 명칭한다.
- Entry Folder는 사용자 가입 또는 팀 생성 시 시스템이 자동 생성한다.
- Entry Folder는 외부 API로 직접 생성/수정/삭제하지 않는다.
- Root Folder는 Entry Folder의 직접 자식이다.
- Sub Folder는 Root Folder 이하에서 사용자가 생성하는 일반 폴더다.

- Entry, Root Folder API는 팀과 개인을 구분한다. 반면 subFolder부터는 구분하지 않는다.
  - Entry 폴더는 사용자 UUID/PK와 Team UUID를 기준으로 서비스 내에서만 조회해온다. 외부 API가 없음
    받게 되는 UUID Type이 각기 다른 도메인 컨텍스트를 가지게 됨.
  - Root 폴더 역시 조회, 생성 시에 사용자 UUID/PK 와 Team UUID를 기준으로 서비스 내에서만 조회한다.
  - 반면 Sub folder부터는 상위폴더의 UUID를 파라미터로 공통되게 받을 수 있다.

## 핵심 개념

- folderType은 폴더가 속한 스코프를 의미한다.
  - PRIVATE: 개인 폴더
  - TEAM: 팀 폴더

- accessPolicy는 접근 판단 기준을 의미한다.
  - USER: 사용자 ACL 기준으로 접근 판단
  - TEAM: 팀 ACL 기준으로 접근 판단

- resourceOwnerId와 createUserId는 서로 다른 의미를 가진다.
  - createUserId는 최초 생성자 메타데이터다.
  - resourceOwnerId는 현재 폴더 관리 권한을 가진 사용자다.
  - 일반적으로 createUserId = resourceOwnerId 이지만, 소유권 이전이 발생하면 달라질 수 있다.
  
## 도메인 객체 설명

### Folder.java

    - uuid : 외부에서의 식별자 역할을 수행한다.
    - folderId : 폴더 식별자
    - name : 폴더명
    - folderType : 폴더의 종류 (TEAM || PRIVATE)
    - parentFolderId : 부모 폴더 식별자
    
    - resourceScopeTeamId : 폴더 자원 종속 팀 (FolderType = TEAM)
    - resourceScopeUserId : 폴더 자원 종속 사용자 (FolderType = PRIVATE)
        - 두 컬럼은 상호 배타적이며, 정확히 하나만 NOT NULL 이어야 한다.
        - folderType = TEAM이면 resourceScopeTeamId만 값을 가진다.
        - folderType = PRIVATE이면 resourceScopeUserId만 값을 가진다.

    - resourceOwnerId : 폴더 자원 실제 관리 사용자 ID (소유권, 관리권)
    - accessPolicy : 폴더 자원의 접근 정책 (TEAM || USER)

    - folderIdPath : 폴더 ID Path
    - folderNamePath : 폴더 Name Path

    - renderSequence : 출력 순서
    - status : 폴더 자원 상태(삭제, 활성화) 여부
    
    * 하기 메타 6컬럼
    - createUserId
    - createTime
    - modifyUserId
    - modifyTime
    - deleteUserId
    - deleteTime

### 유효하지 않은 조합

- folderType = PRIVATE + resourceScopeTeamId NOT NULL
- folderType = TEAM + resourceScopeUserId NOT NULL
- folderType = PRIVATE + accessPolicy = TEAM
- resourceScopeTeamId와 resourceScopeUserId가 둘 다 NULL
- resourceScopeTeamId와 resourceScopeUserId가 둘 다 NOT NULL

### 폴더조회/API 정책

- 개인폴더
  - 개인폴더 트리 조회는 '이미 어떤 엔트리폴더를 조회할 지 알고 있' 으므로, 랜딩페이지 로드 시 루트폴더 리스트를 제공한다.
  - 개인폴더 트리 뷰에서 엔트리 폴더 클릭 시 -> 루트폴더 리스트를 제공한다.

- 팀 폴더
  - 팀 폴더 트리 조회는 '어떤 팀 엔트리폴더를 클릭할 지 모르'고 있으므로, 랜딩페이지 로드 시, 팀 각각을 '구분가능한 UI'로 그려낸다.
  - 팀 각각 '구분가능한 UI'를 클릭 시 -> 엔트리폴더를 클릭한것으로 간주하고 해당 폴더 하위 루트폴더를 제공한다.


### UI 렌더링 정책
- 전제
  - 좌측 폴더트리뷰 / 메인컨텐츠뷰로 나눈다.
  - 좌측 폴더트리뷰와 메인컨텐츠뷰를 API상, 비즈니스 흐름 상 분할한다.
  - Keyset pagination (cursor pagination) 을 적용하며, 좌측 폴더트리에서는 "더보기" UX를 제공한다.
  - Offset pagination 을 적용하여, 메인컨텐츠뷰에서는 "페이지Number 페이지네이션" 방식을 차용한다.
  - Entry Folder는 사용자에게 노출하지 않는다.
    - 즉, 외부 API는 Entry를 몰라도 되며, 모든 Root 조회는 내부적으로 Entry Folder 기준으로 수행된다
  
- 좌측 폴더 트리뷰는
  - "더보기"를 통해 무한스크롤 계통의 UX를 제공한다. 페이지네이션 정보를 굳이 FE+좌측트리에서 관리 안해도 될것같기때문.
  - 이에 더해, 트리 구조 특성상 traverse 가 자연스럽고 randomAccess가 오히려 어색함
  - "즉, 탐색 및 검색" 개념으로써 참고점 제공
- 메인컨텐츠뷰는
  - "페이지네이션"을 통해 분할 UX를 제공한다. 경력사항에서 배운 UX는 "몇 번째 페이지에 있는 폴더 참조" 라는 식으로 의사소통을 자주한 경험이 있기때문
  - 즉, "사용자가 예측 가능한 참고좌표 제공" 이 주 목적
  - 또한 메인컨텐츠뷰 API에서는 "LIMIT 개수에 따라서 파일 까지 함께" 주는 통합 API여야 한다.
    - 이 경우, 루트폴더조회와 서브폴더 조회가 API가 갈려있고, 루트폴더조회는 폴더만 제공하도록 설계되어있다.
    - 따라서 파일을 루트폴더 레벨 아래에 (부모를 EntryFolder로 가지는 파일) 생성 할 수 없도록 강제해야한다.
  
- 상기 이유를 근거로, 폴더 렌더링은 특정 기준점으로 "일정하게 정렬" 된 채로 조회되어야 한다.
  - Phase1 default : renderSequence asc, folder_id asc
    - "사용자에게 예측 가능한 폴더좌표값" 제공을 목적으로 offsetbase 페이지네이션을 차용했음
      만약 folder_id가 desc일경우, 예측할 수 없는 위치에 있게됨 (새로 폴더가 생성될수록 뒤로 밀림)
      따라서 folder_id로 asc로 지정
    
  - Phase2 : 정렬조건 변경 기능 추가
  - default null이며 (지정안함), postgresql은 null last이므로 "우선순위를 지정하는" rendersequence ASC시, NULL인경우 우선순위 지정폴더보다. 
    후순위에 출력됨

- 팀 폴더 렌더 시 주의사항
  - UI 자체에서 Team은 별도의 구분UI/UX로 출력될 예정
    - 위 전제로, Team Section 클릭 시, 내부적으로는 해당 팀의 Entry Folder를 기준으로 조회하되,
      UI에는 Entry Folder를 노출하지 않고 Root Folder List를 바로 제공한다.
    - 위 전제와는 별개로 개인폴더는 최초 진입 시 부터 root Folder List를 제공해야함.

- 개인/팀 section을 클릭하면 root 조회 API 호출, 해당 영역의 더보기를 클릭하면 root 조회 API 재호출
- 폴더트리 내에서 폴더클릭 || 더보기를 클릭하면 subfolder 조회 API를 호출해야한다.
