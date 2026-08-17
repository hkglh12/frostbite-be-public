# 객체의 관리 및 VO의 계층이동

## 용어의 설명

- RequestDTO : 수정/생성 계열 사용자 입력, UseCase별로 관리
- Condition : 조회 UseCase에서 여러 조회조건을 하나의 의미 있는 계약으로 묶을 필요가 있을 때 활용
  - Condition은 조회라는 이유만으로 레이어 끝까지 유지하는 단순 전달용 객체가 아니다.
  - 단순 식별자 또는 소수의 값만 필요한 조회는 필요한 값을 직접 인자로 전달할 수 있다.
  - 동일한 자원을 PK/UUID 등 서로 다른 식별방식으로 조회하더라도 검색 목적과 출력 의미가 동일하다면 동일 UseCase로 간주한다.
    - 필요 시 하나의 Condition에서 복수 식별방식을 표현하고 Mapper의 `<choose>` 등으로 분기할 수 있다.
  - 검색 목적, 권한 계약, 출력 의미가 달라지는 경우 별도 UseCase로 간주하여 별도 Condition을 생성한다.
  - 예시
    - 관리자의 비활성화 고객 검색(Range 검색 + status 검색) / 사용자의 본인 계정 상태 확인(Exact match) → 별도 Condition
    - 동일 Folder를 `folderId` 또는 `folderUuid`로 식별 → 동일 조회 목적이라면 동일 Condition에서 처리 가능
- Domain Entity : 가변, 행위기반 메서드로 Setter 통제, 도메인 상태표현(.lock(), .deactive()) 혹은 insert 계열 최종형태로 활용
- Command : 불변, 비즈니스 로직 내 변수로 누적정제 후 Update 비즈니스 로직 최종형태, UseCase별로 관리
  - 단순 변경/삭제일 경우 useCase별 xml 활용, service layer에서는 식별자만 전달
  - 복합 변경/삭제일 경우 useCase별 Command 활용
  - Record-Mybatis 활용은 Mybatis^3.5. Record는 getter가 getXXX가 아니라 필드명이 되는데, mybatis ^3.5에서 record Accessor 인식됨
- Projection : 코드 내에서 약속 상 읽기전용, @NoArgs + @Getter + @Setter 조합 사용 (추후 테스트코드에서 필요하면 @AllArgs 추가). 단 Projection에 대한 코드 상 Setter 호출 금지
  - mybatis가 getInstance + setter 로 처리하는 케이스에 대해 -> Lombok @Value(final임) 활용 불가
  - mybatis xml 내 resultMap 매핑 후 constructor 선언 -> @Value는 쓸 수 있게 되나 constructor 인자 순서가 select와 일치해야 하는 제약 발생 -> name 선언으로 매핑이 가능하지만 컴파일 인자값이 추가되어야 하고 상당히 번거로울 수 있음
  - @NoArgs 기반이면 mybatis가 Setter 기반으로 호출하므로 별도의 순서의존성이 발생하지 않으므로 해당 방식 채용 = 코드내에서 setter 호출 금지
- ResponseDTO : 사용자 응답 제출 최종형태. UseCase별로 관리
- Draft : 단순 정제는 변수로 처리하되, 여러 단계에 걸쳐서 값이 연동되어 전달됨과 동시에 누적정제가 발생할 때 활용. 이때 Draft는 원본의 상태나 필드를 누적정제하고 있다면 원본객체와 연결짓기 위해 식별자 필요

## DTO의 폴더 관리

- 내부전용 VO (외부 노출 안되는 계층간 이동 VO) 의 경우 internalDTO 패키지에 선언한다
- 외부노출 VO (외부에 노출되는 DTO)의 경우 dto 패키지에 선언한다
- 도메인 엔티티의 경우 domain 패키지에 선언한다.

## 도메인 엔티티의 생성

- 이 문서상에서 엔티티는 DB에 저장 가능한 상태로써, 하나의 객체로 표현될 수 있는 값과 상태의 집합체를 의미한다.
- 이 문서상에서 도메인은 엔티티의 비즈니스 규칙과 상태변화, 행위를 포함한다.
- 이 문서상에서 도메인 엔티티는 규칙과 행위를 준수해 생성 및 변경이 통제되는 엔티티를 의미한다.
- 필수값이 명확하며, 그 비즈니스 플로우 상 도메인 엔티티로 취급하기 위한 유효한 값이 모두 제공된 경우 Factory 패턴을 적극 사용해 엔티티로 전환한다.
- Draft를 사용하는 상황일 경우(누적정제가 있을 경우) 유연성을 더하기 위해 builder패턴을 사용해 엔티티로 전환한다.

