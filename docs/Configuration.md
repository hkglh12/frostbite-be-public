## Configuration 설정 규칙

### mybatis
- SqlSessionFactoryBean 설정 (mapper location, datasource 매핑)은 autoConfiguration + application.properties 설정 유지
- 해당 건 외, TypeHandler 등 Mybatis 전역설정은 JavaConfiguration으로 처리