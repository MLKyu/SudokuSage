package com.mingeek.sudokusage.variant.mini

import com.mingeek.sudokusage.domain.board.BoxedRuleSet
import com.mingeek.sudokusage.domain.board.VariantId

/** 4×4 micro Sudoku, 2×2 boxes. Great for first-time players. */
class Mini4RuleSet : BoxedRuleSet(
    id = VariantId.Mini4,
    boardSize = 4,
    boxRows = 2,
    boxCols = 2,
)
