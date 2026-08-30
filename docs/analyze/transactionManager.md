> Reference : https://docs.spring.io/spring-framework/docs/6.2.6/javadoc-api/org/springframework/transaction/support/TransactionSynchronization.html?utm_source=chatgpt.com#afterCommit()

## TransactionSynchronization

- interface,
- transaction synchronization callback에 사용
- orderedInterface 안쓰면 단순히 끝자락에 append


### #afterCommit()
- 메인 트랜잭션이 commit되면 -> 직후에 후작업을 수행
- 트랜잭션이 커밋됬더라도, transactional Resources는 여전히 활성상태 일 수 있음 (javadoc에서도 might still-).
  - 이 시점에 db에 접근하면 자원이 아직 bind()된 상태가 되어있을 수 있으므로 -> afterCommit() 시점에 DB에 접근하는 행위 자체가 원본 트랜잭션 리소스에 엮여있는 상태가 됨 (커밋은 완료됐지만 자원 릴리즈가 안된상태라고 보면됨).
  - 이렇게되면 신규 DB 요청이 구 트랜잭션 리소스에 엮일 위험이 있음. 결과적으로, 이미 그 트랜잭션에서는 commit() 이 날아간 뒤라서, 신규 db 요청이 commit()이 안날아가는 불상사가 발생할수있음
  - 방지하려면 명시적으로 별도 트랜잭션으로 동작함을 명시할것 (@Transactional(propagation=requires_new))


### Transactional Resources?
    - DataSource -> JDBC Connection -> mybatis sqlsessions...
    - 즉 현재 스레드에 바인드된 DB conn


### afterCommit()의 처리?
    > References : 
        TransactionExecutionListener.afterCommit() -> find call hierarchy
        -> 
            AbstractPlatformTransactionManager.java#processCommit()

- 코드를 보면, commit과 triggerAfterCommit() 이 한개 메서드에서 호출
  @Transactional target 메서드 return
  → Transaction Proxy 후처리
  → DB commit
  → afterCommit()
  → cleanup
  → Proxy 호출 종료
  → caller로 return
- 따라서, afterCommit()에서 throw날리면 caller에 전파할 수는 있음.