package com.uruz.frostbite.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class IntegrationTestSupport {

    /**
        테스트 코드 전체 리팩터링 (CODEX, AccessControlServiceTest.java 작성 시점)

        원칙 (Codex 조언을 통한 Test 코드 개념 학습)
            하나의 테스트는 하나의 행동과 명확한 실패 이유를 가진다.

        Given
            "현재 When" 을 돌리기 위해 요구되는 종속 데이터를 준비하는 과정.
            When 실행 직전까지의 과정중의 실패로 인해 현재 검증하려는 When이 영향받는걸 막기 위해 독립적으로 준비해야함.

            따라서, 서비스 통합 테스트에서는 원칙적으로 테스트 대상이 아닌 비즈니스 메서드 호출로 Given을 만들지 않으며,
            서비스 통합 테스트에서는 Fixture를 통해 DB에 직접 삽입하고, 단위 테스트에서는 객체 생성 또는 Mock의 반환값을 설정한다.

            * 단, 직접 생성해 넣을때에도 도메인 규칙을 깨지 않도록 조심해야 한다.
        When
            진짜 이 테스트 메서드가 검증하려는 비즈니스 메서드를 호출한다.
            일반적으로 테스트 대상 public 메서드 하나를 한번 호출한다.
        Then
            결과 확인을 위해 다른 비즈니스 메서드에 의존하지 않는다. 그 비즈니스 메서드에 버그가 있으면 버그가 난 상태를 신뢰하게 되기 때문.
            즉, 검증 조회 메서드를 강제로 "신뢰함" 전제 상태가 된다. 이건 피해야 한다.

            ArgumentCaptor는 Mock 협력 객체에 올바른 값이 전달되었는지 검증할 때 사용한다. ##Acl 단위테스트 참고

        When & Then
            예외 발생 자체가 결과인 경우 실행과 검증을 하나의 표현으로 작성할 수 있다.
            코드 표현은 합칠 수 있지만 실행과 검증의 개념적 역할은 구분한다.

        예외원칙

            회원가입처럼 “사용자 + 기본폴더 + ACL 생성” 자체가 하나의 계약이면 그 테스트에서는 전부 같이 확인해도 된다.
     * */

    /**
        테스트 코드 잘 썼나 궁금할때는 이 질문을 해보자.
            이 테스트가 실패하면 원인을 검증 대상 행동으로 좁힐 수 있는가?

            여러 결과를 검증하고 있다면, 그것들은 정말 하나의 비즈니스 계약인가?
     * */
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected TestDataFixture fixture;
    protected TestDataQuerySupport query;

    @BeforeEach
    void setUpTestSupport() {
        fixture = new TestDataFixture(jdbcTemplate);
        query = new TestDataQuerySupport(jdbcTemplate);
    }
}
