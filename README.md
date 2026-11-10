# RealRunner

Apple Watch·NRC 러닝 기록을 가져와 기간별 훈련량을 보고, 기준 기록으로 10km 예상 시간을 계산하는 개인 프로젝트.

## 실행

Java 17을 사용한다. Spring Boot 4.1.2와 Gradle Wrapper는 다운로드의 Spring Initializr 프로젝트에서 가져왔다.

```sh
./gradlew bootRun
./gradlew bootJar spotlessCheck test --tests 'com.example.realrunner.architecture.ArchitectureTest'
```

현재는 서버 시작이 가능한 초기 골격이다. 데이터 저장과 러닝 기능은 요구사항 검토 및 ERD 작성 이후 붙인다.

## 모듈 경계

한 Spring Boot 앱에 `identity`, `ingestion`, `activity`, `analytics`, `prediction`을 둔다. 각 모듈의 코드는 필요해질 때 모뉴의 `api / web / application / domain / infra` 구조에 맞춰 배치한다. 모듈 간에는 공개 `api`의 계약만 사용한다. 타 모듈의 엔티티나 저장소를 직접 참조하지 않는다. 공통 설정은 필요해질 때 `global`에 둔다.

ArchUnit은 모듈 간 공개 `api` 참조, 계층의 의존 방향, 모듈 간 순환 의존을 검사한다. 새 도메인 모듈을 만들면 `ArchitectureTest`의 모듈 목록에 등록한다. CI는 PR, `main` 푸시, 수동 실행에서 Java 17로 패키징, 포맷 검사, 아키텍처 검사를 수행한다.
