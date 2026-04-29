package com.mingeek.sudokusage.domain.board

import com.mingeek.sudokusage.domain.generator.PuzzleGenerator

/**
 * Holds the pluggable set of available variants. Each variant module registers
 * its [RuleSet] + [PuzzleGenerator] at app startup; UI code then enumerates [all]
 * to build difficulty pickers, etc.
 *
 * Wired manually today; trivially convertible to a Hilt multibinding later.
 */
class VariantRegistry {
    private data class Entry(val rules: RuleSet, val generator: PuzzleGenerator)
    private val entries = linkedMapOf<VariantId, Entry>()

    fun register(rules: RuleSet, generator: PuzzleGenerator) {
        entries[rules.id] = Entry(rules, generator)
    }

    fun rules(id: VariantId): RuleSet =
        entries[id]?.rules ?: error("Variant not registered: ${id.value}")

    fun generator(id: VariantId): PuzzleGenerator =
        entries[id]?.generator ?: error("Variant not registered: ${id.value}")

    fun all(): List<VariantId> = entries.keys.toList()
    fun isRegistered(id: VariantId): Boolean = entries.containsKey(id)
}
