package com.mingeek.sudokusage.domain.board

/**
 * Stable identifier for a Sudoku variant. New variants are added by:
 *   1. Defining a new constant here.
 *   2. Implementing [RuleSet] + [com.mingeek.sudokusage.domain.generator.PuzzleGenerator].
 *   3. Registering them in [com.mingeek.sudokusage.domain.board.VariantRegistry].
 *
 * The string value is the persistence key; never rename an existing one.
 */
@JvmInline
value class VariantId(val value: String) {
    companion object {
        val Classic = VariantId("classic")     // 9x9
        val Mini6 = VariantId("mini6")         // 6x6
        val Mini4 = VariantId("mini4")         // 4x4
        val Killer = VariantId("killer")       // 9x9 + cages
        val XSudoku = VariantId("x")           // 9x9 + diagonals
        val Hyper = VariantId("hyper")         // 9x9 + extra hyper boxes
    }
}
