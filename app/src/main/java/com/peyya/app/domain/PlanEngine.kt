package com.peyya.app.domain

import com.peyya.app.data.SeedCatalog
import com.peyya.app.data.SeedPoi
import java.util.Locale
import java.util.UUID

/**
 * Local seed itinerary builder — vehicle-reachable POIs only, no network/LLM.
 */
object PlanEngine {
    fun suggest(slots: PlanSlots, catalog: List<SeedPoi> = SeedCatalog.pois): List<PlanCandidate> {
        require(slots.ready()) { "destination, companions, timeBand required" }
        val q = normalize(slots.destination + " " + slots.style + " " + slots.timeBand)
        val regionHits = catalog.filter { poi ->
            normalize(poi.region).contains(regionKey(q)) ||
                poi.tags.any { normalize(it) in q || q.contains(normalize(it)) } ||
                normalize(poi.stop.name).let { q.contains(it) || it.contains(regionKey(q)) }
        }.ifEmpty {
            catalog.filter { it.region == inferRegion(q) }
        }.ifEmpty { catalog.take(4) }

        val byRegion = regionHits.groupBy { it.region }
        return byRegion.entries.take(3).mapIndexed { index, (region, pois) ->
            val ordered = orderStops(pois, slots)
            PlanCandidate(
                id = "cand-$region-$index",
                title = titleFor(region, ordered, slots),
                region = region,
                stops = ordered.map { it.stop.copy(selected = true) },
                blurb = blurbFor(slots, ordered),
            )
        }.ifEmpty {
            listOf(fallbackCandidate(slots, catalog))
        }
    }

    fun advanceSlots(session: PlanSession, answer: String): PlanSession {
        val trimmed = answer.trim()
        if (trimmed.isEmpty()) return session
        val slots = when (session.step) {
            0 -> session.slots.copy(destination = trimmed)
            1 -> session.slots.copy(companions = trimmed)
            2 -> session.slots.copy(timeBand = trimmed)
            else -> session.slots.copy(style = trimmed)
        }
        val nextStep = (session.step + 1).coerceAtMost(3)
        if (slots.ready() && (session.step >= 2 || nextStep >= 3)) {
            val withStyle = if (slots.style.isBlank() && session.step >= 3) slots.copy(style = trimmed) else slots
            val ready = if (withStyle.style.isBlank()) withStyle.copy(style = withStyle.timeBand) else withStyle
            return session.copy(
                slots = ready,
                step = 3,
                phase = PlanPhase.Candidates,
                candidates = suggest(ready),
                selectedCandidateId = null,
            )
        }
        return session.copy(slots = slots, step = nextStep, phase = PlanPhase.Collecting)
    }

    /** Popular card → 바로 추천 코스(+지도) 단계로. */
    fun quickFromDestination(destination: String): PlanSession {
        val slots = PlanSlots(
            destination = destination.trim(),
            companions = "가족",
            timeBand = "오후",
            style = "여유롭게",
        )
        return PlanSession(
            slots = slots,
            step = 3,
            phase = PlanPhase.Candidates,
            candidates = suggest(slots),
            selectedCandidateId = null,
        )
    }

    fun backToCollecting(session: PlanSession): PlanSession =
        session.copy(
            phase = PlanPhase.Collecting,
            step = 0,
            candidates = emptyList(),
            selectedCandidateId = null,
            slots = PlanSlots(),
        )

    fun backToCandidates(session: PlanSession): PlanSession =
        session.copy(phase = PlanPhase.Candidates, selectedCandidateId = null)

    fun selectCandidate(session: PlanSession, candidateId: String): PlanSession =
        session.copy(selectedCandidateId = candidateId, phase = PlanPhase.Confirm)

    fun toScheduledRoute(
        candidate: PlanCandidate,
        scheduledAtEpochMs: Long,
    ): SavedRoute = SavedRoute(
        id = "route-${UUID.randomUUID().toString().take(8)}",
        title = candidate.title,
        stops = candidate.stops,
        status = RouteStatus.Scheduled,
        source = RouteSource.PlanEngine,
        region = candidate.region,
        scheduledAtEpochMs = scheduledAtEpochMs,
        note = candidate.blurb,
    )

    private fun orderStops(pois: List<SeedPoi>, slots: PlanSlots): List<SeedPoi> {
        val culture = pois.filter { it.category == StopCategory.Culture || it.category == StopCategory.Park || it.category == StopCategory.Shopping }
        val food = pois.filter { it.category == StopCategory.Food || it.category == StopCategory.Cafe }
        val first = culture.firstOrNull() ?: pois.first()
        val second = food.firstOrNull { it.stop.id != first.stop.id }
            ?: pois.firstOrNull { it.stop.id != first.stop.id }
        val third = when {
            normalize(slots.style).contains("여유") || normalize(slots.timeBand).contains("오후") ->
                pois.firstOrNull { it.category == StopCategory.Cafe && it.stop.id != first.stop.id && it.stop.id != second?.stop?.id }
            else -> null
        }
        return listOfNotNull(first, second, third).distinctBy { it.stop.id }.take(4)
    }

    private fun titleFor(region: String, stops: List<SeedPoi>, slots: PlanSlots): String {
        val focus = stops.firstOrNull()?.category?.let {
            when (it) {
                StopCategory.Culture -> "문화"
                StopCategory.Park -> "피크닉"
                StopCategory.Shopping -> "감성"
                else -> "드라이브"
            }
        } ?: "주말"
        return "$region $focus · ${slots.timeBand.ifBlank { "당일" }} 루트"
    }

    private fun blurbFor(slots: PlanSlots, stops: List<SeedPoi>): String =
        "${slots.companions}과 함께, 차량으로 이어지는 ${stops.size}스톱. ${slots.style.ifBlank { slots.timeBand }}"

    private fun fallbackCandidate(slots: PlanSlots, catalog: List<SeedPoi>): PlanCandidate {
        val picks = catalog.take(2)
        return PlanCandidate(
            id = "cand-fallback",
            title = "추천 주말 루트",
            region = picks.firstOrNull()?.region ?: "용산",
            stops = picks.map { it.stop.copy(selected = true) },
            blurb = "근처 시드 코스로 안내합니다. (${slots.destination})",
        )
    }

    private fun inferRegion(q: String): String = when {
        "성수" in q -> "성수"
        "분당" in q -> "분당"
        "용산" in q || "박물관" in q || "이촌" in q -> "용산"
        else -> "용산"
    }

    private fun regionKey(q: String): String = inferRegion(q).lowercase(Locale.ROOT)

    private fun normalize(raw: String): String =
        raw.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "")
}
