package com.mingeek.sudokusage.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StatsEntity)

    @Query("SELECT * FROM puzzle_stats WHERE variant = :variant AND difficulty = :difficulty LIMIT 1")
    suspend fun get(variant: String, difficulty: String): StatsEntity?

    @Query("SELECT * FROM puzzle_stats")
    fun observeAll(): Flow<List<StatsEntity>>

    @Query("SELECT * FROM puzzle_stats WHERE variant = :variant ORDER BY difficulty")
    fun observeForVariant(variant: String): Flow<List<StatsEntity>>
}
