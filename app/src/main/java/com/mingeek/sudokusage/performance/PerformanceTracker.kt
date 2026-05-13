package com.mingeek.sudokusage.performance

/**
 * Backend-agnostic performance traces. Today: Firebase Performance Monitoring.
 * Tomorrow: anything else, or no-op for tests. Use [trace] for the common
 * "measure this suspend block" case; reach for [newTrace] only when you need
 * mid-span metrics/attributes.
 */
interface PerformanceTracker {
    fun newTrace(name: String): Trace

    suspend fun <T> trace(name: String, block: suspend () -> T): T {
        val t = newTrace(name)
        t.start()
        return try {
            block()
        } finally {
            t.stop()
        }
    }
}

interface Trace {
    fun start()
    fun stop()
    fun putMetric(key: String, value: Long)
    fun incrementMetric(key: String, by: Long = 1L)
    fun putAttribute(key: String, value: String)
}

class NoOpPerformanceTracker : PerformanceTracker {
    override fun newTrace(name: String): Trace = NoOpTrace
    private object NoOpTrace : Trace {
        override fun start() = Unit
        override fun stop() = Unit
        override fun putMetric(key: String, value: Long) = Unit
        override fun incrementMetric(key: String, by: Long) = Unit
        override fun putAttribute(key: String, value: String) = Unit
    }
}
