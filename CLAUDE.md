# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트

Aide — 「업무 비서」 Android 앱. applicationId·패키지 루트 `com.giwon.aide`, GitHub `2giwon/Aide-Android`.
현재 1-a(AI 없이 폰 혼자 쓰는 기본 기능) 단계. 1-b에서 `core:ai`(LLM) 모듈과 첫 화면 아바타가 붙는다.

## 설계 문서 (repo 밖)

설계·결정·계획은 `~/Documents/personal-docs/Aide-Android/` 에 있다. **새 세션은 코드보다 먼저 여기를 읽는다.**

1. `handover/` 최신 핸드오프 — 지금 어느 Task 인지, 미결 사항
2. `decisions.md` — 확정된 결정(다시 묻지 않는다)
3. `terminology.md` — 용어(일정·할 일·완료 확인·리마인더·아바타 등)
4. `specs/2026-10-12-aide-1a-design.md` — 1-a 설계
5. `plans/2026-10-12-aide-1a-plan.md` — 1-a 구현 계획(18 Task)
6. `guides/task-NN-*.md` — Task별 가이드

## 작업 방식

- **1-a 코드는 사용자가 직접 짠다.** AI는 Task 가이드(`guides/`)와 코드 리뷰만 한다. 가이드에는 완성 코드를 넣지 않는다 — 파일·시그니처·TDD 케이스 표·순서와 이유·막히는 곳만.
- 예외: **Task 5·6(캘린더 이식)은 AI가 짠다.** 주변 코드 스타일에 맞추고, 불필요한 주석·과한 추상화·방어 코드를 넣지 않는다.
- 디자인 제안 시 관습적 todo UI(목록+체크박스+추가 버튼)를 출발점으로 삼지 않는다. AI가 "완료되었나요?"라고 묻고 사용자가 답하는 구조가 전제다.

## 명령

```bash
./gradlew assembleDebug                      # 디버그 빌드
./gradlew testDebugUnitTest                  # 단위 테스트
./gradlew :app:testDebugUnitTest --tests "com.giwon.aide.ExampleUnitTest"   # 단일 테스트 클래스
./gradlew connectedDebugAndroidTest          # 기기/에뮬레이터 계측 테스트
./gradlew lint                               # Android Lint
```

빌드 환경: AGP 9.2.1 · Kotlin 2.2.10 · Compose BOM · Version Catalog(`gradle/libs.versions.toml`) · configuration cache 켜짐. R8 keep 규칙은 `app/src/main/keepRules/` (AGP 9 방식, `proguard-rules.pro` 아님).

## 아키텍처

현재는 Android Studio 템플릿(`:app` 단일 모듈)이다. 아래는 계획된 목표 구조이며 Task 1(컨벤션 플러그인)·Task 2(모듈 골격)에서 만들어진다.

```
app ──┬─> feature:home ─────┐
      ├─> feature:calendar ─┼─> core:data ──┬─> core:database (Room: 할 일)
      └─> feature:settings ─┘               ├─> core:calendar (폰 캘린더 CalendarContract)
             └─> core:designsystem          └─> core:model (순수 Kotlin)
app ─> core:notification (리마인더·08:30 알림, core:data 사용)
```

- `build-logic` 컨벤션 플러그인 ID `aide.android.*` (`application`·`library`·`feature`·`compose`·`hilt`·`room`·`junit`)
- 스택: Hilt · Room · DataStore · Coroutines/Flow · Navigation 3 · kotlinx.serialization · WorkManager · App Startup · Timber
- 테스트: JUnit5 · MockK · Turbine · Konsist(아키텍처 테스트, CI에서 실패 처리)
- `domain`(유스케이스) 모듈은 두지 않는다. 여러 Repository를 엮는 로직이 생길 때 추가한다.
- Repository 함수는 화면 전용 모양이 아니라 **동작 단위**(만들기·지우기·완료·완료 취소)로 둔다 — 1-b의 AI 함수가 그대로 부른다.

### 의존 규칙 (Konsist로 검사)

1. `feature:*` 끼리 의존하지 않는다. 화면 이동은 `app` 이 연결한다.
2. `core:model` 은 Android 의존이 없다.
3. `feature:*` 는 Room·`CalendarContract` 를 직접 쓰지 않고 `core:data` Repository만 쓴다.

### 화면 규칙

- `XxxRoute`(ViewModel 연결) / `XxxScreen`(순수 UI) 분리
- `XxxUiState` 하나를 `StateFlow` 로, `collectAsStateWithLifecycle()` 로 수집
- `XxxContract { Event, Effect }` 로 사용자 동작과 일회성 효과를 나눈다. 모델 `Event`(일정)와 이름이 겹치므로 항상 `HomeContract.Event` 처럼 바깥 이름을 붙여 쓴다
- 화면은 Compose. 이식한 캘린더의 커스텀 View·Fragment만 예외

### 제약

- **인터넷 권한을 넣지 않는다**(1-a는 네트워크 없음)
- 한국어만 · minSdk 26
- 시간은 저장·계산 모두 `Instant`, 화면 표시와 "오늘" 판정에서만 `ZoneId` 로 바꾼다
- 반복 일정은 `Instances` 로 읽고, 알람 키는 (eventId, 회차 시작, 알림 시점). 예약 알람은 가까운 순으로 최대 300개
- Hilt 2.59+ 는 AGP 9 가 필요하다. KSP 2.3.0부터 KSP 버전이 Kotlin과 분리됐다(Kotlin↔KSP 짝 규칙 무효)
- 커밋 메시지는 제목 한 줄
