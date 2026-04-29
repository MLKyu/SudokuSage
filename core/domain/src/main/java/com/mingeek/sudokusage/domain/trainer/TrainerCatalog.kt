package com.mingeek.sudokusage.domain.trainer

/**
 * Lessons shipped with the app, in pedagogical order (easiest first).
 * Mirrors the technique library in [com.mingeek.sudokusage.game.hint.HintBootstrap].
 */
object TrainerCatalog {

    val NakedSingle = TrainerLesson(
        id = "lesson-naked-single",
        techniqueId = "naked-single",
        name = "내추럴 싱글",
        tagline = "한 셀에 한 숫자만 남으면",
        explanation = """
            가장 기본이 되는 기법이에요.

            한 셀에서 같은 행·열·박스에 들어간 숫자를 모두 제외하면, 가능한 후보가 단 하나만 남는 경우가 있어요. 그 셀에는 망설일 필요 없이 그 숫자를 써 넣으세요.

            매 게임에서 가장 많이 쓰는 기법이고, 다른 기법을 쓴 뒤에 다시 등장하기도 해요.
        """.trimIndent(),
        // 행 0의 1..8이 채워져 (0,0)은 9밖에 못 들어감.
        samplePuzzleEncoded = ".12345678" + ".".repeat(72),
    )

    val HiddenSingle = TrainerLesson(
        id = "lesson-hidden-single",
        techniqueId = "hidden-single",
        name = "히든 싱글",
        tagline = "한 영역에서 그 숫자가 들어갈 자리가 한 곳뿐이면",
        explanation = """
            셀 단위가 아니라 한 영역(행·열·박스) 단위로 보세요.

            한 영역 안에서 어떤 숫자가 들어갈 수 있는 칸이 단 한 곳뿐이라면, 다른 후보가 더 있어 보여도 그 숫자가 그 자리에 들어갑니다. 보통 행이나 열의 빈 자리가 많을 때 박스의 같은 숫자들이 만들어 낸 제약을 보면 보여요.
        """.trimIndent(),
        // 행 0은 비어 있지만 열 1..8 각각에 9가 다른 박스에 흩어져 있어 행 0에서 9는
        // (0,0)에만 들어갈 수 있음 — 히든 싱글 발화 조건.
        samplePuzzleEncoded = (
            "........." +
            "...9....." +
            "......9.." +
            ".9......." +
            "....9...." +
            ".......9." +
            "..9......" +
            ".....9..." +
            "........9"
        ),
    )

    val NakedPair = TrainerLesson(
        id = "lesson-naked-pair",
        techniqueId = "naked-pair",
        name = "네이키드 페어",
        tagline = "두 셀이 같은 두 숫자만 가지면",
        explanation = """
            한 영역 안의 두 셀이 정확히 같은 두 후보(예: 3과 7)만 갖는다면, 그 두 셀이 어떤 순서든 그 두 숫자를 차지하게 돼요.

            그러므로 같은 영역의 다른 셀에서는 3과 7이 후보가 될 수 없어요. 노트를 정리해 보세요. 곧 내추럴 싱글이나 히든 싱글이 보일 거예요.
        """.trimIndent(),
    )

    val PointingPair = TrainerLesson(
        id = "lesson-pointing-pair",
        techniqueId = "pointing-pair",
        name = "포인팅 페어",
        tagline = "박스 안 한 숫자가 한 줄에만 모일 때",
        explanation = """
            한 박스에서 어떤 숫자가 들어갈 수 있는 칸들이 모두 같은 행(또는 열) 위에 있다면, 그 박스의 그 숫자는 반드시 그 행(열)의 박스 안 칸에 들어가요.

            결과적으로 같은 행(열)의 다른 박스에서는 그 숫자를 후보에서 제거할 수 있어요. 행/열을 가로질러 박스 사이 간섭을 발견하는 첫 단계입니다.
        """.trimIndent(),
    )

    val all: List<TrainerLesson> = listOf(NakedSingle, HiddenSingle, NakedPair, PointingPair)
}
