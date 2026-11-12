package com.peyya.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.peyya.app.data.PresetTrips
import com.peyya.app.progress.InMemoryTripProgressStore
import com.peyya.app.progress.OnTripCompleted
import com.peyya.app.progress.TripProgressStore
import com.peyya.app.review.InMemoryReviewStore
import com.peyya.app.review.ReviewSessionStarter
import com.peyya.app.review.ReviewStore
import com.peyya.app.review.ReviewSyncPort
import com.peyya.app.service.GatewayFactory
import com.peyya.app.service.ReviewCompletionNotifier
import com.peyya.app.service.SpeechGuide

class TripViewModelFactory(
    private val appContext: Context,
    private val forceMock: Boolean = false,
    private val store: TripProgressStore = InMemoryTripProgressStore(),
    private val reviewStore: ReviewStore = InMemoryReviewStore(),
    private val reviewSync: ReviewSyncPort = ReviewSyncPort.NoOp,
    private val onTripCompleted: OnTripCompleted? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TripViewModel::class.java)) {
            "Unknown ViewModel ${modelClass.name}"
        }
        val gateways = GatewayFactory.create(appContext, forceMock = forceMock)
        val speech = SpeechGuide(appContext)
        reviewStore.loadSeedIfNeeded()
        val starter = ReviewSessionStarter(reviewStore)
        ReviewCompletionNotifier.ensureChannel(appContext)
        return TripViewModel(
            navi = gateways.navi,
            ignition = gateways.ignition,
            speech = speech,
            store = store,
            mockMode = gateways.mockMode,
            reviewStore = reviewStore,
            reviewSessionStarter = starter,
            reviewSync = reviewSync,
            courseCatalog = PresetTrips.courseCatalog,
            appContext = appContext.applicationContext,
            onTripCompleted = onTripCompleted,
        ) as T
    }
}
