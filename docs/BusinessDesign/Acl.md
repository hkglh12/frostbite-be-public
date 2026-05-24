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
    
    - 하위 폴더 생성시, ACL은 아래와 같이 동작한다.
        - 상위 폴더의 유효 ACL 전체 복제 후 재할당한다. 즉, 명시적 복제 방식으로 동작한다.
    - ACL의 적용 범위
        - ACL은 선언된 folderId에만 동작한다.
    
    - FILE ACL의 적용
        - FILE ACL은 Implicitly 폴더 ACL을 따라간다.
          직접 제어가 필요할 경우 File별 ACL을 생성한다.

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
    - 단, effectType = DENY인 경우 "접근 자체를 막는 것" 이므로, permissionBitMask는 값이 있어서는 안된다. NULL로 할당되어야 함
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
        - FolderAcl.effectType = DENY + FolderAcl.permissionBitMask != NULL
