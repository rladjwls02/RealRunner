# RealRunner

Apple Watch·NRC 러닝 기록을 가져와 기간별 훈련량을 보고, 기준 기록으로 10km 예상 시간을 계산하는 개인 프로젝트.

## 실행

Java 17과 Docker Compose가 필요하다. Docker를 실행한 뒤 프로젝트 루트에서 아래 명령으로 앱을 시작한다. IntelliJ에서는 Gradle 프로젝트로 열고 **RealRunner Dev** 실행 구성을 선택한다.

```sh
./gradlew bootRun
```

기본 프로필 `dev`에서는 `bootRun`이 Compose의 PostgreSQL을 시작한다. Spring은 `127.0.0.1:8080`, PostgreSQL은 `127.0.0.1:5432`를 사용한다. `http://127.0.0.1:8080/actuator/health`에서 앱과 DB가 정상인지 확인할 수 있다. 앱을 종료한 뒤에도 DB는 실행되며, `docker compose stop postgres`로 멈출 수 있다. 데이터는 Docker 볼륨에 남는다.

### 개인 환경에서 정할 값

- **로컬 DB.** 처음 실행할 때 `.env.example`을 `.env`로 복사하고 `DB_PASSWORD`를 설정한다. Compose와 Spring이 함께 읽을 수 있도록 한 줄에 `KEY=value`로 적는다. 비밀번호에는 `openssl rand -hex 24`의 출력처럼 따옴표 없이 쓸 수 있는 값을 사용한다. 계정과 비밀번호는 PostgreSQL 볼륨을 처음 만들 때 적용된다.
- **Git 훅.** 커밋 메시지와 Java 포맷, 푸시 전 빌드·ArchUnit을 검사하려면 `git config --local core.hooksPath .githooks`를 실행한다.
- **홈서버.** 운영용 DB 주소와 자격 증명은 운영 환경에서 정한다. `prod` 프로필에는 `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`가 필요하다. DB 포트가 5432가 아니면 `DB_PORT`도 설정한다. 컨테이너로 실행할 때는 앱 컨테이너가 연결할 수 있는 DB 주소를 지정한다.

`application.yaml`은 공통 설정, `application-dev.yaml`과 `application-prod.yaml`은 환경별 설정을 관리한다. JPA 스키마 자동 생성은 꺼져 있다. Docker 이미지의 기본 프로필은 `prod`다. JAR를 직접 실행하는 운영 환경에서는 `SPRING_PROFILES_ACTIVE=prod`를 지정한다.

### 빌드와 이미지 생성

```sh
./gradlew bootJar spotlessCheck test --tests 'com.example.realrunner.architecture.ArchitectureTest'
docker build -t realrunner:local .
```

현재는 서버 시작이 가능한 초기 골격이다. 데이터 저장과 러닝 기능은 요구사항 검토 및 ERD 작성 이후 붙인다.

## 모듈 경계

한 Spring Boot 앱에 `identity`, `ingestion`, `activity`, `analytics`, `prediction`을 둔다. 각 모듈의 코드는 필요해질 때 모뉴의 `api / web / application / domain / infra` 구조에 맞춰 배치한다. 모듈 간에는 공개 `api`의 계약만 사용한다. 타 모듈의 엔티티나 저장소를 직접 참조하지 않는다. 공통 설정은 필요해질 때 `global`에 둔다.

ArchUnit은 모듈 간 공개 `api` 참조, 계층의 의존 방향, 모듈 간 순환 의존을 검사한다. 새 도메인 모듈을 만들면 `ArchitectureTest`의 모듈 목록에 등록한다. CI는 PR, `dev`·`main` 푸시, 수동 실행에서 Java 17로 패키징, 포맷 검사, 아키텍처 검사를 수행한다.

## 브랜치와 PR

| 구분 | 작업 방식 |
| --- | --- |
| 브랜치 | 최신 `dev`에서 `feature/#12-import-workouts`, `fix/#15-duplicate-import`, `infra/local-setup`처럼 만든다. 긴급 수정도 `hotfix/`에서 작업한 뒤 `dev`를 거쳐 릴리스한다. |
| 커밋 | `feat: 운동 기록 가져오기 #12`처럼 `type: 요약` 형식을 사용한다. 관련 이슈가 있을 때 번호를 붙인다. |
| PR | 작업 브랜치에서 `dev`로 연다. 작업 중에는 Draft로 두고, 연결된 이슈는 본문에 `Closes #12`를 적는다. PR Labeler가 브랜치와 변경 파일을 보고 라벨을 붙인다. |
| 병합 | 최신 `dev`로 rebase하고 CI 통과 후 **Rebase and merge**를 사용한다. 개인 저장소이므로 필수 사람 승인 수는 0이다. |
| 릴리스 | `dev` → `main` PR에서 변경과 CI를 확인한다. 그 PR에서 검토한 `dev` 커밋 SHA를 `REVIEWED_DEV_SHA`에 넣고 아래 명령으로 fast-forward한다. |

```sh
git fetch origin
git merge-base --is-ancestor "$REVIEWED_DEV_SHA" origin/dev &&
  git push origin "$REVIEWED_DEV_SHA:refs/heads/main"
```

CodeRabbit은 일반 `dev` PR을 한국어로 자동 리뷰하도록 설정한다. 처음 연결할 때 GitHub 앱 설치 또는 기존 설치의 저장소 선택에서 **RealRunner**를 지정한다. [CodeRabbit GitHub 앱](https://github.com/apps/coderabbitai)
