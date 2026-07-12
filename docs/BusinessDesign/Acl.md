# ACL 도메인 설계

## 주 목적
- ACL은 접근 주체가 폴더와 파일에 대한 접근 가능 여부와 수행 가능 행위를 판단하기 위한 모델임
- 폴더/파일에 대한 접근은 Explicitly ALLOW, 즉 접근 가능을 명시한 ACL이 없을경우 기본적으로 접근 거부로 동작한다.

## 핵심 원칙
    - ACL은 실제 접근 허용/거부 엔트리 의미를 가진다.
    - ACL은 Implicit DENY, Explicit ALLOW 로 동작한다.
    - ACL은 Folder 도메인과 조합해 생각해야 한다.
        - Folder.folderType 이 폴더 종류를 설명하고(팀폴더 / 개인폴더)
          Folder.accessPolicy가 "폴더의 접근제어 종류" 를 설명한다. 이로부터 ACL의 의미가 달라질 수 있다.
            - Folder.folderType = TEAM이면 Folder.resourceScopeTeamId만 값을 가지고 Folder.resourceScopeUserId는 NULL이다.
            - Folder.folderType = PRIVATE이면 Folder.resourceScopeUserId만 값을 가지고 Folder.resourceScopeTeamId는 NULL이다.
            - Folder.folderType = TEAM + Folder.accessPolicy = USER -> 팀 폴더이지만 특정 사용자에게만 접근허용
                - 이경우, 해당 폴더 ACL은 subjectType = TEAM을 가질 수 없다. 특정 사용자에게만 허용된 종류이기 때문
    - Entry Folder 생성 시 ACL은 아래와 같이 동작한다.
        - 팀 Entry Folder :
            - 팀 Entry Folder는 반드시 "모든 팀원 ALLOW, 모든 팀원에게 * 동작권한"을 제공한다.
                - 실제 ACL Entry는 subjectType = TEAM, subjectTeamId = 생성된 팀 PK, PermissionBitMask = 모든 권한 비트 합산값. 단, NONE(0)은 권한 비트가 아니므로 제외한다.
                - 일부 인원 ALLOW 시 -> 일부는 팀 폴더 이용이 아예 불가능. 이경우 팀을 나누는게 옳은 케이스가 된다.
                - 일부 동작권한만 제공 시 -> 팀 폴더 전체에 대해 해당 제약이 생성되어 관리가 오히려 불편해질 수 있음. 혹은 공지사항폴더가 됨.
        - 개인 Entry Folder:
            - 개인 Entry Folder는 "소유자 ALLOW, 소유자 * 동작권한" 을 제공한다. 여기서 소유자는 resourceScopeUserId 식별자를 사용하는 사용자.
                - 실제 ACL Entry는 subjectType = USER, subjectUserId = 생성한 사용자PK, PermissionBitMask = 모든 권한 비트 합산값. 단, NONE(0)은 권한 비트가 아니므로 제외한다.
                - 개인 Entry Folder에서는 사용자가 직접 ACL을 조작할 수 없다. 접근권한의 변경은 "공유" 기능을 통해서만 조작 가능하다.
    - Root Folder 생성 시 ACL은 아래와 같이 동작한다.
        - 팀 Root Folder : 
            - 팀 Root Folder는 default "모든 팀원 ALLOW, 모든 팀원 * 동작권한" 을 제공한다.
                - 실제 ACL Entry는 subjectType = TEAM, subjectTeamId = 생성된 팀 PK, PermissionBitMask = 모든 권한 비트 합산값. 단, NONE(0)은 권한 비트가 아니므로 제외한다.
                - 폴더 생성 시점에 사용자가 직접 ACL 제약을 지정할 수 있다. 이경우 folder 생성 -> 기본 acl 생성 -> acl 제약변경 으로 처리한다.
        - 개인 Root Folder:
            - 개인 Root Folder는 "소유자 ALLOW, 소유자 * 동작권한"을 제공한다. 여기서 소유자는 resourceScopeUserId 식별자를 사용하는 사용자.
                - 실제 ACL Entry는 subjectType = USER, subjectUserId = 생성한 사용자PK, PermissionBitMask = 모든 권한 비트 합산값. 단, NONE(0)은 권한 비트가 아니므로 제외한다.
                - 개인 Root Folder에서는 사용자가 직접 ACL을 조작할 수 없다. 접근권한의 변경은 "공유" 기능을 통해서만 조작 가능하다.
    - Sub 폴더 생성시, ACL은 아래와 같이 동작한다.
        - 상위 폴더의 유효 ACL (status = ACTIVE) 전체 복제 후 재할당한다. 즉, 명시적 복제 방식으로 동작한다.
            - Clone의 대상은 접근제어 대상종류(subject_type), 제어종류 (effect_type), 제어적용대상(subject_[team||user_id]), 권한비트(permission_bit_mask) 로 제한한다.
            - 이후, 직접적 메타정보는 현재 ACL을 생성/수정하는 사용자의 메타정보로 대체한다.
        - 위의 원칙을 위해, 한개의 폴더는 최소 1개의 AclStatus.ACTIVE + AclEffectType.ALLOW인 ACL을 가지고 있어야만 한다. 해당 ACL이 단 한개도 없는 폴더는 허용되어서는 안된다.
    - ACL의 적용 범위
        - ACL은 선언된 folderId에만 동작한다.
    
    - FILE ACL의 적용
        - FILE ACL은 Implicitly 폴더 ACL을 따라간다.
        - 직접 제어가 필요할 경우 File별 ACL을 생성한다.
            - resource_owner_id 모든사람 건들수없음
            - 특정 인원만 deny

