# 페이야 시연 영상 스토리보드 (v2 · ~80초)

**목적:** 공모전 1차 가산점 · 접수양식 7번 참고자료  
**길이:** 약 80초 · 가로 1920×1080 · 무음  
**소스:** Phone + Car Navi 이중 UI 목업 (`output/appdesign/2026-11-03/peyya-dual/`)

## 원라이너 (오프닝)

> 내비는 길을, 페이야는 가족의 다음 장소를 알려준다.

## 샷 구성

| # | 시간 | 셸 | 화면 | 자막 포인트 |
|---|------|-----|------|-------------|
| 1 | 0–8s | Phone | 스플래시 | 브랜드·한 줄 약속 |
| 2 | 8–20s | Phone | 오늘 루트 홈 | 가족·스톱 카드 선택 |
| 3 | 20–32s | Car | 안내 중 | NaviHelper / Mock 경로 |
| 4 | 32–46s | Car | 도착 · 시동 히어로 | **시동하면 다음** |
| 5 | 46–60s | Car | TTS 확인 | 음성으로 다음 장소 |
| 6 | 60–72s | Car | 맛집 안내 | 재검색 없이 이어짐 |
| 7 | 72–82s | Car | 완료 | 주말 동선 OS |

## 산출물

- `docs/contest/peyya-demo-preview.mp4`
- `peyya/docs/peyya-demo-preview.mp4` (GitHub 동봉)
- 프레임: `docs/contest/demo-frames-v2/`

## 실기기 재촬영 (권장)

```bat
adb install -r peyya\apk\Peyya_Peyya_release.apk
adb shell am start -n com.peyya.app/.MainActivity --ez force_mock true
```

- 폰: 세로 / 차량·에뮬: 가로  
- 재촬영 시 앱 내 **일정 초기화**
