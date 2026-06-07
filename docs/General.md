## 일반 컨벤션

### 외부 인터페이스에서는 내부 PK를 최대한 노출하지 않으며, 외부 식별이 필요한 경우 UUID 등 별도의 공개 식별자를 사용한다.
- OWASP IDOR(Insecure Direct Object Reference) 및 Broken Access Control 위험을 줄이기 위함
- 모든 조회/수정/삭제 요청은 반드시 서버 측 권한 검증을 수행
- 내부 PK는 DB 식별 및 내부 조인/참조 용도로만 사용하고, 외부 요청 파라미터나 응답에는 직접 노출하지 않는 것을 기본 원칙으로 한다.

### 행위에 대한 메서드 명
- repository 레이어를 제외하고 조회/리스팅/생성/수정/삭제 는 get/get*s/Create/Modify/Remove 로 명칭한다
- repository 레이어에서 조회/리스팅/생성/수정/삭제 는 select/select*s/insert/update/delete 로 명칭한다.
  - 단, 조회 내용이 단순 "존재여부" 일 경우 is* 로 명명한다.

### 명칭 규칙
- 메서드명은 행위와 대상을 표현하도록 선언한다.
- 레이어별 클래스명은 대상 도메인과 해당 레이어의 역할이 드러나도록 선언한다.
- VO명은 객체가 담고 있는 정보와 해당 정보의 성격(Condition, Command, Projection, Response 등)이 드러나도록 선언한다.
    - Draft명은 대상 도메인과 Draft가 발생한 원인 또는 목적이 드러나도록 선언한다.

### ENUM의 활용
- ENUM은 JAVA영역에서는 ENUM인 상태로 순회한다
- Repository 계층 진입 시, TypeHandler에게 위임하여 DB에서 표현 가능한 boolean / int 값으로 치환한다.

### FE 응답
- 리턴 흐름에서 서비스레벨 -> 컨트롤러 레벨까지는 각각의 DTO/VO로 리턴한다. (서비스의 리턴값을 한눈에 알아볼 수 있도록)
- Controller 레벨에서 HTTP Response(ResponseEntity) 발송 전, 공통응답래퍼로 감싸고 HTTP Response에 넣고 ResponseEntity로 다시 감싼다.
  - 각 API 별로 접근할 수 있는 최상위 필드명을 공통처리해서 혼동을 줄이기 위함
  - 단, 이미 HTTP status code는 ResponseEntity가 가지고 있으므로, 공통응답래퍼는 T result 와 String message, timestamp(api요청시각), code(ENUM, 별도의 추가정보)을 가지도록 한다.
  - 이때, String message는 code의 message값을, code는 enum의 코드값을 활용한다.
- 주어진 조건으로 select에 실패했을 경우 notFound* 계열 Exception을 던지고 GlobalExceptionHandler에서 HTTP status 코드값을 적절히 값을 나타내는 코드로 변환, 에러 전용 DTO로 리턴한다.
  - statusCode는 globalExceptionHandler에서도 ResponseEntity를 사용할것이므로 httpstatus code는 제외, message, timestamp(api요청시각), code(ENUM, 별도의 오류정보)을 가지도록 한다.
  - 이때, String message는 code의 message값을, code는 enum의 코드값을 활용한다.