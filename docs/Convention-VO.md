## 객체의 관리 및 VO의 계층이동

### 용어의 설명

- RequestDTO : 수정/생성 계열 사용자 입력, UseCase별로 관리
- Condition : 조회 계열 사용자 입력, 최대한 조회계열 useCase를 단일객체로 처리. 단, 검색목적과 출력의미가 달라지는경우 별도 유스케이스로 간주, 별도 Condition 생성
  - 예시 : 관리자의 비활성화 고객 검색 (Range검색 + status 검색) / 사용자의 본인 계정 상태 확인 (Exact match) 
- Domain Entity : 가변, 행위기반 메서드로 Setter 통제, 도메인 상태표현(.lock(), .deactive()) 혹은 insert 계열 최종형태로 활용
- Command : 불변, 비즈니스 로직 내 변수로 누적정제 후 Update 비즈니스 로직 최종형태, UseCase별로 관리, record 클래스 활용 (jdk21환경)
  - 단순 변경/삭제 일경우 useCase별 xml 활용, service layer에서는 식별자만 전달
  - 복합 변경/삭제일 경우 useCase별 Command 활용
  - Record-Mybatis 활용은 Mybatis^3.5. Record는 getter가 getXXX가 아니라 필드명이 되는데, mybatis ^3.5에서 record Accessor 인식됨
- Projection : 불변, 읽기전용, 단, Class를 활용하며, @NoArgs + @Getter + @Setter 조합 사용 (추후 테스트코드에서 필요하면 @AllArgs 추가). 단 Projection에 대한 코드 상 Setter 호출 금지
  - mybatis가 getInstance + setter 로 처리하는 케이스에 대해 -> Lombok @Value(final임) 활용 불가
  - mybatis xml 내 resultMap 매핑 후 constructor 선언 -> @Value는 쓸수있게되나 constructor 인자 순서가 select와 일치해야하는 제약발생 -> name 선언으로 매핑이 가능하지만 컴파일 인자값이 추가되어야하고 상당히 번거로울 수 있음
  - @NoArgs 기반이면 mybatis가 Setter 기반으로 호출하므로 별도의 순서의존성이 발생하지 않으므로 해당 방식 채용
- ResponseDTO : 사용자 응답 제출 최종형태. UseCase별로 관리
- Draft : 단순 정제는 변수로 처리하되, 여러 단계에 걸쳐서 값이 연동되어 전달됨과 동시에 누적정제가 발생할 때 활용. 이때 Draft는 원본의 상태나 필드를 누적정제하고있다면 원본객체와 연결짓기 위해 식별자 필요

### 비즈니스 플로우 별 각 레이어 진입/진출 시 컨벤션
1. 사용자 요청 -> Controller : RequestDTO/Condition 으로 Accept, Validation 적극활용
2. Controller -> Service : DTO/Condition로 전달, Service 진입 직후 아래의 경우를 활용
   CASE1. 생성 비즈니스 로직, Domain Entity로 전환. 행위기반 메서드명으로 변경 통제. 추가 누적 변경 및 상태누적관리가 필요할경우 Draft로 처리
   CASE2. 수정 비즈니스 로직, 비즈니스 로직 내에서 누적정제는 변수/Draft 적극활용 후 Command로 최종전환
   CASE3. 삭제 비즈니스 로직, 단순삭제일경우 식별자전달, 복합삭제일경우 Command로 전환
   CASE4. 조회 비즈니스 로직, Condition으로 유지
3. Service -> Mapper
   CASE1. Create의 경우 domain entity로 진입
   CASE2. update의 경우 command로 진입
   CASE3. Read/Listing의 경우 condition으로 진입
   CASE4. Delete의 경우 단순 삭제일경우 식별자전달, 복합삭제일경우 command로 진입
4. Mapper -> Service : USECASE에 따라 아래와 같이 따른다
   CASE1. 리스트조회/객체조회 구분 없이 Projection으로 진출. Projection의 경우, UseCase별로 객체를 별도로 준비한다.
5. Service -> Service : 서비스 순회 내 누적변경은 Draft, 단순변경은 변수활용, 헬퍼메서드 등을 활용해 처리.
6. Service -> Controller : 진출 직전에 ResponseDto로 전환. 만약 Draft가 있었다면 Draft를 해당 단계에서 활용, 
7. Controller -> 사용자 : ResponseDto로 제출


### Draft 사용 예시

- 파일업로드 -> 임시경로적재
- 비동기로 우리 스토리지로 이동 ->(1차 변경:신규패스)
- 비동기로 전문검색을 위한 전처리경로 -> (2차변경 : 전처리경로)
- DB에는 전처리 완료된 문서만 저장 (그럴일은없겠지만 예를들면)

- 즉 여러단계에 걸쳐서 MAP으로 관리할 만한 것들을 모두 draft로 관리. (Get 시점 컴파일에러 스킵 방지 및 타입안정성 체크)
- 무분별한 draft 생성 전, 단순정제 -> 지역변수 / Entity 값변경, 상태변경을 통한 흡수 가능 -> entity 활용 / 외부결과 조합 -> factory / 단순 값 변경 -> helper 활용을 우선적으로 생각할것