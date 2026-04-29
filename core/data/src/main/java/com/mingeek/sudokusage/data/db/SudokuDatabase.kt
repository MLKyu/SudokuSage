package com.mingeek.sudokusage.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Schema versions:
 *   1 → initial: game_saves + puzzle_stats
 *   2 → adds daily_completions; adds dailyDate column to game_saves
 *   3 → adds achievement_progress
 *   4 → adds cages column to game_saves (Killer variant)
 *
 * Pre-release: any unrecognized version is wiped and rebuilt
 * (`fallbackToDestructiveMigration`). Replace with proper migrations once we ship.
 */
@Database(
    entities = [
        GameSaveEntity::class,
        StatsEntity::class,
        DailyCompletionEntity::class,
        AchievementProgressEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class SudokuDatabase : RoomDatabase() {

    abstract fun gameSaveDao(): GameSaveDao
    abstract fun statsDao(): StatsDao
    abstract fun dailyCompletionDao(): DailyCompletionDao
    abstract fun achievementProgressDao(): AchievementProgressDao

    companion object {
        fun create(context: Context): SudokuDatabase =
            Room.databaseBuilder(context, SudokuDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        private const val DB_NAME = "sudoku_sage.db"
    }
}
