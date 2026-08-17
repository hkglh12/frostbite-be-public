# 일반 컨벤션

## 외부 식별자와 내부 식별자

- 외부 인터페이스에서는 내부 PK를 최대한 노출하지 않으며, 외부 식별이 필요한 경우 UUID 등 별도의 공개 식별자를 사용한다.
- OWASP IDOR(Insecure Direct Object Reference) 및 Broken Access Control 위험을 줄이기 위함
- 모든 조회/수정/삭제 요청은 반드시 서버 측 접근조건 및 권한 검증을 수행한다.
- 내부 PK는 DB 식별 및 내부 조인/참조 용도로만 사용하고, 외부 요청 파라미터나 응답에는 직접 노출하지 않는 것을 기본 원칙으로 한다.

### 외부 식별자의 Resolve

- 외부에서 전달받은 UUID는 가능한 경우 해당 자원에 대한 존재 여부 및 요청자의 접근 가능한 Scope/소속 여부를 확인하면서 내부 자원으로 Resolve한다.
- Resolve 결과로 Entity/Projection 등을 확보한 이후의 내부 비즈니스 흐름에서는 해당 객체의 내부 PK를 활용할 수 있다.
- 후속 Mapper가 동일한 외부 UUID를 반복적으로 다시 해석하도록 만들지 않는다.
- 최초 Resolve SELECT를 단순한 UUID -> PK 변환 비용으로만 간주하지 않는다.
  - 외부 입력으로 지정된 자원의 존재 여부 확인
  - Scope/소속 관계 확인
  - 후속 비즈니스에 필요한 내부 데이터 확보
  를 함께 수행하는 자원 검증 단계로 간주한다.

예:
```
parentFolderUuid
    ↓
현재 사용자가 접근 가능한 ParentFolder 조회
    ↓
ParentFolder.folderId
    ↓
자식 Folder/File 조회
```

- UUID는 외부 경계의 공개 식별자로 사용하고, Resolve 이후의 내부 처리에서는 PK 사용을 우선한다.
- 단, 내부 처리에서도 UUID 자체가 비즈니스 의미를 가지거나 외부 시스템 연계 식별자로 필요한 경우에는 예외로 한다.

## 관계 확인과 도메인 권한 판정

- 단순 관계/소속 여부는 조회 SQL의 `JOIN` / `EXISTS`를 통해 처리할 수 있다.
  - 사용자가 특정 Team의 구성원인가
  - 해당 Folder가 특정 사용자 또는 Team Scope에 속하는가
- 단순 관계 확인을 위해 반드시 별도 ResolverService를 선행 호출할 필요는 없다.
- 단, 단순 관계 사실을 넘어 허용/거부 등의 접근정책 또는 행위 허가와 같은 도메인 정책을 판정해야 하는 경우 SQL 외부로 분리한다.
- 해당 정책 판정은 ResolverService 또는 Domain Policy에 위임한다.
- ACL과 같이 ALLOW/DENY 우선순위, PermissionBitMask 등 여러 규칙을 조합해야 하는 권한 판정을 단순 조회 SQL 내부에 숨기지 않는다.

판단 기준:
```
관계/소속이라는 사실을 확인한다
→ JOIN / EXISTS

여러 사실을 해석하여 허용/거부를 결정한다
→ ResolverService / Domain Policy
```

### ACL 권한 판정

```java
// ACL 권한 판정은 조회 SQL에 포함하지 않는다.
/*
 * 단순 관계/소속 여부는 SQL JOIN/EXISTS로 처리한다.
 * -> 해당 자원이 특정 사용자/팀 Scope 내에 있는가
 *
 * 그러나 "허용/거부" 등의 접근 정책과 행위 허가 등
 * 도메인 정책을 판정해야 하는 순간 SQL 외부로 꺼내 ResolverService를 활용한다.
 *
 * 도메인 정책 판정, 허용 판정 등이 SQL 내부로 숨어 캡슐화되면
 * 오히려 유지보수가 어려워진다.
 */
```

## 행위에 대한 메서드 명

- repository 레이어를 제외하고 조회/리스팅/생성/수정/삭제는 get/get*s/Create/Modify/Remove 로 명칭한다
- repository 레이어에서 조회/리스팅/생성/수정/삭제는 select/select*s/insert/update/delete 로 명칭한다.
  - 단, 조회 내용이 단순 "존재여부"일 경우 is* 로 명명한다.
- 조회 메서드명은 가능하면 조회 계약을 드러내도록 한다.
  - 단순 식별 조회인지
  - 접근 가능한 자원을 조회하는 것인지
  - 특정 Scope/관계를 전제로 하는 조회인지
  등을 메서드명에서 구분할 수 있도록 한다.

예:
```java
selectFolderById(...)
selectAccessiblePrivateFolder(...)
selectAccessibleTeamEntryFolderByTeamUuid(...)
```