## 도메인 객체 설명

### FolderAcl
    - aclId : ACL 식별자
    - folderId : 이 ACL이 보호하려는 폴더 PK
    - effectType : 이 ACL이 명시하는 접근 허용 타입을 의미한다
        - ALLOW : 접근 허용
        - DENY : 명시적 접근 불허, "팀 전체에 접근은 허용되지만, 특정 인원에게 허용하지 않음" 시 사용

    - subjectType : 이 ACL이 접근을 제어하려는 접근제어 주체 종류를 의미
        - TEAM : 이 ACL은 팀에게 적용되는 객체이다.
        - USER : 이 ACL은 사용자에게 적용되는 객체이다.
    - subjectTeamId
    - subjectUserId
        - subjectType이 TEAM -> temaId에 team PK를 입력하며, 팀의 행동을 제어한다.
        - subjectType이 USER -> userId에 User Pk를 입력하며, 사용자의 행동을 제어한다.
        - 두 컬럼은 상호 배타적이며, 정확히 하나만 NOT NULL이어야 한다.
    - permissionBitMask : 이 ACL이 허용하는 접근주체의 동작을 의미한다.
        - NONE(0) : 그 어떠한 권한도 갖지 못함
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

### permissionBitmask 이용
    - permissionBitmask ENUM 내, 각 권한은 각각의 독립적인 비트값을 가진다.
    - DB 저장 시 permissionBitMask는 Integer 형으로 저장하며
      해당 권한 이하의 하위 권한을 모두 허가한것으로 간주해, 하위 권한을 모두 포함한 누적값으로 저장해야 한다. 
      단, ACL effect_type이 ALLOW일 경우 0 (NONE)은 제외한것으로 간주해야 하며, ACL 생성시에도 ALLOW Type일 경우 permissinoBitMask.NONE 의 삽입을 비즈니스 로직에서 방지해야 한다.
    - 예시 : WRITE = 7 (Browse 1 + READ 2 + WRITE 4) = 7

### permissionBitMask 권한 판정
- ACL의 `permissionBitMask`는 하위 권한을 포함한 누적 비트마스크 값으로 저장한다.
    - 예: WRITE 권한은 BROWSE(1) + READ(2) + WRITE(4) = 7로 저장한다.
    - 예: MANAGE 권한은 BROWSE(1) + READ(2) + WRITE(4) + CONTROL(8) + MANAGE(16) = 31로 저장한다.
  - 동일 사용자가 USER ACL과 TEAM ACL을 동시에 만족할 수 있으므로, ALLOW 판정 시에는 적용 가능한 ALLOW ACL 중 가장 큰 `permissionBitMask` 값을 대표 권한으로 사용한다.
      - 예: `ALLOW USER:7, permissionBitMask = 7`
      - 예: `ALLOW TEAM:13, permissionBitMask = 31`
      - 이 경우 최종 판정값은 `31`이다.
  - 단, `permissionBitMask = 0`은 DENY ACL에서만 사용할 수 있다.
      - ALLOW ACL 생성 시 `NONE(0)`은 허용하지 않는다.
      - 따라서 ALLOW 판정 흐름에서는 `0`을 고려하지 않는다.

### 접근 허용 판정 우선순위
    1. 팀 폴더에서는 팀 내 역할 (TeamRole.MEMBER, TeamRole.MANAGER, TeamRole.HEAD) 중, HEAD와 MANAGER는 ACL을 무시하고 동작한다.
    2. Explicit DENY (명시적 DENY는 명시적 ALLOW보다 우선한다)
    3. Explicit ALLOW
    4. Implicit DENY