## 도메인 엔티티 생성 컨벤션 이유

- 팩토리패턴을 사용함으로써, 중간상태값을 가지는 도메인 엔티티 생성을 방지
- 팩토리패턴을 사용함으로써, 해당 비즈니스플로우에서 필수값이 null인 객체의 생성을 방지하고, 도메인 엔티티 전환 시 필수값 검증을 위한 if문을 최대한 캡슐화
- 빌더패턴을 사용함으로써, Draft등 누적정제가 필요한 상황이었음을 알리는 컨벤션 용도로 사용

## 비즈니스 플로우 별 각 레이어 진입/진출(수직) 시 컨벤션

1. 사용자 요청 -> Controller
   - Create/Modify/Delete 계열은 RequestDTO로 Accept
   - 조회 계열에서 조회조건이 하나의 의미 있는 조건집합을 형성하는 경우 Condition으로 Accept
   - Validation 적극 활용

2. Controller -> Service
   - DTO/Condition 또는 단순 인자값으로 전달
   - Service 진입 후 아래의 경우를 활용

   CASE1. 생성 비즈니스 로직
   - 행위기반 메서드명으로 변경 통제
   - 추가 누적 변경 및 상태누적관리가 필요할 경우 Draft로 처리
   - 단일변경이나 단순정제면 지역변수로 처리
   - 정제 완료 시 Domain Entity로 전환

   CASE2. 수정 비즈니스 로직
   - 비즈니스 로직 내에서 누적정제는 변수/Draft 적극활용 후 Command로 최종전환

   CASE3. 삭제 비즈니스 로직
   - 단순삭제일 경우 식별자 전달
   - 복합삭제일 경우 Command로 전환

   CASE4. 조회 비즈니스 로직
   - Condition은 해당 조회 UseCase의 조건을 표현하는 동안 활용한다.
   - 조회 도중 외부 식별자(UUID 등)를 통해 실제 자원을 Resolve한 경우, 이후 내부 흐름에서는 Resolve된 Entity/Projection에서 필요한 내부 식별자(PK 등)를 추출해 사용할 수 있다.
   - 최초 요청에서 사용한 Condition을 단순히 최초 요청 객체라는 이유로 후속 내부 조회까지 계속 전달하지 않는다.
   - 이미 Resolve가 끝난 자원에 대한 후속 내부 조회는 필요한 식별자/상태값만 전달할 수 있다.

   예:
   ```
   parentFolderUuid
       ↓
   접근 가능한 ParentFolder Resolve
       ↓
   parentFolderId 확보
       ↓
   자식 Folder/File 조회는 parentFolderId 사용
   ```

3. Service -> Mapper

   CASE1. Create
   - domain entity로 진입

   CASE2-1. Update
   - command로 진입

   CASE2-2. 단순 상태변이/전파
   - 상태 전파 메서드를 통한 인자값으로 update 진입

   CASE3. Read/Listing
   - 조회조건이 하나의 의미 있는 조건집합을 형성하는 경우 Condition으로 진입한다.
   - 단순 식별조회 또는 이미 Resolve가 끝난 자원의 내부 조회는 식별자 등 필요한 최소 인자를 직접 전달할 수 있다.
   - 하나의 자원을 PK/UUID 등 서로 다른 방식으로 식별하는 차이만 존재한다면, 별도 Mapper/Condition을 불필요하게 분리하지 않고 하나의 Condition + `<choose>` 등의 분기를 활용할 수 있다.
   - 단, 조회 목적, 권한 계약, 출력 의미가 다르면 별도 Mapper 메서드 및 필요 시 별도 Condition으로 분리한다.
   - Mapper 메서드명은 단순 식별조회인지, 접근 가능한 자원을 조회하는 것인지 등 조회 계약이 드러나도록 작성한다.

   예:
   ```java
   selectFolderById(folderId);                       // 내부 직접 조회
   selectAccessiblePrivateFolder(condition);         // 사용자 접근조건 포함 조회
   selectAccessibleTeamEntryFolderByTeamUuid(
       teamUuid,
       userId
   );                                                // 접근 가능한 자원 Resolve
   ```

   CASE4. Delete
   - 단순 삭제일 경우 식별자 전달
   - 복합삭제일 경우 Command로 진입

