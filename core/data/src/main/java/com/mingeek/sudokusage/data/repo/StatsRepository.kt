package com.mingeek.sudokusage.data.repo

import com.mingeek.sudokusage.data.db.StatsDao
import com.mingeek.sudokusage.data.db.StatsEntity
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.domain.event.PuzzleResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max
import kotlin.math.min

data class StatsSummary(
    val variant: VariantId,
    val difficulty: Difficulty,
    val gamesStarted: Int,
    val gamesWon: Int,
    val gamesFailed: Int,
    val bestTimeMs: Long?,
    val avgTimeMs: Long?,
    val currentStreak: Int,
    val bestStreak: Int,
    val lastPlayedAt: Long?,
)

interface StatsRepository {
    fun observeAll(): Flow<List<StatsSummary>>
    fun observeForVariant(variant: VariantId): Flow<List<StatsSummary>>
    suspend fun get(variant: VariantId, difficulty: Difficulty): StatsSummary?
    suspend fun recordStarted(variant: VariantId, difficulty: Difficulty)
    suspend fun recordCompleted(variant: VariantId, difficulty: Difficulty, result: PuzzleResult, elapsedMs: Long)
}

class RoomStatsRepository(
    private val dao: StatsDao,
    private val now: () -> Long = System::currentTimeMillis,
) : StatsRepository {

    override fun observeAll(): Flow<List<StatsSummary>> =
        dao.observeAll().map { rows -> rows.map { it.toSummary() } }

    override fun observeForVariant(variant: VariantId): Flow<List<StatsSummary>> =
        dao.observeForVariant(variant.value).map { rows -> rows.map { it.toSummary() } }

    override suspend fun get(variant: VariantId, difficulty: Difficulty): StatsSummary? =
        dao.get(variant.value, difficulty.name)?.toSummary()

    override suspend fun recordStarted(variant: VariantId, difficulty: Difficulty) {
        val current = dao.get(variant.value, difficulty.name)
            ?: StatsEntity(variant = variant.value, difficulty = difficulty.name)
        dao.upsert(
            current.copy(
                gamesStarted = current.gamesStarted + 1,
                lastPlayedAt = now(),
            )
        )
    }

    override suspend fun recordCompleted(
        variant: VariantId,
        difficulty: Difficulty,
        result: PuzzleResult,
        elapsedMs: Long,
    ) {
        val current = dao.get(variant.value, difficulty.name)
            ?: StatsEntity(variant = variant.value, difficulty = difficulty.name)
        val updated = when (result) {
            PuzzleResult.Won -> {
                val streak = current.currentStreak + 1
                current.copy(
                    gamesWon = current.gamesWon + 1,
                    bestTimeMs = current.bestTimeMs?.let { min(it, elapsedMs) } ?: elapsedMs,
                    totalTimeMs = current.totalTimeMs + elapsedMs,
                    currentStreak = streak,
                    bestStreak = max(current.bestStreak, streak),
                    lastPlayedAt = now(),
                )
            }
            PuzzleResult.Failed -> current.copy(
                gamesFailed = current.gamesFailed + 1,
                currentStreak = 0,
                lastPlayedAt = now(),
            )
        }
        dao.upsert(updated)
    }

    private fun StatsEntity.toSummary(): StatsSummary = StatsSummary(
        variant = VariantId(variant),
        difficulty = Difficulty.valueOf(difficulty),
        gamesStarted = gamesStarted,
        gamesWon = gamesWon,
        gamesFailed = gamesFailed,
        bestTimeMs = bestTimeMs,
        avgTimeMs = if (gamesWon > 0) totalTimeMs / gamesWon else null,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        lastPlayedAt = lastPlayedAt,
    )
}
