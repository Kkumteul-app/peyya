# Peyya APK

| 항목 | 값 |
|------|-----|
| 제출용 (release) | `Peyya_Peyya_release.apk` |
| 휴대폰 설치용 (debug) | `Peyya_ui-refresh_debug-signed.apk` |
| versionName | `0.1.0` |
| versionCode | `1` |
| buildType | release = unsigned / debug = debug-signed |
| 빌드 | `./gradlew.bat :app:assembleDebug :app:assembleRelease` |
| 갱신 | 2026-11-12 · design/ui-refresh (본내비 위젯·코스 테마 추천 포함) |

## 설치 (휴대폰)

```bat
adb install -r apk\Peyya_ui-refresh_debug-signed.apk
adb shell am start -n com.peyya.app/.MainActivity --ez force_mock true
```

서명 키스토어가 없어 release는 unsigned입니다. 실기기 설치는 debug APK를 사용하세요.
