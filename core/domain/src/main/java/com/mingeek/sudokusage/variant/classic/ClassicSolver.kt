package com.mingeek.sudokusage.variant.classic

import com.mingeek.sudokusage.domain.board.GenericSolver

/**
 * Backwards-compatible alias kept for existing call sites and tests.
 * Functionally identical to [GenericSolver] specialised to [ClassicRuleSet].
 */
class ClassicSolver : GenericSolver(ClassicRuleSet())
