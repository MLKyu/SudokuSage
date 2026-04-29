package com.mingeek.sudokusage.data.repo

import com.mingeek.sudokusage.data.codec.BoardCodec
import com.mingeek.sudokusage.data.codec.CageCodec
import com.mingeek.sudokusage.data.codec.MoveCodec
import com.mingeek.sudokusage.data.codec.NotesCodec
import com.mingeek.sudokusage.data.db.GameSaveDao
import com.mingeek.sudokusage.data.db.GameSaveEntity
import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.GameState
import com.mingeek.sudokusage.domain.board.GameStatus
import com.mingeek.sudokusage.domain.board.VariantId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

interface GameSaveRepository {
    suspend fun save(state: GameState)
    suspend fun load(): GameState?
    fun observe(): Flow<GameState?>
    suspend fun clear()
}

class RoomGameSaveRepository(
    private val dao: GameSaveDao,
    private val now: () -> Long = System::currentTimeMillis,
) : GameSaveRepository {

    override suspend fun save(state: GameState) = dao.upsert(toEntity(state))

    override suspend fun load(): GameState? = dao.get()?.let(::fromEntity)

    override fun observe(): Flow<GameState?> = dao.observe().map { it?.let(::fromEntity) }

    override suspend fun clear() = dao.clear()

    private fun toEntity(state: GameState): GameSaveEntity = GameSaveEntity(
        variant = state.variant.value,
        difficulty = state.difficulty.name,
        seed = state.seed,
        initialBoard = BoardCodec.encodeValues(state.initial),
        currentValues = BoardCodec.encodeValues(state.board),
        currentNotes = NotesCodec.encode(state.board),
        solution = BoardCodec.encodeValues(state.solution),
        moveHistory = MoveCodec.encode(state.moveHistory),
        redoStack = MoveCodec.encode(state.redoStack),
        mistakes = state.mistakes,
        mistakeLimit = state.mistakeLimit,
        elapsedMs = state.elapsedMs,
        hintsUsed = state.hintsUsed,
        status = state.status.name,
        startedAt = state.startedAt,
        updatedAt = now(),
        dailyDate = state.dailyDate?.toString(),
        cages = CageCodec.encode(state.cages),
    )

    private fun fromEntity(e: GameSaveEntity): GameState {
        val initial = BoardCodec.decodeAsGivens(e.initialBoard)
        val solution = BoardCodec.decodeAsGivens(e.solution)
        val merged = mergeCurrent(initial, e.currentValues)
        val board = NotesCodec.applyTo(merged, e.currentNotes)
        return GameState(
            initial = initial,
            board = board,
            solution = solution,
            variant = VariantId(e.variant),
            difficulty = Difficulty.valueOf(e.difficulty),
            mistakes = e.mistakes,
            mistakeLimit = e.mistakeLimit,
            elapsedMs = e.elapsedMs,
            moveHistory = MoveCodec.decode(e.moveHistory),
            redoStack = MoveCodec.decode(e.redoStack),
            hintsUsed = e.hintsUsed,
            status = GameStatus.valueOf(e.status),
            startedAt = e.startedAt,
            seed = e.seed,
            dailyDate = e.dailyDate?.let(LocalDate::parse),
            cages = CageCodec.decode(e.cages),
        )
    }

    private fun mergeCurrent(initial: Board, currentValues: String): Board {
        require(currentValues.length == initial.size * initial.size)
        val cells = (0 until initial.cells.size).map { i ->
            val r = i / initial.size
            val c = i % initial.size
            val cell = initial.cellAt(r, c)
            if (cell.isGiven) {
                cell
            } else {
                val v = currentValues[i].takeIf { it != '.' }?.digitToInt()
                cell.copy(value = v)
            }
        }
        return Board(initial.size, cells)
    }
}
