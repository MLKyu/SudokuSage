package com.mingeek.sudokusage.ui.screens.pro

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mingeek.sudokusage.monetization.EntitlementGate
import com.mingeek.sudokusage.monetization.IapProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface ProEvent {
    data object PurchaseSuccess : ProEvent
    data object PurchaseFailed : ProEvent
    data object Restored : ProEvent
}

class ProViewModel(
    private val iapProvider: IapProvider,
    private val entitlementGate: EntitlementGate,
) : ViewModel() {

    val isPro: StateFlow<Boolean> = entitlementGate.isPro

    private val _events = MutableSharedFlow<ProEvent>(
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: Flow<ProEvent> = _events.asSharedFlow()

    fun purchase(activity: Activity) {
        viewModelScope.launch {
            val ok = iapProvider.purchasePro(activity)
            entitlementGate.refresh()
            _events.tryEmit(if (ok) ProEvent.PurchaseSuccess else ProEvent.PurchaseFailed)
        }
    }

    fun restore() {
        viewModelScope.launch {
            iapProvider.restorePurchases()
            entitlementGate.refresh()
            _events.tryEmit(ProEvent.Restored)
        }
    }
}