## 명칭 규칙

- 메서드명은 행위와 대상을 표현하도록 선언한다.
- 레이어별 클래스명은 대상 도메인과 해당 레이어의 역할이 드러나도록 선언한다.
- VO명은 객체가 담고 있는 정보와 해당 정보의 성격(Condition, Command, Projection, Response 등)이 드러나도록 선언한다.
  - Draft명은 대상 도메인과 Draft가 발생한 원인 또는 목적이 드러나도록 선언한다.

## ENUM의 활용

- ENUM은 JAVA 영역에서는 ENUM인 상태로 순회한다.
- Repository 계층 진입 시, TypeHandler에게 위임하여 DB에서 표현 가능한 boolean / int / String 값으로 치환한다.

## FE 응답

- 리턴 흐름에서 서비스레벨 -> 컨트롤러 레벨까지는 각각의 DTO/VO로 리턴한다. (서비스의 리턴값을 한눈에 알아볼 수 있도록)
- Controller 레벨에서 HTTP Response(ResponseEntity) 발송 전, 공통응답래퍼로 감싸고 HTTP Response에 넣고 ResponseEntity로 다시 감싼다.
  - 각 API 별로 접근할 수 있는 최상위 필드명을 공통처리해서 혼동을 줄이기 위함
  - 단, 이미 HTTP status code는 ResponseEntity가 가지고 있으므로, 공통응답래퍼는 T result 와 String message, timestamp(api요청시각), code(ENUM, 별도의 추가정보)을 가지도록 한다.
  - 이때, String message는 code의 message값을, code는 enum의 코드값을 활용한다.
- 각 에러 상황 발생 시 apiErrorCode에 선언하고 각 httpStatus를 지정하도록 한다.
- 각 Exception 클래스 종류는 아래와 같은 의미를 가진다.
  - GeneralException : 예측하지 못한, 잡아내지 못한 상황에 대한 Http 응답 시 Exception
  - RequestProcessingException : 주어진 파라미터를 통해 비즈니스 로직을 순회하다 발생한 Exception 중 외부(FE)에 반환해야 할 경우에 사용하는 Exception
  - InternalProcessingException : 주어진 파라미터를 통해 비즈니스 로직을 순회하다 발생한 Exception 중 내부에서 소화하고 응답에 사용하지 않을 Exception

### OffsetBase 리스팅 시 응답

- 실제 응답 정보
- Pagination 정보
  - total 카운트 : 주어진 검색조건에 부합하는 count 개수
  - offset : 실제 적용된 offset 값. FE param과 동일하면 Echo 역할을 수행
  - limit : 실제 적용된 limit 값. FE param과 동일하면 Echo 역할을 수행

### CursorBase 리스팅 시 응답

- 실제 응답 정보
- Cursor/Pagination 정보
  - cursor는 "지난 조회 이후 어디서부터 이어서 조회할 것인지"를 표현하는 위치 식별자다.
  - cursor가 UUID 하나로 표현될 수 있더라도, UUID 자체가 Cursor의 의미와 동일한 것은 아니다.
  - 정렬조건이 복합화되는 경우 cursor는 정렬키 조합 또는 opaque token으로 확장될 수 있다.
  - cursor의 의미가 `lastCursor`인지 `nextCursor`인지 API 계약에서 명확히 고정한다.
  - `lastCursor`를 사용할 경우 현재 응답의 마지막 row를 기준으로 다음 조회에서 그 이후 row를 요청한다.
  - `nextCursor`를 사용할 경우 다음 요청에서 시작해야 할 row를 가리키도록 서버/클라이언트가 동일한 의미를 사용한다.

## Optional의 사용

- "어떠한 연산의 결과"에서 값 없음이 발생할 수 있다면 Optional로 처리한다.
  - 예: count의 0은 결과없음이 아니라 0건이 나온 것임
  - 단건 사용자 조회를 했는데 대상이 존재하지 않음 -> 실제로 "값 없음" 발생. 이 케이스에 적합함
- Optional은 "null이어도 괜찮다"를 의미하는 것이 아니라, 해당 연산 결과에 값의 부재가 발생할 수 있음을 타입으로 명시한다.
- `orElse`, `orElseThrow`, `ifPresent` 등의 결정은 그 Optional을 사용하는 비즈니스 로직에서 처리하도록 한다.
  - 하위 계층에 숨어 있으면 코드를 한눈에 알아보기 힘들고, 실제로 '그 값의 부재가 문제인지, 기본값으로 대체 가능한지'는 비즈니스 레벨에서 결정하기 때문.
- Collection/List 조회는 결과 없음일 때 empty collection을 사용하고 Optional로 감싸지 않는 것을 기본으로 한다.
- `COUNT`, `EXISTS`처럼 값 자체가 결과인 조회는 각각 숫자/boolean으로 반환하고 Optional을 사용하지 않는다.
