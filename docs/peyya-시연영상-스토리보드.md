# 페이야 시연 영상 스토리보드 (v3 · ~100초)

**목적:** 공모전 1차 가산점 · 접수양식 7번 참고자료  
**길이:** 약 90–100초 · 가로 1920×1080 · 무음(또는 TTS만)  
**앱:** `peyya/` · APK `peyya/apk/Peyya_Peyya_release.apk` (**versionName 0.1.0 · unsigned release**)  
**갱신:** 2026-11-04 · 축2(리뷰·감성·코스추천) · 예매 · 당일적응 · 음성 시뮬 반영  
**워크스페이스 원본:** `docs/contest/peyya-시연영상-스토리보드.md` (동일 내용)

## 원라이너 (오프닝)

> 내비는 길을, 페이야는 가족의 다음 장소를 알려준다.

## 자산 링크 (최신)

| 구분 | 경로 |
|------|------|
| 제출 APK | `apk/Peyya_Peyya_release.apk` (0.1.0, **unsigned**) · `apk/README.md` |
| 실기기 apk-shots | 워크스페이스 `output/appdesign/2026-11-04/peyya-auto-progress/apk-shots/` |
| Design preview · 축1 진행 | `output/appdesign/2026-11-04/peyya-auto-progress/preview/` |
| Design preview · 축2 리뷰·코스 | `output/appdesign/2026-11-04/peyya-review-recommend/preview/` |
| Dual 셸 참고(구) | `output/appdesign/2026-11-03/peyya-dual/preview/` |
| 시연 mp4 | `peyya-demo-preview.mp4` · `docs/contest/peyya-demo-preview.mp4` |

## 샷 구성

| # | 시간 | 셸 | 화면·기능 | 자막 포인트 |
|---|------|-----|-----------|-------------|
| 1 | 0–8s | Phone | 스플래시 → Idle 홈 | 브랜드·한 줄 약속 |
| 2 | 8–18s | Phone | 오늘 루트 · **예매·준비** 체크리스트 | 박물관 딥링크·「인앱 결제 없음」 |
| 3 | 18–28s | Car | 안내 중 (Navigating) | NaviHelper / Mock 경로 |
| 4 | 28–40s | Car | 도착 · **당일 적응** 패널 | 우회·주차 후보 · 배지 **「시연용/추정」** |
| 5 | 40–52s | Car | 시동 히어로 · TTS 확인 | **시동하면 다음** |
| 6 | 52–62s | Car | **음성 시뮬** 칩 (다음/건너뛰기/유지) | 「음성 시뮬 (시연)」— STT SDK 대신 시뮬 |
| 7 | 62–72s | Car | 맛집 안내 | 재검색 없이 이어짐 |
| 8 | 72–86s | Phone/Car | 완료 → **「오늘 하루는 어땠나요?»** | 장소별 별점+글 · 감성(별점1차+키워드) |
| 9 | 86–100s | Phone/Car | Idle **코스 추천** (M=3·N=5) | 로컬 엔진+시드 · 주말 동선 OS |

## 시연 표기 (필수)

- 혼잡·우회·주차: **시연용/추정** — 실시간 API 아님  
- 음성: **음성 시뮬 (시연)** — 마이크 STT SDK는 로드맵  
- APK: **unsigned** release

## 실기기 재촬영 (권장)

```bat
adb install -r apk\Peyya_Peyya_release.apk
adb shell am start -n com.peyya.app/.MainActivity --ez force_mock true
```

- 폰: 세로 / 차량·에뮬: 가로  
- 재촬영 시 앱 내 **일정 초기화**
