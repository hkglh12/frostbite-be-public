
폴더 ACL
ACL 을 어떻게굴릴거냐.



PK 관리 필요
Long AclId;


---
"어떤 폴더(팀||개인) 에 대한 보호이냐 컬럼으로 구분할 수 있다."

AclResourceFolderType : AclResourecFolderType.enum(1,2,3) (팀 || 개인)
AclResourceFolderId : Integer (폴더PK)
---

---
"그래서, 이 ACL이 누구에게 영향을 주는데? 는 컬럼으로 구분한다"
AclSubjectType : AclSubjectType.ENUM (PRIVATE || TEAM)
SubjectId : Integer (사용자PK || TEAM PK)
----

----
" 팀에게 공유, 사용자에게 공유" 는 boolean값으로 제어가능하다.
isShared : boolean (true || false)
----

"ACL Group에 대한 어떤 동작을 수행할 수 있냐는 별도 관리해야한다. 권한있는 사용자라고 싹 다 삭제하고 싹다 공유걸게 허용해줄건 아니지않나"

PermissionBitMask : PermissionBitMask.enum(1,2,4,8,16)
----


이 ACL 적용 대상이 "팀 에게 공개 "일땐 괜찮은데, "팀 내 특정 인원에게만 공개" 일경우엔?

* ACL은 기본적으로 "찾을수 없으면 = DENY" 로 동작한다.
* 또한 Team 내에서도 "특정인원" 에게만 Open하고 싶은 경우가 있을 수 있다.

  	- Team 전체에게 ALLOW를 주는 일반적인 경우, group_team 이 아닌 group을 대상으로 ALLOW를 할당한다.
  		- 이 경우, Bitmask 계산은 Relation 테이블 내 역할(Leader, Manager, Member) 별로 각 (16,8,4)로 관리한다.
  	- 팀 내 일부에게만 접근권한 ALLOW를 제공할 경우 -> 
  		- Explicitly, 사용자가 허용할 사용자를 UI에서 지정한다. 또한 이경우 Bitmask는 
  			- 폴더생성자(folder_tb:create_user_id) 16, 나머지는 ACL에 직접 명시하도록 한다.
  			- 추후, 폴더 소유권이전 기능 및, 사용자 탈퇴 등에 대비하기 위해
  				folder_tb의 schema 변경이 발생한다.
  				ownerId -> resourceScopeId (보호자원 소속 범위) / resourceOwnerId (그래서 실제 그 resource의 관리자)
  	- 즉, 아래와 같이 구분한다.
  		- 팀 전체에게 허용 : AclSubjectType : TEAM + FOLDER_TB(ACCESSPOLICY : TEAM)
  		- 팀 일부에게 허용 : AclSubjectType : USER + FOLDER_TB(AccessPolicy: TEAM_MEMBER_SPECIFIC)
  		- 팀 일부에게 DENY : AclSubjectType : USER + AclEffectType:DENY + FOLDER_TB(AccessPolicy: TEAM_MEMBER_SPECIFIC)
  	- 즉, 폴더에 AccessPolicy 컬럼이 생겨야 한다.

* 팀 탈퇴, 강퇴를 생각해보니.. 권한도..바뀐다..


ACL은 매니저 (팀 역할 : 리더, 매니저, 팀원) 이상만 제어할 수 있도록 하자.
즉, 팀원은 ACL 변경요청을 생성하고 -> 매니저이상이 승인하면, 해당 ACL 이 생성되도록.
* 별도테이블 필요 *


위에 따라, ACL / folder를 아래와 같이 정의한다.

folder_tb

public class Folder {

    /** 폴더 식별자 */
    private Long folderId;

    /** 폴더명 */
    private String name;

    /** 폴더종류 (1: private, 2: team) */
    private FolderType folderType;

    /** 해당 폴더의 피종속 자원 식별자
     * folderType : private -> userid
     * folderType : team -> teamid
     * */
    @Deprecated
    private Integer ownerId;
    // 기존 OwnerId 를 -> resourceScopeId 변경 ( 팀폴더 -> 소속팀 PK, 개인폴더 -> 개인 PK) 를 나타내기 위함
    private Integer resourceScopeId;

    // 실제 리소스의 관리자. 이경우, "개인폴더 -> 개인" 이지만, 팀폴더의 특정 사용자 허용 일경우 사용자 탈퇴에 대비해, 소유권 이전을 할 필요가 있다.
    // Create_user는 메타정보니 건들지 말고, resourceOwnerId 로 관리하자. 실제 폴더를 생성하고 그 specific Allow로 폴더를 만든 사람이 resourceOwner가 된다.
    private Integer resourceOwnerId;

	// ACCESS POLICY : (PRIVATE || TEAM || TEAM_MEMBER_SPECIFIC)
	private AccessPolicy accessPolicy;

    /** 폴더 생성자 (user_id) */
    private Integer createUserId;

    /** 생성시각 */
    private LocalDateTime createTime;

