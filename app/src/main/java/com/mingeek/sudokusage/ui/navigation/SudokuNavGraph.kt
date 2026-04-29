package com.mingeek.sudokusage.ui.navigation

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.game.GameLaunch
import com.mingeek.sudokusage.ui.screens.achievements.AchievementsScreen
import com.mingeek.sudokusage.ui.screens.daily.DailyScreen
import com.mingeek.sudokusage.ui.screens.game.GameScreen
import com.mingeek.sudokusage.ui.screens.home.HomeScreen
import com.mingeek.sudokusage.ui.screens.pro.ProScreen
import com.mingeek.sudokusage.ui.screens.replay.ReplayScreen
import com.mingeek.sudokusage.ui.screens.settings.SettingsScreen
import com.mingeek.sudokusage.ui.screens.stats.StatsScreen
import com.mingeek.sudokusage.ui.screens.trainer.TrainerScreen
import java.time.LocalDate

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val STATS = "stats"
    const val DAILY = "daily"
    const val TRAINER = "trainer"
    const val ACHIEVEMENTS = "achievements"
    const val PRO = "pro"
    const val REPLAY = "replay"

    const val GAME_NEW_PATTERN =
        "game/new/{${NavArgs.VARIANT}}/{${NavArgs.DIFFICULTY}}?${NavArgs.SEED}={${NavArgs.SEED}}"
    const val GAME_RESUME = "game/resume"
    const val GAME_DAILY_PATTERN = "game/daily/{${NavArgs.DATE}}"
    const val GAME_TRAINER_PATTERN = "game/trainer/{${NavArgs.LESSON}}"

    fun newGame(variant: VariantId, difficulty: Difficulty, seed: Long? = null): String =
        buildString {
            append("game/new/${variant.value}/${difficulty.name}")
            if (seed != null) append("?${NavArgs.SEED}=$seed")
        }

    fun gameDaily(date: LocalDate): String = "game/daily/$date"
    fun gameTrainer(lessonId: String): String = "game/trainer/$lessonId"
}

object NavArgs {
    const val VARIANT = "variant"
    const val DIFFICULTY = "difficulty"
    const val SEED = "seed"
    const val DATE = "date"
    const val LESSON = "lesson"

    /**
     * Derive [GameLaunch] from nav args present in the SavedStateHandle.
     * Detection order: lesson > date > variant+difficulty > resume.
     */
    fun gameLaunchFrom(handle: SavedStateHandle): GameLaunch {
        val lesson = handle.get<String>(LESSON)
        if (!lesson.isNullOrEmpty()) return GameLaunch.Trainer(lesson)

        val dateStr = handle.get<String>(DATE)
        if (!dateStr.isNullOrEmpty()) {
            val date = runCatching { LocalDate.parse(dateStr) }.getOrDefault(LocalDate.now())
            return GameLaunch.Daily(date)
        }

        val variant = handle.get<String>(VARIANT)
        val difficulty = handle.get<String>(DIFFICULTY)
        if (!variant.isNullOrEmpty() && !difficulty.isNullOrEmpty()) {
            return GameLaunch.New(
                variantId = VariantId(variant),
                difficulty = runCatching { Difficulty.valueOf(difficulty) }
                    .getOrDefault(Difficulty.Easy),
                seed = handle.get<String>(SEED)?.toLongOrNull(),
            )
        }

        return GameLaunch.Resume
    }
}

@Composable
fun SudokuNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = koinViewModel(),
                onPickDifficulty = { variant, difficulty ->
                    navController.navigate(Routes.newGame(variant, difficulty))
                },
                onContinue = { navController.navigate(Routes.GAME_RESUME) },
                onOpenDaily = { navController.navigate(Routes.DAILY) },
                onOpenTrainer = { navController.navigate(Routes.TRAINER) },
                onOpenAchievements = { navController.navigate(Routes.ACHIEVEMENTS) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = Routes.GAME_NEW_PATTERN,
            arguments = listOf(
                navArgument(NavArgs.VARIANT) { type = NavType.StringType },
                navArgument(NavArgs.DIFFICULTY) { type = NavType.StringType },
                navArgument(NavArgs.SEED) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            GameScreen(
                viewModel = koinViewModel(),
                onExit = { navController.popBackStack() },
                onOpenPro = { navController.navigate(Routes.PRO) },
                onOpenReplay = { navController.navigate(Routes.REPLAY) },
            )
        }

        composable(Routes.GAME_RESUME) {
            GameScreen(
                viewModel = koinViewModel(),
                onExit = { navController.popBackStack() },
                onOpenPro = { navController.navigate(Routes.PRO) },
                onOpenReplay = { navController.navigate(Routes.REPLAY) },
            )
        }

        composable(
            route = Routes.GAME_DAILY_PATTERN,
            arguments = listOf(
                navArgument(NavArgs.DATE) { type = NavType.StringType },
            ),
        ) {
            GameScreen(
                viewModel = koinViewModel(),
                onExit = { navController.popBackStack() },
                onOpenPro = { navController.navigate(Routes.PRO) },
                onOpenReplay = { navController.navigate(Routes.REPLAY) },
            )
        }

        composable(
            route = Routes.DAILY,
            deepLinks = listOf(navDeepLink { uriPattern = "sudokusage://daily" }),
        ) {
            DailyScreen(
                viewModel = koinViewModel(),
                onPlay = { date ->
                    navController.navigate(Routes.gameDaily(date)) {
                        popUpTo(Routes.DAILY) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.STATS) {
            StatsScreen(viewModel = koinViewModel(), onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() },
                onOpenPro = { navController.navigate(Routes.PRO) },
            )
        }

        composable(Routes.TRAINER) {
            TrainerScreen(
                onBack = { navController.popBackStack() },
                onPractice = { lesson ->
                    if (lesson.samplePuzzleEncoded != null) {
                        navController.navigate(Routes.gameTrainer(lesson.id))
                    } else {
                        navController.navigate(Routes.newGame(VariantId.Classic, Difficulty.Medium))
                    }
                },
            )
        }

        composable(
            route = Routes.GAME_TRAINER_PATTERN,
            arguments = listOf(
                navArgument(NavArgs.LESSON) { type = NavType.StringType },
            ),
        ) {
            GameScreen(
                viewModel = koinViewModel(),
                onExit = { navController.popBackStack() },
                onOpenPro = { navController.navigate(Routes.PRO) },
                onOpenReplay = { navController.navigate(Routes.REPLAY) },
            )
        }

        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(viewModel = koinViewModel(), onBack = { navController.popBackStack() })
        }

        composable(Routes.PRO) {
            ProScreen(viewModel = koinViewModel(), onBack = { navController.popBackStack() })
        }

        composable(Routes.REPLAY) {
            ReplayScreen(viewModel = koinViewModel(), onBack = { navController.popBackStack() })
        }
    }
}
