package com.mingeek.sudokusage.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AchievementProgressEntity)

    @Query("SELECT * FROM achievement_progress WHERE achievementId = :id LIMIT 1")
    suspend fun get(id: String): AchievementProgressEntity?

    @Query("SELECT * FROM achievement_progress")
    fun observeAll(): Flow<List<AchievementProgressEntity>>

    @Query("SELECT * FROM achievement_progress")
    suspend fun getAll(): List<AchievementProgressEntity>
}
