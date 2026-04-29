package com.mingeek.sudokusage.game.hint

import com.mingeek.sudokusage.domain.hint.HintEngine
import com.mingeek.sudokusage.domain.hint.Technique
import com.mingeek.sudokusage.domain.hint.techniques.HiddenSingleTechnique
import com.mingeek.sudokusage.domain.hint.techniques.NakedPairTechnique
import com.mingeek.sudokusage.domain.hint.techniques.NakedSingleTechnique
import com.mingeek.sudokusage.domain.hint.techniques.PointingPairTechnique

/**
 * Single point that registers every shipping [Technique] with [HintEngine].
 * New techniques land by adding a constructor call to [defaultTechniques] —
 * AppContainer stays free of technique-specific imports.
 */
object HintBootstrap {
    fun create(): HintEngine = HintEngine(defaultTechniques())

    private fun defaultTechniques(): List<Technique> = listOf(
        NakedSingleTechnique(),
        HiddenSingleTechnique(),
        NakedPairTechnique(),
        PointingPairTechnique(),
        // M5+: HiddenPairTechnique, BoxLineReductionTechnique, XWingTechnique, ...
    )
}