    /** 수정자 (user_id) */
    private Integer modifyUserId;

    /** 수정시각 */
    private LocalDateTime modifyTime;

    /** 부모 폴더 식별자 */
    private Long parentFolderId;

    /** 출력순서 */
    private Integer renderSequence;

    /** UUID */
    private UUID uuid;

    /** 삭제여부 */
    private FolderStatus status;

    private String folderIdPath;
    private String folderNamePath;

}

** 폴더의 경우, ownerId(폴더 리소스 소속 범위) 를 의미를 명백히 하기 위해 resourceScopeId로 변경
** 폴더 실제 관리 주체를 명시하기 위해 resourceOwnerId 를 신규생성,
- 일반적인 경우 create_user_id = resourceOwnerId.
- 팀 탈퇴나, 소유권이전 발생시 create_user_id =/= resourceOwnerId

** 폴더 ACL은 아래와 같이 정리한다.

FolderACL{

	// ACL 식별자
	Long aclId;
	
	// ACL 보호대상 (Object, Folder) 식별자
	Integer folderId;
	
	// 접근제어 종류 ( ALLOW || DENY)
	AclEffectType effectType
	
	// 접근제어 주체 종류 ( USER || TEAM )
	AclSubjectType subjectType;
	
	// 접근제어 주체 식별자 (USER -> USERPK, TEAM -> TEAMPK)
	Integer subjectId;
	
	// 수행가능 작업 (1:READ, 2:CREATE, 4:DELETE, 8:SHARE || CONTROL , 16: MANAGE
	PermissionBitMask permissionBitMask;
	
	
	/** 접근제어 생성시각 */
    private LocalDateTime createTime;

    /** 제약 생성자 (user_id) */
    private Integer createUser;

    /** 삭제여부 */
    private AclStatus status;
}

ACL의 기본동작은 아래와 같다.

1. 폴더 ACL은 해당 폴더에 대해 동작한다. 즉, 상위폴더에 Browse 이상의 권한이 있다면, 접근권한이 없는 폴더더라도 조회는 된다.
    - 해당 폴더 Entries 조회 시에 튕겨나간다.

2. 폴더 ACL은 Bitmask에 따라 수행 가능한 작업이 상이하며, Linux Bitmask처럼 동작한다.
    - 1 : READ/Browse
    - 2 : CREATE
    - 3 : READ/BROWSE + Create
    - 7 : READ/BROWSE + CREATE + DELETE/MODIFY
    - 15 : READ/BROWSE + CREATE + DELETE/MODIFY + SHARE(Control)


3. ACL은 Implicitly DENY이다.
    - 따라서, ALLOW Type으로 직접 선언된 ACL이 없다면 DENY로 동작한다.
    - Explicitly Deny는 "팀 내 다른 멤버는 모두 허용되지만, 특정 사용자만 불능" 이다. 따라서 "SubjectType이 TEAM" 인 폴더에만 선언 가능하다.

4-1. ACL 적용 우선순위는 아래와 같다.
- TEAM Leader는 ACL을 무시하고 ALLOW ALL 이다.
- TEAM Manager 이하는 ACL에 영향을 받는다.
- 즉, TEAM LEADER/Manager ROLE > Explicitly Deny > Else Share (팀원/팀 외 공유) > Explicitly ALLOW > Implicity DENY 순으로 동작한다.
- 다만, UX를 위해 Team Leader는 Explicitly DENY를 UI에서 방지해야 한다.
- 다만, UX를 위해, SubjectType : TEAM 이 아닌 폴더에 대해서는 Explicitly Deny를 UI에서 막아야 한다.
- isShared 가 제외된다. 별도 테이블로 분리 (명백한 공유권한의 분리를 위해)
- 폴더 ACL은 "주체의 특정 폴더에 대한 허용 행위" 를 주로 관리한다.
- 공유는 "관심사 주체 외 특정 인원||특정 팀 의 특정 폴더에 대한 허용 행위" 로, 주 관심사 주체를 다르게 보는 방향으로 관점을 변경한다.

4-2. 역할과 ACL의 충돌
- 기본적으로 역할이 우선한다. 즉, ACL 폴더를 보기 전에 역할이 LEADER||MANAGER면 ACL을 무시하고 통과한다.
- 단, Leader ROLE까지 허용할지, Manager Role까지 허용할지는 추가로 고민해보자.
- 역으로 말하자면, ACL은 결국 TEAM MEMBER ROLE에 대해서만 동작한다.

5. AclSubjectType 아래와 같다.
    - USER : 개인을 대상으로 할 경우 적용 (개인폴더 소유자 || 공유대상 유저)
    - TEAM : 팀 전체에게 주어지는 SubjectType. (소속 팀 || 공유대상 팀)


