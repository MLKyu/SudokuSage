package com.mingeek.sudokusage.variant.classic

import com.mingeek.sudokusage.domain.generator.GenericGenerator

/**
 * Backwards-compatible alias kept for existing call sites and tests.
 * Equivalent to [GenericGenerator] specialised to [ClassicRuleSet].
 */
class ClassicGenerator(
    solver: ClassicSolver = ClassicSolver(),
) : GenericGenerator(ClassicRuleSet(), solver)
