# ACL 도메인 설계

## 주 목적
- ACL은 접근 주체가 폴더와 파일에 대한 접근 가능 여부와 수행 가능 행위를 판단하기 위한 모델임


## 핵심 원칙
    - ACL은 실제 접근 허용/거부 + 수행가능 동작의 범위를 의미한다.
    - 폴더에 대한 접근은 Implicitly Deny, Explicitly ALLOW를 전제로 한다.
    - 파일에 대한 접근은 Implicitly Inherited from folder, Explicitly DENY를 적용한다.
      - 파일 접근은 기본적으로 폴더로부터 상속이며, deny만 명시됨. -> 도메인이 다르며, 작성 케이스가 분리되어있으므로 별도테이블 관리
      - 테이블은 분리되어있으나, 횡단 관심사로는 일치하므로 entity 객체 / 테이블구조 자체는 동일하게 유지

## 도메인 객체 설명

### FolderAcl
    - aclId : ACL 식별자
    - folderId : 이 ACL이 보호하려는 폴더 PK
    - effectType : 이 ACL이 명시하는 접근 허용 타입을 의미한다
        - ALLOW : 명시적 접근 허용
        - DENY : 명시적 접근 불허 (예시 : "팀 전체에 접근은 허용되지만, 특정 인원에게 허용하지 않음" )

    - subjectType : 이 ACL이 접근을 제어하려는 접근제어 주체 종류를 의미
        - TEAM : 이 ACL은 팀에게 적용되는 객체이다.
        - USER : 이 ACL은 사용자에게 적용되는 객체이다.
    - subjectTeamId
    - subjectUserId
        - subjectType이 TEAM -> temaId에 team PK를 입력하며, 팀의 행동을 제어한다.
        - subjectType이 USER -> userId에 User Pk를 입력하며, 사용자의 행동을 제어한다.
        - 두 컬럼은 정확히 하나만 NOT NULL이어야 한다.
    - permissionBitMask : 이 ACL이 허용하는 접근주체의 동작을 의미한다.
        - NONE(0) : Default 값 Guard용. 비즈니스에서 의미를 가지지 않음.
        - BROWSE(1): 폴더/파일 리스팅 가능
        - READ(2): 다운로드, 읽기, 폴더 진입 후 내용 조회
        - WRITE(4): 생성, 수정, 덮어쓰기, 삭제, 폴더/파일명 수정
        - CONTROL(8): 메타데이터 수정, 공유, ACL 제어
        - MANAGE(16): 폴더의 타 팀 소유권 이전

    - status : ACL 활성상태 여부 
    - createTime : 생성시각
    - modifyTime : 수정시각
    - deleteTime : 삭제시각
    - createUserId : 생성자 식별자
    - modifyUserId : 수정자 식별자
    - deleteUserId : 삭제자 식별자

### FileACL
- FileACL은 별도의 ALLOW를 가지지 않고 Folder ACL을 따른다.
- 특정 주체의 파일 접근 전체를 차단해야 할 경우 DENY ACL을 기록한다.
- File DENY는 해당 File Resource에 대한 직접 접근/행위를 차단한다.
  단, BROWSE는 File 단위 권한이 아니라 부모 Folder의 namespace 조회 권한이므로 File DENY의 영향을 받지 않는다.
  - Browse 는 폴더 엔트리 리스팅에 해당하는 퍼미션이므로 최소 "파일명, 파일사이즈, 확장자, 생성자, 생성일시" 를 조회할 수 있게된다.
- 또한, 파일에 DENY가 걸린 사람이 해당 파일이 소속된 폴더를 공유하면, 공유받은 사람도 해당 파일을 볼수 없어야 한다. 
  - 즉, 공유 행위로 인해 부여될 수 있는 권한은 공유자의 유효 접근범위를 초과할 수 없다.
  - ACL 복제가 아닌, share_tb -> reference acl 등으로 후속처리한다. (복제방식일경우 ACL 재전파 오버헤드가 발생한다.)
    - (추후 share domain 설계시 설계 및 구현)

### permissionBitmask 이용
    - permissionBitmask ENUM 내, 각 권한은 각각의 독립적인 비트값을 가진다.
    - DB 저장 시 permissionBitMask는 Integer 형으로 저장한다.
    - 각 행위 비트는 독립적으로 관리되며, 여러개의 권한을 동시에 가졌을 경우 누적값으로 DB에 기록해야 한다.
      - 이때, 용어를 "PermissionBitMask (각 비트) 와 PermissionBit Set (합산값 명칭) 으로 구분한다.
      - 7 ACL SET = Browse(1) + READ(2) + WRITE(4) / Browse Bitmask(1), Read bitmask(2), Write Bitmask(4) 
    - 비트 자체는 독립적으로 관리되나, 비즈니스 흐름 상 특정 동작을 위해서는 선결 행위가 필요하다.
      - 쓰기 => 브라우징 + 조회
      - 공유 => 브라우징 + 조회 + 메타데이터수정
      - 따라서, bitmask 단일을 저장하는게 아니라 bitmask set으로 현재 부여하려는 permissionBitmask 의 앞 값을 모두 합산한 
        permissionBitmaskSet으로 저장해야 한다.

