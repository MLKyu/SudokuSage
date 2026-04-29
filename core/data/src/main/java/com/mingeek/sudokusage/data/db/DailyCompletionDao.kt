package com.mingeek.sudokusage.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyCompletionEntity)

    @Query("SELECT * FROM daily_completions WHERE dateKey = :dateKey LIMIT 1")
    suspend fun get(dateKey: String): DailyCompletionEntity?

    @Query("SELECT * FROM daily_completions WHERE dateKey >= :sinceKey ORDER BY dateKey")
    fun observeSince(sinceKey: String): Flow<List<DailyCompletionEntity>>

    @Query("SELECT * FROM daily_completions ORDER BY dateKey")
    fun observeAll(): Flow<List<DailyCompletionEntity>>

    @Query("SELECT * FROM daily_completions ORDER BY dateKey")
    suspend fun getAll(): List<DailyCompletionEntity>
}
