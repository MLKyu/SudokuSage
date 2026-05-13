package com.mingeek.sudokusage.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    /** Auth backend hasn't reported a state yet (cold start, listener not fired). */
    data object Unknown : AuthState

    /** No user. Either never signed in or after a sign-out. */
    data object SignedOut : AuthState

    data class SignedIn(val uid: String, val isAnonymous: Boolean) : AuthState
}

/**
 * Backend-agnostic auth surface. Today: Firebase Auth (anonymous-first).
 * Tomorrow: linkable with Google / email when the user creates a real account.
 *
 * Callers should treat the user as anonymous until [AuthState.SignedIn] arrives;
 * cloud features should be no-ops while [state] is Unknown/SignedOut.
 */
interface AuthSession {
    val state: StateFlow<AuthState>

    /**
     * Ensures the user has at least an anonymous session. Returns the uid on
     * success, null on failure (e.g., no network during first launch).
     * Subsequent calls are cheap — they return the cached uid without a
     * round-trip when [state] is already SignedIn.
     */
    suspend fun ensureSignedInAnonymously(): String?

    suspend fun signOut()
}

class NoOpAuthSession : AuthSession {
    private val _state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    override val state: StateFlow<AuthState> = _state.asStateFlow()
    override suspend fun ensureSignedInAnonymously(): String? = null
    override suspend fun signOut() { /* nothing to sign out of */ }
}
