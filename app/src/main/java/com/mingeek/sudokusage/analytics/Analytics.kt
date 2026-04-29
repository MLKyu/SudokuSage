package com.mingeek.sudokusage.analytics

/**
 * Backend-agnostic analytics surface. Today: no-op. Tomorrow: Firebase / Amplitude /
 * your own pipe — swapped in [com.mingeek.sudokusage.data.AppContainer] without
 * touching call sites.
 */
interface Analytics {
    fun track(name: String, params: Map<String, Any?> = emptyMap())
    fun setUserProperty(key: String, value: String?)
}

class NoOpAnalytics : Analytics {
    override fun track(name: String, params: Map<String, Any?>) { /* discarded */ }
    override fun setUserProperty(key: String, value: String?) { /* discarded */ }
}
