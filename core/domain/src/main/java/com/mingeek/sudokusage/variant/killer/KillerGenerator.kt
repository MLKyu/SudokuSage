package com.mingeek.sudokusage.variant.killer

import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.domain.generator.Puzzle
import com.mingeek.sudokusage.domain.generator.PuzzleGenerator

/**
 * Picks a hand-curated Killer puzzle from [KillerPuzzleBank]. Initial board has no
 * givens — Killer is solved purely from cage constraints. Same seed + difficulty
 * → identical puzzle (so daily/share semantics still hold).
 */
class KillerGenerator : PuzzleGenerator {
    override suspend fun generate(
        variant: VariantId,
        difficulty: Difficulty,
        seed: Long?,
    ): Puzzle {
        val effectiveSeed = seed ?: System.currentTimeMillis()
        val data = KillerPuzzleBank.puzzle(difficulty, effectiveSeed)
        return Puzzle(
            variant = VariantId.Killer,
            difficulty = difficulty,
            seed = effectiveSeed,
            initial = data.initial,
            solution = data.solution,
            cages = data.cages,
        )
    }
}
