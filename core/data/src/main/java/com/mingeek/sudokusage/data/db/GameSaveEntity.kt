package com.mingeek.sudokusage.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton row holding the currently active game (if any). The id is fixed to 1 —
 * starting a new game overwrites the previous save (Sudoku.com semantics).
 *
 * Boards and move history are stored as opaque codec strings (see `data.codec`).
 * Adding columns is fine; removing or renaming requires a migration.
 */
@Entity(tableName = "game_saves")
data class GameSaveEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val variant: String,
    val difficulty: String,
    val seed: Long,
    val initialBoard: String,
    val currentValues: String,
    val currentNotes: String,
    val solution: String,
    val moveHistory: String,
    val redoStack: String,
    val mistakes: Int,
    val mistakeLimit: Int?,
    val elapsedMs: Long,
    val hintsUsed: Int,
    val status: String,
    val startedAt: Long,
    val updatedAt: Long,
    val dailyDate: String? = null,
    /** Killer cage definitions encoded by [CageCodec]. Empty string for non-Killer. */
    val cages: String = "",
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
