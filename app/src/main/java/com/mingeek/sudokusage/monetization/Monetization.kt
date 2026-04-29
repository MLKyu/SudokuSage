package com.mingeek.sudokusage.monetization

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Pro entitlement check. UI gates premium-only flows behind [isPro].
 * Today: always false. Tomorrow: backed by Play Billing / a server.
 */
interface EntitlementGate {
    val isPro: StateFlow<Boolean>
    suspend fun refresh()
}

/**
 * Ad surface. Free flavor uses real impl; Pro entitlement should short-circuit calls.
 *
 * Methods that show ads take an [Activity] because the platform SDK (AdMob, etc.)
 * needs an active Activity to render. Callers in Compose obtain it via
 * `LocalContext.current as Activity`.
 */
interface AdProvider {
    /** Returns true iff user actually watched and earned the reward. */
    suspend fun showRewarded(activity: Activity): Boolean
    fun showInterstitialIfReady(activity: Activity)
    fun preload()
}

/**
 * In-app purchases. UI calls these; impl wraps Play Billing.
 *
 * [purchasePro] needs an [Activity] to launch the billing flow. Returns true
 * once the purchase is acknowledged (the Pro entitlement should reflect within
 * a refresh cycle).
 */
interface IapProvider {
    suspend fun purchasePro(activity: Activity): Boolean
    suspend fun restorePurchases()
}

class NoOpEntitlementGate : EntitlementGate {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro
    override suspend fun refresh() { /* nothing to refresh in no-op */ }
}

class NoOpAdProvider : AdProvider {
    override suspend fun showRewarded(activity: Activity): Boolean = false
    override fun showInterstitialIfReady(activity: Activity) { /* no ads in no-op */ }
    override fun preload() { /* no ads in no-op */ }
}

class NoOpIapProvider : IapProvider {
    override suspend fun purchasePro(activity: Activity): Boolean = false
    override suspend fun restorePurchases() { /* nothing */ }
}
