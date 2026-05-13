package com.mingeek.sudokusage.featureflags

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Firebase Remote Config-backed [FeatureFlags]. SDK ships with an offline
 * cache, so reads after the first successful fetch survive cold starts even
 * without network. Until the first fetch completes, every read falls through
 * to the caller-provided default (the SDK also serves baked-in defaults if
 * [DEFAULTS] is registered — duplicated here for safety against the
 * "STATIC" source).
 */
class RemoteConfigFeatureFlags(
    private val remoteConfig: FirebaseRemoteConfig,
    fetchIntervalSeconds: Long = DEFAULT_FETCH_INTERVAL_SEC,
) : FeatureFlags {

    init {
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(fetchIntervalSeconds)
                .build()
        )
        remoteConfig.setDefaultsAsync(DEFAULTS)
    }

    /** Fetches and activates the latest config. Idempotent. */
    override suspend fun refresh(): Boolean = try {
        remoteConfig.fetchAndActivate().await()
    } catch (e: Exception) {
        Log.w(TAG, "Remote Config fetch failed: ${e.message}")
        false
    }

    override fun bool(key: FlagKey, default: Boolean): Boolean {
        val v = remoteConfig.getValue(key.value)
        return if (v.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) default else v.asBoolean()
    }

    override fun string(key: FlagKey, default: String): String {
        val v = remoteConfig.getValue(key.value)
        return if (v.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) default else v.asString()
    }

    override fun int(key: FlagKey, default: Int): Int {
        val v = remoteConfig.getValue(key.value)
        return if (v.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) default else v.asLong().toInt()
    }

    private companion object {
        const val TAG = "RemoteConfigFlags"
        // 12h: long enough that the SDK doesn't hammer the network, short
        // enough that a same-day Console change reaches users by next session.
        const val DEFAULT_FETCH_INTERVAL_SEC = 12L * 60 * 60

        // Defaults baked into the SDK so flags resolve even before the first
        // remote fetch completes. Mirror the legacy LocalFeatureFlags posture:
        // everything that was true-by-default in code stays that way until the
        // Console says otherwise.
        val DEFAULTS: Map<String, Any> = mapOf(
            FlagKeys.EnableHintTrainer.value to true,
            FlagKeys.EnableKillerVariant.value to false,
            FlagKeys.EnableXVariant.value to true,
            FlagKeys.EnableHyperVariant.value to true,
            FlagKeys.EnableMiniVariants.value to true,
            FlagKeys.EnableDailyChallenge.value to true,
            FlagKeys.EnableCloudSync.value to true,
            FlagKeys.EnableAds.value to false,
            FlagKeys.EnableProUpsell.value to true,
        )
    }
}
