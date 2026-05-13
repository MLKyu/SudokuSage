package com.mingeek.sudokusage

import android.app.Application
import com.mingeek.sudokusage.audio.AudioController
import com.mingeek.sudokusage.data.achievement.AchievementCollector
import com.mingeek.sudokusage.data.stats.StatsCollector
import com.mingeek.sudokusage.di.sudokuSageModules
import com.mingeek.sudokusage.messaging.NotificationChannels
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class SudokuSageApp : Application() {

    private val audioController: AudioController by inject()
    private val statsCollector: StatsCollector by inject()
    private val achievementCollector: AchievementCollector by inject()

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
    }

    override fun onTerminate() {
        audioController.release()
        super.onTerminate()
    }
}
