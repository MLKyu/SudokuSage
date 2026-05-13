package com.mingeek.sudokusage.featureflags

@JvmInline
value class FlagKey(val value: String)

/**
 * Plug-point for remote config. Today: local in-memory map. Tomorrow: Firebase
 * Remote Config / your own server, swapped in via [com.mingeek.sudokusage.data.AppContainer].
 *
 * Callers always pass a default — flags are never required to exist.
 */
interface FeatureFlags {
    fun bool(key: FlagKey, default: Boolean = false): Boolean
    fun string(key: FlagKey, default: String = ""): String
    fun int(key: FlagKey, default: Int = 0): Int

    /** Force a fresh fetch from the backing store. NoOp impls return true. */
    suspend fun refresh(): Boolean = true
}

object FlagKeys {
    val EnableHintTrainer = FlagKey("hint_trainer_enabled")
    val EnableKillerVariant = FlagKey("killer_variant_enabled")
    val EnableXVariant = FlagKey("x_variant_enabled")
    val EnableHyperVariant = FlagKey("hyper_variant_enabled")
    val EnableMiniVariants = FlagKey("mini_variants_enabled")
    val EnableDailyChallenge = FlagKey("daily_challenge_enabled")
    val EnableCloudSync = FlagKey("cloud_sync_enabled")
    val EnableAds = FlagKey("ads_enabled")
    val EnableProUpsell = FlagKey("pro_upsell_enabled")
}

class LocalFeatureFlags(
    private val overrides: Map<FlagKey, Any> = emptyMap(),
) : FeatureFlags {
    override fun bool(key: FlagKey, default: Boolean): Boolean =
        overrides[key] as? Boolean ?: default
    override fun string(key: FlagKey, default: String): String =
        overrides[key] as? String ?: default
    override fun int(key: FlagKey, default: Int): Int =
        overrides[key] as? Int ?: default
}
