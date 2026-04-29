package com.mingeek.sudokusage.variant

import com.mingeek.sudokusage.domain.board.RuleSet
import com.mingeek.sudokusage.domain.board.VariantRegistry
import com.mingeek.sudokusage.domain.generator.GenericGenerator
import com.mingeek.sudokusage.variant.classic.ClassicGenerator
import com.mingeek.sudokusage.variant.classic.ClassicRuleSet
import com.mingeek.sudokusage.variant.hyper.HyperRuleSet
import com.mingeek.sudokusage.variant.killer.KillerGenerator
import com.mingeek.sudokusage.variant.killer.KillerRuleSet
import com.mingeek.sudokusage.variant.mini.Mini4RuleSet
import com.mingeek.sudokusage.variant.mini.Mini6RuleSet
import com.mingeek.sudokusage.variant.x.XSudokuRuleSet

/**
 * Single point that registers every shipping variant with the [VariantRegistry].
 * New variants land by adding a `register` call here — `AppContainer` stays free
 * of variant-specific imports so it never has to change.
 */
object VariantsBootstrap {
    fun registerAll(registry: VariantRegistry) {
        registry.register(rules = ClassicRuleSet(), generator = ClassicGenerator())
        registerGeneric(registry, Mini6RuleSet())
        registerGeneric(registry, Mini4RuleSet())
        registerGeneric(registry, XSudokuRuleSet())
        registerGeneric(registry, HyperRuleSet())
        // Killer: registry holds an empty-cage default; per-puzzle ruleset is
        // built from Puzzle.cages by GameViewModel.
        registry.register(rules = KillerRuleSet(emptyList()), generator = KillerGenerator())
    }

    private fun registerGeneric(registry: VariantRegistry, rules: RuleSet) {
        registry.register(rules = rules, generator = GenericGenerator(rules))
    }
}
