package com.mingeek.sudokusage.data.repo

import com.mingeek.sudokusage.data.db.DailyCompletionDao
import com.mingeek.sudokusage.data.db.DailyCompletionEntity
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.daily.DailyOutcome
import com.mingeek.sudokusage.domain.daily.FixedDifficultyStrategy
import com.mingeek.sudokusage.domain.event.PuzzleResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DailyChallengeRepositoryTest {

    private val noopDao = object : DailyCompletionDao {
        override suspend fun upsert(entity: DailyCompletionEntity) = Unit
        override suspend fun get(dateKey: String): DailyCompletionEntity? = null
        override fun observeSince(sinceKey: String): Flow<List<DailyCompletionEntity>> = emptyFlow()
        override fun observeAll(): Flow<List<DailyCompletionEntity>> = emptyFlow()
        override suspend fun getAll(): List<DailyCompletionEntity> = emptyList()
    }

    private val repo = RoomDailyChallengeRepository(
        dao = noopDao,
        strategy = FixedDifficultyStrategy(Difficulty.Medium),
    )
    private val today = LocalDate.of(2026, 4, 28)

    @Test
    fun emptyHistory_zeroStreak_notPlayedToday() {
        val status = repo.buildStatus(emptyList(), today)
        assertEquals(0, status.currentStreak)
        assertEquals(0, status.bestStreak)
        assertEquals(DailyOutcome.NotPlayed, status.todayInfo.outcome)
    }

    @Test
    fun consecutiveWinsIncludingToday_streakCountsToday() {
        val rows = listOf(
            won(today.minusDays(2)),
            won(today.minusDays(1)),
            won(today),
        )
        val status = repo.buildStatus(rows, today)
        assertEquals(3, status.currentStreak)
        assertEquals(3, status.bestStreak)
        assertEquals(DailyOutcome.Won, status.todayInfo.outcome)
    }

    @Test
    fun todayNotPlayed_butYesterdayWon_streakCountsYesterdayBackward() {
        val rows = listOf(
            won(today.minusDays(2)),
            won(today.minusDays(1)),
        )
        val status = repo.buildStatus(rows, today)
        assertEquals(2, status.currentStreak)
        assertEquals(2, status.bestStreak)
        assertEquals(DailyOutcome.NotPlayed, status.todayInfo.outcome)
    }

    @Test
    fun failedToday_breaksCurrentStreak() {
        val rows = listOf(
            won(today.minusDays(1)),
            failed(today),
        )
        val status = repo.buildStatus(rows, today)
        assertEquals(0, status.currentStreak)
        assertEquals(1, status.bestStreak)
        assertEquals(DailyOutcome.Failed, status.todayInfo.outcome)
    }

    @Test
    fun gapBreaksRunButLatestUnbrokenStreakCounts() {
        val rows = listOf(
            won(today.minusDays(7)),
            won(today.minusDays(6)),
            // gap day at -5
            won(today.minusDays(3)),
            won(today.minusDays(2)),
            won(today.minusDays(1)),
            won(today),
        )
        val status = repo.buildStatus(rows, today)
        // Today + 3 prior unbroken days = 4
        assertEquals(4, status.currentStreak)
        assertEquals(4, status.bestStreak)
    }

    @Test
    fun bestStreakReflectsHistoricalRun() {
        // 5-win run early, then a 2-win run ending today
        val rows = listOf(
            won(today.minusDays(20)),
            won(today.minusDays(19)),
            won(today.minusDays(18)),
            won(today.minusDays(17)),
            won(today.minusDays(16)),
            // gap
            won(today.minusDays(1)),
            won(today),
        )
        val status = repo.buildStatus(rows, today)
        assertEquals(2, status.currentStreak)
        assertEquals(5, status.bestStreak)
    }

    private fun won(date: LocalDate) = entity(date, PuzzleResult.Won)
    private fun failed(date: LocalDate) = entity(date, PuzzleResult.Failed)

    private fun entity(date: LocalDate, result: PuzzleResult) = DailyCompletionEntity(
        dateKey = date.toString(),
        variant = "classic",
        difficulty = "Medium",
        seed = 0L,
        result = result.name,
        elapsedMs = 60_000L,
        mistakes = 0,
        completedAt = 0L,
    )
}