4. Mapper -> Service

   CASE1. 일반적인 사용자 응답용 단순 조회
   - 별도 후속 정제가 없고 UseCase 출력형태와 동일한 경우 ResponseDTO로 제출 가능

   CASE2. 후속 정제과정이 있거나, 수평 레이어 이동이 예상되는 경우
   - 리스트조회/객체조회 구분 없이 Projection 또는 필요한 Domain Entity로 진출
   - Projection의 경우 UseCase별로 객체를 별도로 준비한다.
   - Domain Entity를 조회 결과로 활용할 경우 해당 흐름에서 필요한 상태/식별자 확보를 목적으로 하며, 다른 도메인에서 임의 변경하지 않는다.

5. Service -> Controller
   - Projection 활용중이었을 경우 진출 직전에 ResponseDTO로 전환
   - Draft가 있었다면 해당 단계에서 활용
   - 이미 ResponseDTO 형태로 완성되어 있다면 그대로 진출

6. Controller -> 사용자
   - ResponseDTO로 제출

> Insert/Update의 경우 최종 형태(Domain Entity) 값을 이미 코드에서 알고 있을 경우 그대로 활용한다. 별도 조회가 필요하다면 조회 후 Entity/Projection 등 현재 UseCase에 적합한 형태로 활용한다.

## 외부 식별자 Resolve 이후의 내부 흐름

- 외부 인터페이스에서 전달된 UUID 등의 공개 식별자는 가능한 경우 자원의 존재 여부 및 해당 UseCase에서 필요한 접근 가능 Scope/소속 조건을 함께 확인하며 Resolve한다.
- Resolve 과정은 단순 UUID -> PK 변환으로 보지 않는다.
  - 외부 입력으로 지정된 자원의 존재 여부 확인
  - 사용자의 Scope/소속 관계 확인
  - 후속 비즈니스에 필요한 내부 데이터 확보
  를 함께 수행하는 자원 검증 단계로 본다.
- Resolve가 완료된 이후에는 Entity/Projection에서 내부 PK를 추출하여 후속 Mapper/Service 흐름에 활용할 수 있다.
- 후속 Mapper가 동일한 외부 UUID를 반복적으로 다시 해석하도록 만들지 않는다.
- 부모/자식 간 Scope 불일치처럼 도메인상 허용되지 않아야 하는 상태는 매 조회마다 중복 검증하기보다 생성/이동/수정 UseCase에서 invariant로 통제하는 것을 우선한다.

예:
```
teamUuid + userId
    ↓
접근 가능한 Team EntryFolder Resolve
    ↓
folderId
    ↓
후속 Folder/File 조회
```

## 다른 도메인의 관계 확인과 정책 판정

- 타 도메인의 단순 관계/소속 사실 확인은 현재 조회 SQL의 `JOIN`, `EXISTS` 등을 통해 함께 처리할 수 있다.
  - 예: 사용자가 특정 Team의 구성원인가
  - 예: Folder가 특정 사용자/Team Scope에 속하는가
- 단순 관계 확인만을 위해 반드시 타 도메인 ResolverService를 선행 호출할 필요는 없다.
- 단, 여러 사실을 해석하여 허용/거부, 행위 가능 여부 등 도메인 정책을 판정해야 하는 순간에는 SQL 외부로 분리하고 해당 도메인의 ResolverService/Policy를 활용한다.
  - 예: ACL ALLOW/DENY 우선순위 판정
  - 예: PermissionBitMask에 따른 READ/WRITE 허용 판정
- 도메인 정책과 허용 판정이 조회 SQL 내부에 숨어 캡슐화되지 않도록 한다.

판단 기준:
```
"관계가 존재하는가?"          → SQL JOIN / EXISTS 가능
"그래서 이 행위가 허용되는가?" → ResolverService / Domain Policy
```

## 동일 레이어 내 다른 도메인(혹은 서비스) 진입/진출(수평) 시 컨벤션

