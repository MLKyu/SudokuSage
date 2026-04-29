package com.mingeek.sudokusage.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSaveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GameSaveEntity)

    @Query("SELECT * FROM game_saves WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = GameSaveEntity.SINGLETON_ID): GameSaveEntity?

    @Query("SELECT * FROM game_saves WHERE id = :id LIMIT 1")
    fun observe(id: Int = GameSaveEntity.SINGLETON_ID): Flow<GameSaveEntity?>

    @Query("DELETE FROM game_saves WHERE id = :id")
    suspend fun clear(id: Int = GameSaveEntity.SINGLETON_ID)
}
