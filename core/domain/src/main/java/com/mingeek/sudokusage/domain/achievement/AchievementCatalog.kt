package com.mingeek.sudokusage.domain.achievement

/**
 * The full list of achievements the app ships with. Adding a new one:
 *   1. Append an entry here (stable id; never rename).
 *   2. Add a matching [com.mingeek.sudokusage.data.achievement.AchievementRule] that
 *      decides when it unlocks.
 *
 * UI iterates this list in declaration order.
 */
object AchievementCatalog {

    val FirstWin = Achievement(
        id = "first-win",
        name = "첫 승리",
        description = "첫 게임을 클리어했어요",
    )
    val PerfectGame = Achievement(
        id = "perfect-game",
        name = "완벽한 한 판",
        description = "실수 없이 한 게임을 클리어했어요",
    )
    val MasterTier = Achievement(
        id = "master-tier",
        name = "마스터 입문",
        description = "마스터 난이도 한 게임을 클리어했어요",
    )
    val ExtremeTier = Achievement(
        id = "extreme-tier",
        name = "익스트림 정복",
        description = "익스트림 난이도 한 게임을 클리어했어요",
    )
    val DailyFirst = Achievement(
        id = "daily-first",
        name = "오늘의 도전 시작",
        description = "첫 일일 챌린지를 완료했어요",
    )
    val HintNovice = Achievement(
        id = "hint-novice",
        name = "현자의 가르침",
        description = "처음으로 힌트를 사용했어요",
    )

    val all: List<Achievement> = listOf(
        FirstWin, PerfectGame, MasterTier, ExtremeTier, DailyFirst, HintNovice,
    )
}
