package com.mingeek.sudokusage.platform.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SyncState { Idle, Syncing, Error }

data class SyncStatus(
    val state: SyncState = SyncState.Idle,
    val lastSyncedAt: Long? = null,
    val errorMessage: String? = null,
)

/**
 * Bi-directional cloud sync for stats, daily completions, achievements, and the
 * current saved game. Today: a no-op stub so call sites compile. M7.1 swaps in
 * a Firebase Firestore implementation (or your own backend) without changes
 * upstream.
 */
interface CloudSyncProvider {
    val status: Flow<SyncStatus>
    suspend fun pushAll()
    suspend fun pullAll()
}

class NoOpCloudSyncProvider : CloudSyncProvider {
    private val _status = MutableStateFlow(SyncStatus())
    override val status: Flow<SyncStatus> = _status.asStateFlow()
    override suspend fun pushAll() { /* nothing to push */ }
    override suspend fun pullAll() { /* nothing to pull */ }
}
