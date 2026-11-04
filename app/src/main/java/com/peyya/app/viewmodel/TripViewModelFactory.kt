package com.peyya.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.peyya.app.progress.InMemoryTripProgressStore
import com.peyya.app.progress.OnTripCompleted
import com.peyya.app.progress.TripProgressStore
import com.peyya.app.service.GatewayFactory
import com.peyya.app.service.SpeechGuide

class TripViewModelFactory(
    private val appContext: Context,
    private val forceMock: Boolean = false,
    private val store: TripProgressStore = InMemoryTripProgressStore(),
    private val onTripCompleted: OnTripCompleted = OnTripCompleted { /* 축2 스텁 */ },
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TripViewModel::class.java)) {
            "Unknown ViewModel ${modelClass.name}"
        }
        val gateways = GatewayFactory.create(appContext, forceMock = forceMock)
        val speech = SpeechGuide(appContext)
        return TripViewModel(
            navi = gateways.navi,
            ignition = gateways.ignition,
            speech = speech,
            store = store,
            mockMode = gateways.mockMode,
            onTripCompleted = onTripCompleted,
        ) as T
    }
}
