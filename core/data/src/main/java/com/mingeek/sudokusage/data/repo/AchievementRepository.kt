package com.mingeek.sudokusage.data.repo

import com.mingeek.sudokusage.data.db.AchievementProgressDao
import com.mingeek.sudokusage.data.db.AchievementProgressEntity
import com.mingeek.sudokusage.domain.achievement.Achievement
import com.mingeek.sudokusage.domain.achievement.AchievementCatalog
import com.mingeek.sudokusage.domain.achievement.AchievementProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AchievementRepository {
    fun observeAll(): Flow<List<AchievementProgress>>
    suspend fun isUnlocked(id: String): Boolean
    suspend fun unlock(id: String)
}

class RoomAchievementRepository(
    private val dao: AchievementProgressDao,
    private val now: () -> Long = System::currentTimeMillis,
) : AchievementRepository {

    override fun observeAll(): Flow<List<AchievementProgress>> =
        dao.observeAll().map { rows ->
            val byId = rows.associateBy { it.achievementId }
            AchievementCatalog.all.map { achievement ->
                achievement.toProgress(byId[achievement.id])
            }
        }

    override suspend fun isUnlocked(id: String): Boolean =
        dao.get(id)?.unlocked == true

    override suspend fun unlock(id: String) {
        if (dao.get(id)?.unlocked == true) return
        dao.upsert(
            AchievementProgressEntity(
                achievementId = id,
                unlocked = true,
                progress = 1,
                unlockedAt = now(),
            )
        )
    }

    private fun Achievement.toProgress(entity: AchievementProgressEntity?): AchievementProgress =
        AchievementProgress(
            achievement = this,
            unlocked = entity?.unlocked == true,
            unlockedAt = entity?.unlockedAt,
        )
}
