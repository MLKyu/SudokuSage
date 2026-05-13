package com.mingeek.sudokusage.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthSession(
    private val auth: FirebaseAuth,
) : AuthSession {

    private val _state = MutableStateFlow<AuthState>(
        auth.currentUser?.let { AuthState.SignedIn(it.uid, it.isAnonymous) } ?: AuthState.Unknown
    )
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        auth.addAuthStateListener { fa ->
            _state.value = fa.currentUser
                ?.let { AuthState.SignedIn(it.uid, it.isAnonymous) }
                ?: AuthState.SignedOut
        }
    }

    override suspend fun ensureSignedInAnonymously(): String? {
        auth.currentUser?.uid?.let { return it }
        return try {
            auth.signInAnonymously().await().user?.uid
        } catch (e: Exception) {
            Log.w(TAG, "Anonymous sign-in failed: ${e.message}")
            null
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    private companion object {
        const val TAG = "AuthSession"
    }
}
