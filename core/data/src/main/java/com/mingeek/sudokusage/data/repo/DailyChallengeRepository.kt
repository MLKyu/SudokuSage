package com.mingeek.sudokusage.data.repo

import com.mingeek.sudokusage.data.db.DailyCompletionDao
import com.mingeek.sudokusage.data.db.DailyCompletionEntity
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.domain.daily.DailyConfig
import com.mingeek.sudokusage.domain.daily.DailyDayInfo
import com.mingeek.sudokusage.domain.daily.DailyDifficultyStrategy
import com.mingeek.sudokusage.domain.daily.DailyOutcome
import com.mingeek.sudokusage.domain.daily.DailySeed
import com.mingeek.sudokusage.domain.daily.DailyStatus
import com.mingeek.sudokusage.domain.daily.FixedDifficultyStrategy
import com.mingeek.sudokusage.domain.event.PuzzleResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

interface DailyChallengeRepository {
    fun configFor(date: LocalDate): DailyConfig
    suspend fun get(date: LocalDate): DailyCompletionEntity?
    suspend fun record(
        date: LocalDate,
        result: PuzzleResult,
        elapsedMs: Long,
        mistakes: Int,
        seed: Long,
        variant: VariantId,
        difficulty: Difficulty,
    )
    fun observeStatus(today: LocalDate): Flow<DailyStatus>
    suspend fun currentStreakAsOf(date: LocalDate): Int
}

class RoomDailyChallengeRepository(
    private val dao: DailyCompletionDao,
    private val strategy: DailyDifficultyStrategy = FixedDifficultyStrategy(),
    private val now: () -> Long = System::currentTimeMillis,
) : DailyChallengeRepository {

    override fun configFor(date: LocalDate): DailyConfig = DailyConfig(
        variant = VariantId.Classic,
        difficulty = strategy.difficultyFor(date),
        seed = DailySeed.forDate(date),
    )

    override suspend fun get(date: LocalDate): DailyCompletionEntity? =
        dao.get(date.toString())

    override suspend fun record(
        date: LocalDate,
        result: PuzzleResult,
        elapsedMs: Long,
        mistakes: Int,
        seed: Long,
        variant: VariantId,
        difficulty: Difficulty,
    ) {
        dao.upsert(
            DailyCompletionEntity(
                dateKey = date.toString(),
                variant = variant.value,
                difficulty = difficulty.name,
                seed = seed,
                result = result.name,
                elapsedMs = elapsedMs,
                mistakes = mistakes,
                completedAt = now(),
            )
        )
    }

    override fun observeStatus(today: LocalDate): Flow<DailyStatus> =
        dao.observeAll().map { rows -> buildStatus(rows, today) }

    override suspend fun currentStreakAsOf(date: LocalDate): Int {
        val byDate = dao.getAll().associateBy { LocalDate.parse(it.dateKey) }
        return computeCurrentStreak(byDate, date)
    }

    /** Internal for unit tests. */
    internal fun buildStatus(rows: List<DailyCompletionEntity>, today: LocalDate): DailyStatus {
        val byDate = rows.associateBy { LocalDate.parse(it.dateKey) }
        val todayEntry = byDate[today]
        val todayInfo = DailyDayInfo(
            date = today,
            outcome = outcomeOf(todayEntry),
            elapsedMs = todayEntry?.elapsedMs,
        )
        return DailyStatus(
            today = today,
            todayConfig = configFor(today),
            todayInfo = todayInfo,
            currentStreak = computeCurrentStreak(byDate, today),
            bestStreak = computeBestStreak(byDate),
            recentDays = (0 until CALENDAR_DAYS).map { offset ->
                val d = today.minusDays(offset.toLong())
                val e = byDate[d]
                DailyDayInfo(d, outcomeOf(e), e?.elapsedMs)
            },
        )
    }

    private fun outcomeOf(entry: DailyCompletionEntity?): DailyOutcome = when (entry?.result) {
        PuzzleResult.Won.name -> DailyOutcome.Won
        PuzzleResult.Failed.name -> DailyOutcome.Failed
        else -> DailyOutcome.NotPlayed
    }

    private fun computeCurrentStreak(
        byDate: Map<LocalDate, DailyCompletionEntity>,
        today: LocalDate,
    ): Int {
        val todayEntry = byDate[today]
        val startDate = when {
            todayEntry == null -> today.minusDays(1L)               // not yet played today
            todayEntry.result == PuzzleResult.Won.name -> today     // count today
            else -> return 0                                         // failed today → broken
        }
        var streak = 0
        var d = startDate
        while (byDate[d]?.result == PuzzleResult.Won.name) {
            streak++
            d = d.minusDays(1L)
        }
        return streak
    }

    private fun computeBestStreak(byDate: Map<LocalDate, DailyCompletionEntity>): Int {
        val winningDates = byDate.entries
            .asSequence()
            .filter { it.value.result == PuzzleResult.Won.name }
            .map { it.key }
            .sorted()
            .toList()
        if (winningDates.isEmpty()) return 0
        var best = 1
        var run = 1
        for (i in 1 until winningDates.size) {
            run = if (winningDates[i] == winningDates[i - 1].plusDays(1L)) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }

    private companion object {
        const val CALENDAR_DAYS = 35
    }
}
