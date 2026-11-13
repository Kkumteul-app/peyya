package com.peyya.app.domain

enum class RouteStatus {
    Draft,
    Scheduled,
    Active,
    Done,
    Cancelled,
}

enum class RouteSource {
    PlanEngine,
    Community,
    Preset,
}

enum class StopCategory {
    Culture,
    Food,
    Cafe,
    Park,
    Shopping,
}

data class VehicleProfile(
    val id: String = "vehicle-1",
    val nickname: String = "우리 차",
    val typeHint: String = "SUV",
    val plate: String = "",
    val modelName: String = "",
)

data class PlacePins(
    val homeLabel: String = "집",
    val homeAddress: String = "",
    val workLabel: String = "회사",
    val workAddress: String = "",
)

data class AuthProfile(
    val email: String = "",
    val loggedIn: Boolean = false,
)

data class SavedRoute(
    val id: String,
    val title: String,
    val stops: List<Stop>,
    val status: RouteStatus,
    val source: RouteSource,
    val region: String = "",
    val scheduledAtEpochMs: Long? = null,
    val completedAtEpochMs: Long? = null,
    val note: String = "",
) {
    fun toTripPlan(): TripPlan = TripPlan(id = id, title = title, stops = stops.map { it.copy(selected = true) })
}

data class CommunityComment(
    val id: String,
    val authorLabel: String,
    val body: String,
    val createdAtEpochMs: Long,
    val imageModel: String? = null,
)

data class CommunityRoute(
    val id: String,
    val region: String,
    val title: String,
    val stops: List<Stop>,
    val authorLabel: String,
    val likes: Int,
    val likedByMe: Boolean,
    val satisfactionNote: String,
    val comments: List<CommunityComment>,
    val heroTone: StopCategory = StopCategory.Culture,
    /** Experience review cover photo (asset path or null). */
    val reviewImageModel: String? = null,
)

enum class PlanPhase {
    Collecting,
    Candidates,
    Confirm,
}

data class PlanSlots(
    val destination: String = "",
    val companions: String = "",
    val timeBand: String = "",
    val style: String = "",
) {
    fun ready(): Boolean =
        destination.isNotBlank() && companions.isNotBlank() && timeBand.isNotBlank()
}

data class PlanCandidate(
    val id: String,
    val title: String,
    val region: String,
    val stops: List<Stop>,
    val blurb: String,
)

data class PlanSession(
    val slots: PlanSlots = PlanSlots(),
    val phase: PlanPhase = PlanPhase.Collecting,
    val step: Int = 0,
    val candidates: List<PlanCandidate> = emptyList(),
    val selectedCandidateId: String? = null,
)
