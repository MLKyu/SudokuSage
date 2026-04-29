package com.mingeek.sudokusage.di

import com.mingeek.sudokusage.analytics.Analytics
import com.mingeek.sudokusage.analytics.NoOpAnalytics
import com.mingeek.sudokusage.audio.AudioController
import com.mingeek.sudokusage.audio.AudioSettings
import com.mingeek.sudokusage.data.achievement.AchievementCollector
import com.mingeek.sudokusage.data.db.SudokuDatabase
import com.mingeek.sudokusage.data.preferences.GameplaySettings
import com.mingeek.sudokusage.data.repo.AchievementRepository
import com.mingeek.sudokusage.data.repo.DailyChallengeRepository
import com.mingeek.sudokusage.data.repo.GameSaveRepository
import com.mingeek.sudokusage.data.repo.RoomAchievementRepository
import com.mingeek.sudokusage.data.repo.RoomDailyChallengeRepository
import com.mingeek.sudokusage.data.repo.RoomGameSaveRepository
import com.mingeek.sudokusage.data.repo.RoomStatsRepository
import com.mingeek.sudokusage.data.repo.StatsRepository
import com.mingeek.sudokusage.data.stats.StatsCollector
import com.mingeek.sudokusage.domain.board.GameState
import com.mingeek.sudokusage.domain.board.VariantRegistry
import com.mingeek.sudokusage.domain.event.DefaultGameEventBus
import com.mingeek.sudokusage.domain.event.GameEventBus
import com.mingeek.sudokusage.domain.hint.HintEngine
import com.mingeek.sudokusage.featureflags.FeatureFlags
import com.mingeek.sudokusage.featureflags.LocalFeatureFlags
import com.mingeek.sudokusage.feedback.Feedback
import com.mingeek.sudokusage.feedback.HapticController
import com.mingeek.sudokusage.game.GameViewModel
import com.mingeek.sudokusage.game.hint.HintBootstrap
import com.mingeek.sudokusage.monetization.AdProvider
import com.mingeek.sudokusage.monetization.EntitlementGate
import com.mingeek.sudokusage.monetization.IapProvider
import com.mingeek.sudokusage.monetization.NoOpAdProvider
import com.mingeek.sudokusage.monetization.NoOpEntitlementGate
import com.mingeek.sudokusage.monetization.NoOpIapProvider
import com.mingeek.sudokusage.platform.sync.CloudSyncProvider
import com.mingeek.sudokusage.platform.sync.NoOpCloudSyncProvider
import com.mingeek.sudokusage.ui.screens.achievements.AchievementsViewModel
import com.mingeek.sudokusage.ui.screens.daily.DailyViewModel
import com.mingeek.sudokusage.ui.screens.home.HomeViewModel
import com.mingeek.sudokusage.ui.screens.pro.ProViewModel
import com.mingeek.sudokusage.ui.screens.replay.ReplayViewModel
import com.mingeek.sudokusage.ui.screens.settings.SettingsViewModel
import com.mingeek.sudokusage.ui.screens.stats.StatsViewModel
import com.mingeek.sudokusage.variant.VariantsBootstrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Qualifiers for Koin definitions that share a type. */
object DiQualifiers {
    val ApplicationScope = named("applicationScope")
    val LastFinishedGame = named("lastFinishedGame")
}

private val appModule = module {
    single<CoroutineScope>(DiQualifiers.ApplicationScope) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    single<FeatureFlags> { LocalFeatureFlags() }
    single<Analytics> { NoOpAnalytics() }
    single<MutableStateFlow<GameState?>>(DiQualifiers.LastFinishedGame) {
        MutableStateFlow(null)
    }
}

private val persistenceModule = module {
    single { SudokuDatabase.create(androidContext()) }
    single { get<SudokuDatabase>().gameSaveDao() }
    single { get<SudokuDatabase>().statsDao() }
    single { get<SudokuDatabase>().dailyCompletionDao() }
    single { get<SudokuDatabase>().achievementProgressDao() }

    single<GameSaveRepository> { RoomGameSaveRepository(get()) }
    single<StatsRepository> { RoomStatsRepository(get()) }
    single<DailyChallengeRepository> { RoomDailyChallengeRepository(get()) }
    single<AchievementRepository> { RoomAchievementRepository(get()) }

    single { GameplaySettings(androidContext()) }
}

private val audioFeedbackModule = module {
    single { AudioSettings(androidContext()) }
    single {
        AudioController(
            context = androidContext(),
            settings = get(),
            appScope = get(DiQualifiers.ApplicationScope),
        )
    }
    single {
        HapticController(androidContext()).also { hc ->
            val settings = get<AudioSettings>()
            val scope = get<CoroutineScope>(DiQualifiers.ApplicationScope)
            settings.state
                .map { it.hapticEnabled }
                .distinctUntilChanged()
                .onEach(hc::setEnabled)
                .launchIn(scope)
        }
    }
    single { Feedback(get(), get()) }
}

private val domainModule = module {
    single<GameEventBus> { DefaultGameEventBus() }
    single { VariantRegistry().also(VariantsBootstrap::registerAll) }
    single<HintEngine> { HintBootstrap.create() }
}

private val monetizationModule = module {
    single<EntitlementGate> { NoOpEntitlementGate() }
    single<AdProvider> { NoOpAdProvider() }
    single<IapProvider> { NoOpIapProvider() }
    single<CloudSyncProvider> { NoOpCloudSyncProvider() }
}

private val collectorsModule = module {
    single {
        StatsCollector(
            eventBus = get(),
            repository = get(),
            scope = get(DiQualifiers.ApplicationScope),
        )
    }
    single {
        AchievementCollector(
            eventBus = get(),
            repo = get(),
            scope = get(DiQualifiers.ApplicationScope),
        )
    }
}

private val viewModelModule = module {
    viewModel { HomeViewModel(saves = get(), daily = get()) }
    viewModel { DailyViewModel(repo = get()) }
    viewModel { StatsViewModel(statsRepository = get()) }
    viewModel { SettingsViewModel(audio = get(), gameplay = get()) }
    viewModel { AchievementsViewModel(repo = get()) }
    viewModel { ProViewModel(iapProvider = get(), entitlementGate = get()) }
    viewModel {
        ReplayViewModel(
            source = get(DiQualifiers.LastFinishedGame),
            variantRegistry = get(),
        )
    }
    viewModel { params ->
        // SavedStateHandle injected by Koin's koinViewModel() helper.
        GameViewModel(
            savedStateHandle = params.get(),
            feedback = get(),
            audio = get(),
            saves = get(),
            variantRegistry = get(),
            gameEventBus = get(),
            dailyChallengeRepository = get(),
            hintEngine = get(),
            gameplaySettings = get(),
            entitlementGate = get(),
            adProvider = get(),
            lastFinishedGame = get(DiQualifiers.LastFinishedGame),
            appScope = get(DiQualifiers.ApplicationScope),
        )
    }
}

val sudokuSageModules = listOf(
    appModule,
    persistenceModule,
    audioFeedbackModule,
    domainModule,
    monetizationModule,
    collectorsModule,
    viewModelModule,
)
