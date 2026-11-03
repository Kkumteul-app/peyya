# 페이야 시연 영상 스토리보드 (60–90초)

**목적:** 공모전 1차 가산점 · 접수양식 7번 참고자료  
**권장 길이:** 75초 · 가로 16:9 · 무음 또는 TTS만  
**촬영 소스:** Mock APK (폰/에뮬) 또는 UI Preview (`http://127.0.0.1:8901/`)

## 원라이너 (오프닝 자막)

> 내비는 길을, 페이야는 가족의 다음 장소를 알려준다.

## 샷 구성

| # | 시간 | 화면 | 액션 | 자막 |
|---|------|------|------|------|
| 1 | 0–8s | 앱 홈 + Mock 배너 | 가족·경유지 노출 | Pain: 주말 동선이 앱마다 흩어진다 |
| 2 | 8–20s | 경유지 체크 | 박물관·맛집 선택 유지 | 계획: 원하는 구간만 선택 |
| 3 | 20–32s | 오늘 루트 시작 | 국립중앙박물관 안내 | 실행: NaviHelper / Mock 경로 요청 |
| 4 | 32–42s | 도착 처리 | 단계=도착 | 정차 맥락 |
| 5 | 42–58s | 시동 (다음 안내) | TTS 문구 표시 | **히어로: 시동하면 다음** |
| 6 | 58–72s | 다음 안내 확인 | 맛집 경로 요청 | 재설정 없이 이어짐 |
| 7 | 72–80s | SDK/포지션 카드 | 정지 자막 | Vehicle·Navi·TTS · 주말 동선 OS |

## 촬영 체크

```bat
adb install -r peyya\app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.peyya.app/.MainActivity --ez force_mock true
```

- 가로 모드·한국어 TTS 엔진·음량 ON
- 재촬영: 앱 내 **일정 초기화**
- 파일명 예: `peyya-demo-75s.mp4`

## 산출물 경로

- 스토리보드: `docs/contest/peyya-시연영상-스토리보드.md`
- 데모 클립(미리보기 기반): `docs/contest/peyya-demo-preview.mp4`
