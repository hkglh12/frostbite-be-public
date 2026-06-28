## UnitTask

### 정의

- Workflow 내 단위 작업을 의미
- 파일등록, 파일 다운로드 흐름 내에서 암호화, 복호화, ELS동기화 등 의 각 단위작업을 유닛태스크로 명칭

### UnitTask 기능 정의

- 각 UnitTask를 아래와 같이 정의
  - 암호화 UnitTask
    - V1(1KB 암호화), V2(전문암호화) 로 나누어 파일 암호화를 수행
  - 복호화 UnitTask
    - Client 요청에 따라 파일 활용을 위해 복호화
    - 단, 워크플로우 호출 외, 다운로드 요청은 UnitTask (Async)가 아닌 Sync + Stream 방식으로 처리
  - ELS 동기화 UnitTask
    - 파일 메타데이터 및 원문(문서만 지원) 을 ELS 로 동기화
  - 파일 제거 UnitTask
    - 특히, 업로드 흐름에서 생성된 임시 파일을 업로드 워크플로우 종료시에 삭제하는 Task 
