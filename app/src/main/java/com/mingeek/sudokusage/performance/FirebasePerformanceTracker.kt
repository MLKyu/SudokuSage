package com.mingeek.sudokusage.performance

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace as FbTrace

class FirebasePerformanceTracker(
    private val perf: FirebasePerformance,
) : PerformanceTracker {
    override fun newTrace(name: String): Trace = FirebaseTraceAdapter(perf.newTrace(name))
}

private class FirebaseTraceAdapter(private val inner: FbTrace) : Trace {
    override fun start() { inner.start() }
    override fun stop() { inner.stop() }
    override fun putMetric(key: String, value: Long) { inner.putMetric(key, value) }
    override fun incrementMetric(key: String, by: Long) { inner.incrementMetric(key, by) }
    override fun putAttribute(key: String, value: String) { inner.putAttribute(key, value) }
}
