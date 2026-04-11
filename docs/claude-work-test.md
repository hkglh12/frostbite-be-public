# Claude 작업 기록 - 통합 테스트 작성

**timestamp**: 2026-04-11

---

## 1. 요구사항 (What you asked)

> "User 가입과 조회쪽만 완성한 상태야. 이걸 Springboot test를 활용해서 통합테스트로 UUID가 test-1-1인 계정을 만들고 조회하는 테스트 코드를 작성해줘."

- Spring Boot 통합 테스트 작성
- "test-1-1" 식별자를 가진 계정을 **가입(POST /api/user/)** 하고
- 생성된 계정을 **UUID로 조회(GET /api/user/)** 하는 시나리오 검증

---

## 2. 생성된 파일

| 파일 | 역할 |
|---|---|
| `src/test/java/com/uruz/frostbite/integration/UserIntegrationTest.java` | 통합 테스트 본체 |
| `src/test/resources/application-test.properties` | 테스트 전용 설정 (Redis 세션 비활성화) |

---

## 3. 설계 결정 (How it was designed)

### 3-1. UUID "test-1-1" 문제

`User.uuid` 필드는 `java.util.UUID` 타입이며, 서비스에서 `UUID.randomUUID()` 로 자동 생성된다.  
`"test-1-1"` 은 UUID 포맷(예: `550e8400-e29b-41d4-a716-446655440000`)이 아니기 때문에  
직접 주입하면 `IllegalArgumentException` 이 발생한다.

**해결 전략**: 가입 POST 응답의 `$.result.uuid` 값을 추출해 조회 요청에 그대로 사용.  
테스트 계정의 식별자로는 `name = "test-1-1"`, `email = "test-1-1@test.com"` 을 사용.

### 3-2. 테스트 격리 전략 — `@Transactional`

`@SpringBootTest(webEnvironment = MOCK)` 환경에서는 MockMvc 호출이 **동일 스레드**에서 처리된다.  
따라서 테스트 클래스에 `@Transactional` 을 선언하면 각 테스트 메서드가 하나의 트랜잭션 안에서 실행되고,  
메서드 종료 시 자동으로 **롤백**되어 테스트 데이터가 DB에 남지 않는다.

> **주의**: PostgreSQL 시퀀스(`nextval`)는 트랜잭션 롤백 대상이 아니므로  
> `user_id` 는 테스트 실행마다 증가한다. 기능 동작에는 영향 없음.

### 3-3. Redis 세션 비활성화

운영 설정(`application.properties`)에서 `spring.session.store-type=redis` 로 Spring Session이 Redis에 연결된다.  
통합 테스트는 세션이 필요 없으므로 `application-test.properties` 에서 `store-type=none` 으로 오버라이드.  
`@TestPropertySource` 로 이 파일을 명시적으로 로딩한다.

### 3-4. 테스트 케이스 구성

| 테스트 이름 | 시나리오 |
|---|---|
| `join_test1_1_returns_created_user_info` | POST 가입 요청 → 응답에 uuid/name/email/phoneNumber 검증 |
| `join_then_queryByUuid_returns_same_user` | POST 가입 → uuid 추출 → GET 조회 → 동일 정보 반환 검증 |

### 3-5. 검증 포인트

- HTTP 상태: `200 OK`
- `$.message`: `"SUCCEED"` (`SucceedApiResponseCode.SUCCEED` → `ApiResponse.message`)
- `$.result.*`: name, email, phoneNumber, uuid 값 일치 여부

---

## 4. 사용한 기술 스택

| 기술 | 용도 |
|---|---|
| `@SpringBootTest(webEnvironment = MOCK)` | 실제 서블릿 컨테이너 없이 전체 애플리케이션 컨텍스트 로드 |
| `@AutoConfigureMockMvc` | MockMvc 자동 설정 |
| `MockMvc` | HTTP 레이어 시뮬레이션 (실제 소켓 없이 컨트롤러 호출) |
| `@Transactional` | 테스트 메서드 후 자동 롤백으로 DB 격리 |
| `@TestPropertySource` | 테스트 전용 프로퍼티 오버라이드 |
| `ObjectMapper` | JSON 직렬화 / 응답 파싱 (`jsonPath` 추출) |
| `jsonPath` (MockMvc ResultMatcher) | JSON 응답 필드 단위 검증 |
| `andDo(print())` | 테스트 실패 시 요청/응답 전체를 콘솔 출력 (디버깅용) |

---

## 5. 테스트 실행 전제 조건

- **PostgreSQL** (localhost:9413, DB: frostbite) Docker 컨테이너 실행 중
- `USER_TB` 테이블 및 `user_tb_user_id_seq` 시퀀스 생성 완료 (`phase1_inuse.sql` 실행 필요)
- **Redis** 컨테이너는 테스트 설정에서 세션을 비활성화하므로 미실행 상태도 허용됨

---

## 6. 학습 포인트

1. **통합 테스트 vs 단위 테스트**: 이 테스트는 Controller → Service → MyBatis → PostgreSQL 전 레이어를 실제로 통과한다. Mapper를 Mock하지 않으므로 SQL까지 함께 검증된다.

2. **`webEnvironment = MOCK` vs `RANDOM_PORT`**: `MOCK`은 실제 HTTP 포트를 열지 않고 Spring MVC 레이어를 서블릿 컨테이너 없이 실행한다. `@Transactional` 롤백이 MockMvc 호출에도 적용되는 것이 이 방식의 핵심 장점이다.

3. **UUID 추출 패턴**: 서버가 생성한 리소스 ID를 이후 요청에 재사용하는 패턴은 Create → Read 통합 테스트의 표준적인 접근이다. `objectMapper.readTree(responseBody).path("result").path("uuid").asText()` 로 간결하게 추출 가능.

4. **`@TestPropertySource` 활용**: 운영 설정과 테스트 설정을 분리하는 가장 명시적인 방법. `properties` 인라인 방식보다 파일로 관리하면 여러 테스트 클래스에서 재사용할 수 있다.
