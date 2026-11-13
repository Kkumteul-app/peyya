package com.peyya.app.domain

/**
 * Vehicle plate lookup. Real MOLIT/partner APIs need owner consent + approval.
 * Swap [MockVehicleLookup] for a real implementation later.
 */
data class VehicleLookupResult(
    val plate: String,
    val modelName: String,
    val typeHint: String,
    val yearHint: String,
    val source: Source,
) {
    enum class Source { Mock, Api }
}

fun interface VehicleLookupPort {
    fun lookup(plate: String): VehicleLookupResult?
}

object MockVehicleLookup : VehicleLookupPort {
    private val seeds = listOf(
        Triple("12가", "쏘렌토", "SUV"),
        Triple("34나", "아이오닉5", "전기"),
        Triple("56다", "카니발", "SUV"),
        Triple("78라", "그랜저", "세단"),
        Triple("90마", "팰리세이드", "SUV"),
    )

    override fun lookup(plate: String): VehicleLookupResult? {
        val normalized = plate.replace(" ", "").replace("-", "")
        if (normalized.length < 4) return null
        val seed = seeds[normalized.hashCode().mod(seeds.size).let { if (it < 0) it + seeds.size else it }]
        return VehicleLookupResult(
            plate = normalized,
            modelName = seed.second,
            typeHint = seed.third,
            yearHint = "2022",
            source = VehicleLookupResult.Source.Mock,
        )
    }
}
