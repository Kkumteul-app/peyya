# Peyya (페이야)

Hyundai HMG Pleos Connect 공모전 MVP — **주말 멀티스톱 패밀리 일정 OS**.

국립중앙박물관 → 인근 맛집(데모 POI) 프리셋 루트를 체크박스로 고르고,  
`NaviHelper.requestRoute`로 안내를 시작하며, **시동 시뮬** 후 TTS로 다음 목적지를 제안합니다.

- **앱 이름:** 페이야 (Peyya)
- **한 줄:** 내비는 길을, 페이야는 가족의 다음 장소를 알려준다.
- **패키지:** `com.peyya.app`

## 실행 방법

```bat
gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.peyya.app/.MainActivity --ez force_mock true
```

## 테스트 환경

- JDK 17, Android compileSdk 35 / minSdk 28
- Mock 모드: Pleos NaviHelper 초기화 실패 시 자동 (폰 데모·영상용)
- Pleos 실연동: Pleos Connect Emulator + Maven `nexus-playground.pleos.ai`

## APK 위치

- 공모전 제출용: [`apk/Peyya_Peyya_release.apk`](apk/Peyya_Peyya_release.apk)
- 로컬 빌드: `app/build/outputs/apk/debug/app-debug.apk`

## 요구 사항

- JDK 17
- Android SDK (compileSdk 35, minSdk 28)
- Gradle Wrapper 포함 (`gradle-8.10.2`)

## 빌드

```bash
cd peyya
./gradlew assembleDebug
```

Windows:

```bat
cd peyya
gradlew.bat assembleDebug
```

성공 시 APK:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Mock 모드 (폰 데모 / 영상 촬영)

Pleos 에뮬레이터가 없거나 `NaviHelper.initialize()`가 실패하면 **자동으로 Mock**으로 전환합니다.

- Mock: `requestRoute` / 시동을 Logcat에 기록하고 UI·TTS 플로우는 그대로 동작
- 화면 상단 **Mock 모드** 배너 표시
- 강제 Mock: Activity intent extra `force_mock=true`

```bash
adb shell am start -n com.peyya.app/.MainActivity --ez force_mock true
```

### Mock 데모 시나리오 (녹화용)

1. 앱 실행 → Mock 배너 확인
2. 가족 이름 확인, 경유지 체크박스 유지
3. **오늘 루트 시작** → 국립중앙박물관 안내(로그)
4. **도착 처리**
5. **시동 (다음 안내)** → TTS: 다음 맛집 제안 (도착 후에만 활성)
6. **다음 안내 확인** → 맛집 `requestRoute`(로그)
7. 재촬영 시 **일정 초기화** 또는 앱 재실행

## Pleos 에뮬레이터

1. Pleos Playground Maven에서 SDK 해석 가능해야 함  
   `https://nexus-playground.pleos.ai/repository/maven-releases/`
2. 의존성
   - `ai.pleos.playground:NaviHelper:3.0.5`
   - `ai.pleos.playground:Vehicle:2.3.2` (시동은 UI 시뮬 + 안전 초기화)
3. Pleos Connect 에뮬레이터/차량 환경에서 실행하면 NaviHelper 실연동 시도
4. 권한: `pleos.car.permission.NAVI_*` (매니페스트에 선언)

## 데모 POI

| 스톱 | 이름 | lat / lng | 비고 |
|------|------|-----------|------|
| 1 | 국립중앙박물관 | 37.52386, 126.98047 | 근사 좌표 |
| 2 | 용산 한식 담소 (데모) | 37.53120, 126.96580 | 인근 가상 POI (예약 API 없음) |

## 구조

```
peyya/
  app/src/main/java/com/peyya/app/
    MainActivity.kt
    ui/PeyyaApp.kt, TripScreen.kt, theme/
    domain/ FamilyProfile, Stop, TripPlan, TripPhase
    data/PresetTrips.kt
    service/ NaviGateway, Pleos/Mock, Ignition*, SpeechGuide
    viewmodel/TripViewModel.kt
```

## 빌드 결과

`gradlew.bat assembleDebug` — **BUILD SUCCESSFUL** (JDK 17, Android SDK Platform 35).

APK: `app/build/outputs/apk/debug/app-debug.apk`

참고:

- 워크스페이스 경로에 한글이 있어 `gradle.properties`에 `android.overridePathCheck=true` 설정
- 최초 빌드 시 SDK Platform 35가 없으면 Gradle이 자동 설치할 수 있음
- Pleos Maven (`nexus-playground.pleos.ai`) 접근 필요 (NaviHelper / Vehicle AAR)
