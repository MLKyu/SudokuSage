package com.mingeek.sudokusage.variant.classic

import com.mingeek.sudokusage.domain.board.BoxedRuleSet
import com.mingeek.sudokusage.domain.board.VariantId

class ClassicRuleSet : BoxedRuleSet(
    id = VariantId.Classic,
    boardSize = 9,
    boxRows = 3,
    boxCols = 3,
)
