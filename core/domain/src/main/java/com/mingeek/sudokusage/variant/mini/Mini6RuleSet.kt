package com.mingeek.sudokusage.variant.mini

import com.mingeek.sudokusage.domain.board.BoxedRuleSet
import com.mingeek.sudokusage.domain.board.VariantId

/** 6×6 mini Sudoku, 2×3 (rectangular) boxes. */
class Mini6RuleSet : BoxedRuleSet(
    id = VariantId.Mini6,
    boardSize = 6,
    boxRows = 2,
    boxCols = 3,
)
