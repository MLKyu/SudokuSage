package com.mingeek.sudokusage

import android.app.Application
import com.mingeek.sudokusage.analytics.AnalyticsCollector
import com.mingeek.sudokusage.audio.AudioController
import com.mingeek.sudokusage.auth.AuthSession
import com.mingeek.sudokusage.data.achievement.AchievementCollector
import com.mingeek.sudokusage.data.stats.StatsCollector
import com.mingeek.sudokusage.di.DiQualifiers
import com.mingeek.sudokusage.di.sudokuSageModules
import com.mingeek.sudokusage.featureflags.FeatureFlags
import com.mingeek.sudokusage.featureflags.FlagKeys
import com.mingeek.sudokusage.messaging.NotificationChannels
import com.mingeek.sudokusage.platform.sync.CloudSyncProvider
import com.mingeek.sudokusage.platform.sync.SyncCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class SudokuSageApp : Application() {

    private val audioController: AudioController by inject()
    private val statsCollector: StatsCollector by inject()
    private val achievementCollector: AchievementCollector by inject()
    private val analyticsCollector: AnalyticsCollector by inject()
    private val syncCollector: SyncCollector by inject()
    private val authSession: AuthSession by inject()
    private val featureFlags: FeatureFlags by inject()
    private val cloudSync: CloudSyncProvider by inject()
    private val appScope: CoroutineScope by inject(DiQualifiers.ApplicationScope)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SudokuSageApp)
            modules(sudokuSageModules)
        }
        NotificationChannels.ensureCreated(this)
        // Eagerly start GameEventBus subscribers — Koin instantiates lazily
        // otherwise and we need them subscribed from process start.
        statsCollector.start()
        achievementCollector.start()
        analyticsCollector.start()
        syncCollector.start()
        bootstrapFirebase()
    }

    /**
     * Sign in anonymously, fetch Remote Config, then pull cloud state. Anonymous
     * auth means the user gets sync without ever seeing a sign-in screen; if
     * later they create a real account, [com.google.firebase.auth.FirebaseAuth.signInWithCredential]
     * + linkWithCredential preserves the same UID and all the cloud state.
     */
    private fun bootstrapFirebase() {
        appScope.launch {
            val authJob = launch { authSession.ensureSignedInAnonymously() }
            val configJob = launch { featureFlags.refresh() }
            authJob.join()
            configJob.join()
            if (featureFlags.bool(FlagKeys.EnableCloudSync, default = true)) {
                cloudSync.pullAll()
            }
        }
    }

    override fun onTerminate() {
        audioController.release()
        super.onTerminate()
    }
}