### permissionBitMask 권한 판정
* 주의
  - 팀 역할 "HEAD" 와 "MANAGER" 는 PermissionBitMask, ACL SubjectType 평가를 우회한다 (영향받지 않는다).
  
- ACL의 `permissionBitMask`는 허용 권한을 포함한 누적 비트마스크 값으로 저장한다.
  - ACL의 비트마스크의 각 권한은 독립적으로 관리된다.
    - 예: 7 permissionbit Set은 BROWSE(1) + READ(2) + WRITE(4) = 7로 저장한다.
    - 예: 31 permissionbit set은 BROWSE(1) + READ(2) + WRITE(4) + CONTROL(8) + MANAGE(16) = 31로 저장한다.
  - 동일 사용자가 USER ACL과 TEAM ACL을 동시에 만족할 수 있으므로, ALLOW 판정 시에는 적용 가능한 ALLOW ACL 중 한개라도 퍼미션을 만족한다면 통과시킨다.
      - 예: `ALLOW USER:7, permissionBitMask = 7`
      - 예: `ALLOW TEAM:13, permissionBitMask = 31`
      - 이 경우 적용 가능한 ALLOW ACL 중 하나 이상이 요구 비트를 포함하므로 ALLOW
  - 단, `permissionBitMask = 0`은 비즈니스 로직에서 의미를 두지 않는다.
      - ALLOW ACL 생성 시 `NONE(0)`은 null 값을 방지하기 위해서만 사용한다.
      - ALLOW ACL의 PermissionBitMask DB 최종 상태는 최소 BROWSE(1) 이상이어야 한다.
      - DENY ACL에서 PermissionBitMask는  null로 관리한다.
  - 각 ACL에 대해 permission Bitmask 를 수정 (새로운 권한 추가 / 권한 회수) 하고자 한다면, 기존의 ACL을 update해야한다. 신규 Insert로 처리해서는 안된다.
    - 만약 접근명세 자체를 변경하고싶다면 ACL을 삭제해야한다.
  - 동일 Resource + 동일 Subject + 동일 EffectType의 ACTIVE ACL은 동시에 두 개 이상 존재할 수 없다.
  - 
### 사용방안
* 주의
  - 팀 역할 "HEAD" 와 "MANAGER" 는 해당 팀 소속의 폴더에 대해서 PermissionBitMask, ACL SubjectType 평가를 우회한다 (영향받지 않는다).

- 팀 폴더
  - Entry Folder
    - Team Entry Folder는 모든 사용자가 최소 3 Permission Set 이상을 가지도록 구성한다. 그 어느 누구라도 조회할 수 있어야 하기 떄문
      - 특정 사용자만 폴더를 만들수 있게하려면 3 Permission Set, 자유롭게 누구든 폴더를 만들수 있게하려면 7 Permission Set로 구성
        - 이 내용은 FE에서 안내메시지로 출력한다.
      - Default Permission Set 31
        - 조합 셋을 제시해줄것
          - 기본(31) : 누구나 폴더생성/메타데이터조회/공유가 가능합니다.
          - 중앙제어형(7) : 메타데이터 수정, 공유는 부서장 혹은 관리자가 직접 권한을 부여해야합니다. 일반적인 사용은 누구나 할 수 있습니다.
          - 완전관리형(3) : 폴더생성, 메타데이터수정, 공유는 부서장 혹은 관리자가 직접 권한을 부여해야합니다. 조회만 가능합니다.
  - Root folder / Subfolder
    - 폴더 생성자가 ACL 조합을 할 수 있도록 개발한다.
    - default 기본(31)
- 개인폴더
  - 전체 폴더에 대해 해당 사용자에게 ALLOW, 31 PermissionSet이 적용되어야만 한다. 예외없음
  
- DB 저장 시 규칙
  - subjectType[TEAM || USER #AclSubjectType.java] + effectType [ALLOW || DENY #aclEffectType.java]
  - subjectTeamId(subjectType == TEAM 일떄 !null) || subjectUserId (subjectType == USER 일때 !null)
- 해석 규칙
  - Folder의 folderType과 조합해서 생각해야한다.
    - folderType.TEAM + ACL => TEAM 폴더 ACL
    - folderType.PRIVATE + ACL => 개인폴더 ACL

- permissionBitmask 조회요청
  - 퍼미션 비트 자체는 독립적으로 구성되어있으나, 개념상 상위개념을 가진다 -> (공유를 위해서는 : 조회->읽을수있어야하고->메타정보쓰기가 필요 / 쓰기를 위해서는 -> 조회 -> 읽을수있어야함)
    따라서 API 요청 시, 단일 permissionBitmask 판정 요청으로 처리한다.

### 접근 허용 판정 우선순위
    1. 팀 폴더에서는 팀 내 역할 (TeamRole.MEMBER, TeamRole.MANAGER, TeamRole.HEAD) 중, HEAD와 MANAGER는 ACL effectType, PermissionBitmask 평가를 우회한다 (영향받지 않는다).
    2. Explicit DENY (명시적 DENY는 명시적 ALLOW보다 우선한다)
    3. Explicit ALLOW
    4. Implicit DENY
