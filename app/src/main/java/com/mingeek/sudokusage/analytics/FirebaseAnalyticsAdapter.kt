package com.mingeek.sudokusage.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

class FirebaseAnalyticsAdapter(context: Context) : Analytics {

    private val firebase: FirebaseAnalytics = Firebase.analytics.also {
        it.setAnalyticsCollectionEnabled(true)
        // Touch the context so DI graphs that pass an Application reference
        // keep the dependency visible at construction time.
        context.applicationContext
    }

    override fun track(name: String, params: Map<String, Any?>) {
        firebase.logEvent(sanitizeName(name, MAX_EVENT_NAME_LEN), params.toBundle())
    }

    override fun setUserProperty(key: String, value: String?) {
        firebase.setUserProperty(
            sanitizeName(key, MAX_PROPERTY_KEY_LEN),
            value?.take(MAX_PROPERTY_VALUE_LEN),
        )
    }

    private fun Map<String, Any?>.toBundle(): Bundle = Bundle().apply {
        for ((rawKey, value) in this@toBundle) {
            val key = sanitizeName(rawKey, MAX_PARAM_KEY_LEN)
            when (value) {
                null -> putString(key, null)
                is String -> putString(key, value.take(MAX_PARAM_STRING_LEN))
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Short -> putLong(key, value.toLong())
                is Byte -> putLong(key, value.toLong())
                is Float -> putDouble(key, value.toDouble())
                is Double -> putDouble(key, value)
                is Boolean -> putLong(key, if (value) 1L else 0L)
                else -> putString(key, value.toString().take(MAX_PARAM_STRING_LEN))
            }
        }
    }

    private fun sanitizeName(raw: String, maxLen: Int): String {
        if (raw.isEmpty()) return "_"
        val cleaned = buildString(raw.length) {
            for (c in raw) append(if (c.isLetterOrDigit() || c == '_') c else '_')
        }
        val nonDigit = if (cleaned.first().isDigit()) "_$cleaned" else cleaned
        // Firebase reserves these prefixes for the SDK and silently drops any
        // event/param/property whose name starts with one. Prefix-escape so
        // the value still reaches the dashboard under a sanctioned name.
        val unreserved = if (RESERVED_PREFIXES.any { nonDigit.startsWith(it) }) "_$nonDigit"
        else nonDigit
        return unreserved.take(maxLen)
    }

    private companion object {
        // Firebase Analytics SDK limits (current as of BoM 34.x).
        const val MAX_EVENT_NAME_LEN = 40
        const val MAX_PARAM_KEY_LEN = 40
        const val MAX_PROPERTY_KEY_LEN = 24
        const val MAX_PROPERTY_VALUE_LEN = 36
        const val MAX_PARAM_STRING_LEN = 100
        val RESERVED_PREFIXES = arrayOf("firebase_", "google_", "ga_")
    }
}