6. 특정인원에게만 허용 (Explicitly ALLOW USER)
    1. 폴더 생성 : 폴더 AccessPolicy TEAM으로 폴더 생성, ACL SubjectType TEAM으로 ALLOW 생성
    2. 사용자가 (요청 || 혹은 즉시적용) 을 통해, 폴더 ACL을 User로 전환 (특정 인원에게만 허용 = 이외의 인원에게 거부, Implicitly Deny 활용)
        - 이때, 기존 ACL 제거
        - 신규 ACL, ALLOW + USER 특정 사용자에게만 추가
        - 폴더 AccessPolicy TEAM_MEMBER_SPECIFIC으로 전환
        - 추후 폴더 조회 시, "거부 목록에 있는가" 조회(Explicitly DENY 조회) -> "허용 목록에 있는가" 조회 (Explicitly ALLOW 조회)
    3. ACL 대상 추가 및 제거
        - 특정 ACL만 생성 / 제거

7. 특정 인원만 거부 (Explicitly DENY)
    1. 폴더 생성 : 폴더 AccessPolicy TEAM으로 폴더 생성, ACL SubjectType TEAM으로 ALLOW 생성
    2. 사용자가 (요청 || 혹은 즉시적용) 을 통해, 폴더 ACL을 TEAM으로 유지 (특정 인원에게만 거부 = 이외의 인원에게 허용)
        - 이떄, 기존 TEAM ALLOW ACL 유지!!!
        - 신규 ACL, DENY + USER 특정 사용자에게만 추가(DENY)
        - 폴더 AccessPolicy TEAM 유지
        - 추후 폴더 조회 시, "거부 목록에 있는가" 조회(Explicitly USER DENY 조회) -> "허용 목록에 있는가" 조회 (Explicitly TEAM ALLOW 조회)
    3. ACL 대상 추가 및 제거
        - 특정 ACL만 생성 / 제거

8. ACL의 기본 적용
    - 기본적으로 폴더 ACL은 상위폴더 ACL을 Clone 후 적용한다 (Explicitly Assign).
        - Inherit와는 살짝 개념이 다르다.
            - FrostBite의 Folder는 Explicitly Allow여야 함.
            - 이때, "폴더 경로" 에 대한 접근 권한을 검사 라는 개념이라면 굳이 ACL을 Clone후 적용이 필요없다고 느껴질 수 있으나,
              Explicitly ALLOW이므로 결국 해당 폴더 하위로 접근권한을 주려면 Explicitly ALLOW여야 함. ACL 복제 후 적용이 가장 깔끔할 것으로 판단됨.
    - 만약, ROOT폴더인 경우, 기본 ACL Template (ALLOW:TEAM) 을 적용한다.
    - 이때, 상위폴더 ACL 변경 발생 시, 하위폴더도 해당 ACL을 전파한다. (UI에서 경고한다)
        - 아무리 UI에서 상위폴더를 못보면 하위폴더 진입이 안된다 해도, API로는 진입할 수 있기때문.



9. 폴더와 ACL의 연계
    - Folder(FolderType: TEAM + AccessPolicy:USER) + FolderACL (EffectTYpe : ALLOW, SubjectType: USER)
        - 팀 폴더인데, 특정 사용자만 명시적 허용
    - Folder(FolderType: TEAM + AccessPolicy:USER) + FolderACL (DENY, USER)
        - 팀 폴더인데, 특정 사용자만 명시적 거부
    - Folder(FolderType: TEAM + AccessPolicy:TEAM) + FolderACL (ALLOW, TEAM)
        - 팀 전체 접근 허용
    - Folder(FolderType: TEAM + AccessPolicy:TEAM) + FolderACL (ALLOW, USER)
        - 이 경우, 특정 폴더에 대해서 BitMask 갱신을 주 목적으로 함.
            - 기본적으로 bitmask는 4로 생성됨 (Role Base여서 ADMIN이나 Manager는 ACL을 보지 않고, 해당 폴더에 대해 각 31, 15의 권한을 행사함)
            - 이경우 Bitmask 갱신에 의의가 있음.
    - Folder(FolderType: private + AccessPolicy : USER) + FolderACL ( ALLOW, USER)
        - 개인폴더. FolderType Private인경우 AccessPolicy는 TEAM일수 없고, FolderACL EffectType이 DENY 이거나 SubjectType이 Team일 수 없음

10. 공유와 ACL의 연계
    - 이 단계에서, 공유를 고려하지 않음. 다만, ACL 그 자체의 본질에 집중하기 위해 별도 테이블 분리까지만 정리

* 위 재설계를 통해 아래 테이블의 변경이 발생한다.

folder_tb
- ownerId (이름 구분 쉽도록 변경)
+ resourceScopeId[USER||TEAM] ( from ownerId, 해당 자원의 소속 범주를 명시 )
+ resourceOwnerId ( 신규, 실제 해당 폴더의 관리자, 소유자 를 명시 )
+ AccessPolicy[USER||TEAM||TEAM_MEMBER_SPECIFIC]

ACL_TB
- isShared (별도테이블분리)


* 추후 확장 포인트
    - 폴더 Bitmask 별도처리
    - 폴더 ACL Propagation 은 비동기 처리 고려