## Folder ACL : Team Folder 유의 사항
    - Implicit DENY, Explicit ALLOW라 했기에, effectType = DENY가 의미없어 보일 수 있으나, 아래 상황에 유효하다.
        - 특정 폴더 -> TEAM ALLOW, USER DENY
            - 이경우, "TEAM Explicit ALLOW를 통해 모든 사용자가 접근 가능, 그러나 USER Explicit DENY에 의해 특정 사용자 접근불가"
            - Implicit DENY를 사용하면, "100명 중 1명에게만 거부" 생성 시, "ALLOW를 100개 만들어야함"
    - 팀 폴더는 아래의 대표 케이스를 가진다.
        - 팀 폴더, 전체팀에게 허용
            - Folder.folderType = TEAM + Folder.resourceScopeTeamId = teamPk + Folder.resourceScopeUserId = NULL + Folder.accessPolicy = TEAM
            + FolderAcl.effectType = ALLOW + FolderAcl.subjectType = TEAM + FolderAcl.subjectTeamId = teamPk
        - 팀 폴더, 특정 사용자에게만 허용
            - Folder.folderType = TEAM + Folder.resourceScopeTeamId = teamPk + Folder.resourceScopeUserId = NULL + Folder.accessPolicy = USER
            + FolderAcl.effectType = ALLOW + FolderAcl.subjectType = USER + FolderAcl.subjectUserId = userPk
            - 위 조합으로 허용 사용자 N개 만큼 ACL이 존재해야 한다.
        - 팀 폴더, 특정 사용자만 불허
            - Folder.folderType = TEAM + Folder.resourceScopeTeamId = teamPk + Folder.resourceScopeUserId = NULL + Folder.accessPolicy = TEAM
            + FolderAcl.effectType = ALLOW + FolderAcl.subjectType = TEAM + FolderAcl.subjectTeamId = teamPk
            
            - 위 ACL에 더해, 아래 ACL이 접근 불가할 사용자 N개 만큼 있어야 한다.            
            - Folder.folderType = TEAM + Folder.resourceScopeTeamId = teamPk + Folder.resourceScopeUserId = NULL + Folder.accessPolicy = TEAM
            + FolderAcl.effectType = DENY + FolderAcl.subjectType = USER + FolderAcl.subjectUserId = userPk
    - 단, effectType = DENY인 경우 "접근 자체를 막는 것" 이므로, permissionBitMask는 값은 반드시 0이어야 한다.
        - 구현 시 permissionBitMask는 nullable이어야 하며, DB 컬럼도 NULL을 허용해야 한다.

## Folder ACL : Private Folder 유의 사항
    - 개인폴더는 ACL이 사용자 단위로 떨어져야 하며, 폴더와 조합해서 판단해야 한다.
        - Folder.folderType = PRIVATE + Folder.resourceScopeTeamId = NULL + Folder.resourceScopeUserId = userPk + Folder.accessPolicy = USER
        + FolderAcl.effectType = ALLOW + FolderAcl.subjectType = USER + FolderAcl.subjectUserId = userPk

### ACL 사용 유의사항
    - 아래의 케이스는 유효하지 않은 조합이다.
        - Folder.folderType = PRIVATE + Folder.accessPolicy = TEAM 금지
        - Folder.folderType = PRIVATE + FolderAcl.subjectType = TEAM 금지
        - Folder.folderType = TEAM + Folder.accessPolicy = USER + FolderAcl.subjectType = TEAM 금지
        - Folder.accessPolicy = TEAM_MEMBER_SPECIFIC 사용 금지
        - FolderAcl.subjectType = PRIVATE 사용 금지
    
    - 아래의 케이스는 허용되지 않는다.
        - FolderAcl.effectType = DENY + FolderAcl.permissionBitMask != 0

## ACL API 설계 초안

- ACL API는 후순위로 개발한다.
- 특정 폴더의 ACL 변경 기능을 제공한다.
- 팀 전체 허용과 특정 사용자 허용/거부 전환을 지원한다.
- 특정 폴더 ACL 생성 기능을 제공한다.
- ACL 생성 행위 가능자는 CONTROL 이상의 권한을 가진 사용자로 제한하는 방향을 기본값으로 둔다.
- 공유 기능은 ACL 생성/변경 API를 직접 노출하기보다 공유 유스케이스를 통해 ACL을 조작한다.

## 행위 권한 매핑

- 요청 진입 시 API가 요구하는 권한을 명시적으로 판정한다.
- 각 동작에 필요한 PermissionBitMask를 String 배열, record, enum mapping 등으로 관리하는 방안을 검토한다.
- 예시:
  - 폴더/파일 리스팅: BROWSE
  - 파일/폴더 읽기 및 다운로드: READ
  - 생성, 수정, 삭제, 덮어쓰기: WRITE
  - 공유, ACL 제어, 주요 메타데이터 변경: CONTROL
  - 소유권 이전: MANAGE

## 공유와 ACL

- 공유 기능은 ACL을 생성하거나 변경하는 유스케이스다.
- 파일/폴더 공유 시 각 오브젝트 타입에 맞는 ACL을 추가한다.
- 공유받은 파일과 폴더는 ACL을 기준으로 조회한다.
- 사용자에게 직접 공유된 항목뿐 아니라 사용자가 소속된 팀에게 공유된 항목도 조회 대상에 포함한다.