1. 각 도메인에서 요구하는 인자값만을 변수로 추출하여 파라미터로 전송한다.
   - 단, 파라미터 수가 많아지거나 별도 필요가 있을 경우 Command/internalDTO를 활용해 메서드 시그니쳐가 가독성이 떨어지는 것을 방지한다.
   - 이 경우, 호출 도메인 서비스가 피호출 도메인 서비스의 객체/엔티티/계약에 대해 결합이 발생하나, 입력값에 대한 계약만 인지하는 정도로 느슨한 결합을 허용하는 선으로 간주한다.
   - 단순 관계/소속 사실 확인만 필요한 경우에는 별도 서비스 호출 대신 현재 조회 SQL에서 JOIN/EXISTS로 해결할 수 있다.
   - 타 도메인의 도메인 정책을 해석하거나 행위 허용 여부를 판정해야 하는 경우에는 해당 도메인 ResolverService/Policy에 위임한다.

2. 수평 레이어 이동 시, 피호출 서비스는 UseCase 수행 결과를 반환할 수 있으며, 반환 형태는 식별자, 상태값, internalDTO, Projection, 필요 시 Entity 등으로 선택할 수 있다.
   - 기본적으로는 현재 호출 측이 필요로 하는 최소 결과값 반환을 권장한다.
   - Entity 반환은 호출 측에서 해당 도메인 상태를 조회/참조해야 하거나, Resolve 결과로 내부 식별자/상태를 이어서 사용해야 하는 경우에 한해 사용한다.
   - 다른 도메인 서비스에서 리턴한 Entity는 읽기전용이며, 별도의 수정이 필요한 경우 해당 비즈니스 서비스의 흐름에 위임해야 한다.
     - 예: 사용자 생성 -> 폴더 생성 -> 폴더 이름변경 필요 시 다시 폴더서비스 호출
   - 타 서비스에서 재사용 가능한 별도 메서드를 분리함으로써 해당 도메인 서비스의 UseCase 재사용성을 높일 수 있다.
     - 예: 사용자 생성 -> 폴더 자동생성(별도메서드) -> 실제 폴더 생성메서드(원래메서드) -> Entity로 리턴
   - 추가적인 정제나 수정이 필요한 경우 해당 도메인 서비스에게 다시 위임하여 해당 도메인 서비스가 객체를 조작(Draft/Command)하도록 한다.

## 엔티티와 DTO의 변환

- 엔티티 클래스는 가능한 한 인스턴스 생성과 상태 보존 및 상태 변경 행위에 집중한다.
- 엔티티 클래스는 DTO를 알지 않으며, DTO 변환 책임을 가지지 않는다.
- 엔티티와 DTO 간 변환은 서비스 레이어를 중심으로 수행하며, 필요 시 별도의 변환 책임 객체로 분리할 수 있다.

## Draft 사용 예시

- 파일업로드 -> 임시경로적재
- 비동기로 우리 스토리지로 이동 -> (1차 변경: 신규패스)
- 비동기로 전문검색을 위한 전처리경로 -> (2차 변경: 전처리경로)
- DB에는 전처리 완료된 문서만 저장 (그럴 일은 없겠지만 예를 들면)

- 즉 여러 단계에 걸쳐서 MAP으로 관리할 만한 것들을 모두 draft로 관리. (Get 시점 컴파일에러 스킵 방지 및 타입안정성 체크)
- 무분별한 draft 생성 전, 단순정제 -> 지역변수 / Entity 값변경, 상태변경을 통한 흡수 가능 -> Entity 활용 / 외부결과 조합 -> `factory` / 단순 값 변경 -> helper 활용을 우선적으로 생각할 것

## Domain 생성 및 Factory 활용

- 2026-05-24 시점 도메인, DTO 등 객체 생성 중 factory 방식은 모두 정적 메서드 기반 팩토리 기법
- 추후, 비즈니스 로직이 검증 때문에 지나치게 복잡해지거나, 다른 서비스가 다른 객체를 생성하기 위한 조건들을 과하게 들고 있을 때
  - 예: folderService가 folderAcl 생성조건을 과하게 알기 시작할 때
  - 별도 Factory/Resolver/Policy 등으로 분리하는 것을 검토한다.
