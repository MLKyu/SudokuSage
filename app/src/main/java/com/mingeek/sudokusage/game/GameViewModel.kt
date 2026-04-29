package com.mingeek.sudokusage.game

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mingeek.sudokusage.audio.AudioController
import com.mingeek.sudokusage.audio.BgmTrack
import com.mingeek.sudokusage.audio.SoundEvent
import com.mingeek.sudokusage.data.preferences.GameplaySettings
import com.mingeek.sudokusage.data.preferences.InputMode
import com.mingeek.sudokusage.data.repo.DailyChallengeRepository
import com.mingeek.sudokusage.data.repo.GameSaveRepository
import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.GameState
import com.mingeek.sudokusage.domain.board.GameStatus
import com.mingeek.sudokusage.domain.board.Move
import com.mingeek.sudokusage.domain.board.RuleSet
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.domain.board.VariantRegistry
import com.mingeek.sudokusage.domain.event.GameEvent
import com.mingeek.sudokusage.domain.event.GameEventBus
import com.mingeek.sudokusage.domain.event.PuzzleResult
import com.mingeek.sudokusage.domain.hint.HintEngine
import com.mingeek.sudokusage.feedback.Feedback
import com.mingeek.sudokusage.monetization.AdProvider
import com.mingeek.sudokusage.monetization.EntitlementGate
import com.mingeek.sudokusage.variant.killer.KillerRuleSet
import com.mingeek.sudokusage.ui.navigation.NavArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GameViewModel(
    savedStateHandle: SavedStateHandle,
    private val feedback: Feedback,
    private val audio: AudioController,
    private val saves: GameSaveRepository,
    private val variantRegistry: VariantRegistry,
    private val gameEventBus: GameEventBus,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val hintEngine: HintEngine,
    private val gameplaySettings: GameplaySettings,
    private val entitlementGate: EntitlementGate,
    private val adProvider: AdProvider,
    private val lastFinishedGame: MutableStateFlow<GameState?>,
    private val appScope: CoroutineScope,
) : ViewModel() {

    private val launch: GameLaunch = NavArgs.gameLaunchFrom(savedStateHandle)

    private lateinit var rules: RuleSet
    private lateinit var engine: GameEngine

    private val _state = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val game = try {
                withContext(Dispatchers.Default) { resolveInitialGame() }
            } catch (e: Exception) {
                _state.value = GameUiState.Error(e.message ?: "게임을 불러올 수 없습니다")
                return@launch
            }
            rules = rulesFor(game)
            engine = GameEngine(rules, gameEventBus)

            _state.value = GameUiState.Ready(
                game = game,
                conflicts = rules.conflicts(game.board),
                digitCounts = digitCountsOf(game.board),
                boxRows = rules.boxRows,
                boxCols = rules.boxCols,
            )
            audio.playBgm(BgmTrack.Puzzle)
            if (launch !is GameLaunch.Resume) {
                gameEventBus.emit(
                    GameEvent.PuzzleStarted(game.variant, game.difficulty, System.currentTimeMillis())
                )
            }
            game.dailyDate?.let { date ->
                val alreadyRecorded = dailyChallengeRepository.get(date) != null
                if (!alreadyRecorded) attachDailyRecorder(date)
            }
            startTimer()
            attachAutoSave()
            attachEntitlementWatcher()
            attachPreferencesWatcher()
        }
    }

    private fun attachEntitlementWatcher() {
        viewModelScope.launch {
            entitlementGate.isPro.collect { pro ->
                mutate { it.copy(isPro = pro) }
            }
        }
    }

    private fun attachPreferencesWatcher() {
        viewModelScope.launch {
            gameplaySettings.state.collect { prefs ->
                mutate {
                    it.copy(
                        fontScale = prefs.fontScale,
                        colorBlindMode = prefs.colorBlindMode,
                        inputMode = prefs.inputMode,
                        selectedDigit = if (prefs.inputMode == InputMode.CellFirst) null else it.selectedDigit,
                    )
                }
            }
        }
    }

    private fun attachDailyRecorder(date: LocalDate) {
        viewModelScope.launch {
            val terminal = _state
                .filterIsInstance<GameUiState.Ready>()
                .map { it.game.status }
                .first { it == GameStatus.Won || it == GameStatus.Failed }
            val game = current()?.game ?: return@launch
            dailyChallengeRepository.record(
                date = date,
                result = if (terminal == GameStatus.Won) PuzzleResult.Won else PuzzleResult.Failed,
                elapsedMs = game.elapsedMs,
                mistakes = game.mistakes,
                seed = game.seed,
                variant = game.variant,
                difficulty = game.difficulty,
            )
        }
    }

    private suspend fun resolveInitialGame(): GameState = when (val l = launch) {
        is GameLaunch.New -> {
            val limit = currentMistakeLimit()
            val puzzle = variantRegistry.generator(l.variantId).generate(
                l.variantId, l.difficulty, l.seed
            )
            val now = System.currentTimeMillis()
            GameState(
                initial = puzzle.initial,
                board = puzzle.initial,
                solution = puzzle.solution,
                variant = l.variantId,
                difficulty = l.difficulty,
                startedAt = now,
                seed = puzzle.seed,
                mistakeLimit = limit,
                cages = puzzle.cages,
            )
        }
        is GameLaunch.Daily -> {
            val limit = currentMistakeLimit()
            val config = dailyChallengeRepository.configFor(l.date)
            val puzzle = variantRegistry.generator(config.variant).generate(
                config.variant, config.difficulty, config.seed
            )
            val now = System.currentTimeMillis()
            GameState(
                initial = puzzle.initial,
                board = puzzle.initial,
                solution = puzzle.solution,
                variant = config.variant,
                difficulty = config.difficulty,
                startedAt = now,
                seed = puzzle.seed,
                dailyDate = l.date,
                mistakeLimit = limit,
                cages = puzzle.cages,
            )
        }
        GameLaunch.Resume -> saves.load()
            ?: error("Resume requested but no saved game exists")
        is GameLaunch.Trainer -> {
            val lesson = com.mingeek.sudokusage.domain.trainer.TrainerCatalog.all
                .firstOrNull { it.id == l.lessonId }
                ?: error("Unknown trainer lesson: ${l.lessonId}")
            val encoded = lesson.samplePuzzleEncoded
                ?: error("Lesson ${l.lessonId} has no sample puzzle")
            val initial = com.mingeek.sudokusage.data.codec.BoardCodec.decodeAsGivens(encoded)
            val initialArray = IntArray(81) { i -> initial.cells[i].displayValue ?: 0 }
            val solver = com.mingeek.sudokusage.domain.board.GenericSolver(
                com.mingeek.sudokusage.variant.classic.ClassicRuleSet()
            )
            val solutionArray = solver.solveOne(initialArray)
                ?: error("Sample puzzle for ${l.lessonId} is unsolvable")
            val solutionEncoded = solutionArray.joinToString("") { it.toString() }
            val solution = com.mingeek.sudokusage.data.codec.BoardCodec.decodeAsGivens(solutionEncoded)
            val now = System.currentTimeMillis()
            GameState(
                initial = initial,
                board = initial,
                solution = solution,
                variant = VariantId.Classic,
                difficulty = Difficulty.Easy,
                startedAt = now,
                seed = l.lessonId.hashCode().toLong(),
                mistakeLimit = null,
            )
        }
    }

    private fun rulesFor(state: GameState): RuleSet =
        if (state.variant == VariantId.Killer && state.cages.isNotEmpty()) {
            KillerRuleSet(state.cages)
        } else {
            variantRegistry.rules(state.variant)
        }

    private suspend fun currentMistakeLimit(): Int? {
        val prefs = gameplaySettings.state.first()
        return if (prefs.mistakeLimitEnabled) 3 else null
    }

    @OptIn(FlowPreview::class)
    private fun attachAutoSave() {
        viewModelScope.launch {
            _state
                .filterIsInstance<GameUiState.Ready>()
                .map { it.game }
                .debounce(500L)
                .collect { game ->
                    if (game.status == GameStatus.Won || game.status == GameStatus.Failed) {
                        saves.clear()
                        lastFinishedGame.value = game
                    } else {
                        saves.save(game)
                    }
                }
        }
    }

    fun selectCell(ref: CellRef) {
        val ready = current() ?: return
        val digit = ready.selectedDigit
        if (ready.inputMode == InputMode.NumberFirst && digit != null) {
            mutate { it.copy(selected = ref) }
            applyDigitToCell(ref, digit)
        } else {
            mutate { it.copy(selected = ref) }
            feedback.emit(SoundEvent.CellSelect)
        }
        gameEventBus.emit(GameEvent.CellSelected(ref, System.currentTimeMillis()))
    }

    fun longPressCell(ref: CellRef) {
        mutate { it.copy(selected = ref, noteMode = true) }
        feedback.emit(SoundEvent.UiToggle)
        gameEventBus.emit(GameEvent.CellSelected(ref, System.currentTimeMillis()))
    }

    fun input(value: Int) {
        val ready = current() ?: return
        when (ready.inputMode) {
            InputMode.CellFirst -> {
                val sel = ready.selected ?: return
                applyDigitToCell(sel, value)
            }
            InputMode.NumberFirst -> {
                mutate {
                    it.copy(selectedDigit = if (it.selectedDigit == value) null else value)
                }
                feedback.emit(SoundEvent.UiTap)
            }
        }
    }

    private fun applyDigitToCell(ref: CellRef, value: Int) {
        val ready = current() ?: return
        val cell = ready.game.board.cellAt(ref)
        if (cell.isGiven) return
        if (ready.noteMode) {
            applyMove(Move.ToggleNote(ref, value))
            feedback.emit(SoundEvent.NoteToggle)
        } else {
            val mistakesBefore = ready.game.mistakes
            val statusBefore = ready.game.status
            applyMove(Move.Place(ref, value))
            val after = current()?.game ?: return
            when {
                after.status == GameStatus.Won && statusBefore != GameStatus.Won ->
                    feedback.emit(SoundEvent.PuzzleComplete)
                after.mistakes > mistakesBefore -> feedback.emit(SoundEvent.Mistake)
                else -> feedback.emit(SoundEvent.NumberPlace)
            }
        }
    }

    fun erase() {
        val ready = current() ?: return
        val sel = ready.selected ?: return
        if (ready.game.board.cellAt(sel).isGiven) return
        applyMove(Move.Erase(sel))
        feedback.emit(SoundEvent.NumberErase)
    }

    fun requestHint() {
        val ready = current() ?: return
        if (ready.game.status != GameStatus.Playing) return
        when (val st = ready.hintState) {
            HintState.None -> {
                val canHint = ready.isPro ||
                    ready.game.hintsUsed < FREE_HINT_LIMIT ||
                    ready.rewardedHintAvailable
                if (!canHint) {
                    mutate { it.copy(hintGateOpen = true) }
                    feedback.emit(SoundEvent.UiOpen)
                    return
                }
                val hint = hintEngine.nextHint(ready.game.board, rules) ?: return
                mutate { current ->
                    val consumesReward = !current.isPro &&
                        current.game.hintsUsed >= FREE_HINT_LIMIT &&
                        current.rewardedHintAvailable
                    current.copy(
                        hintState = HintState.Active(hint, level = 1),
                        rewardedHintAvailable = if (consumesReward) false else current.rewardedHintAvailable,
                    )
                }
                feedback.emit(SoundEvent.HintReveal)
            }
            is HintState.Active -> when (st.level) {
                1 -> {
                    mutate { it.copy(hintState = st.copy(level = 2)) }
                    feedback.emit(SoundEvent.UiTap)
                }
                else -> applyHint(st.hint)
            }
        }
    }

    fun dismissHintGate() {
        mutate { it.copy(hintGateOpen = false) }
        feedback.emit(SoundEvent.UiClose)
    }

    fun watchAdForHint(activity: Activity) {
        viewModelScope.launch {
            mutate { it.copy(hintGateOpen = false) }
            val granted = adProvider.showRewarded(activity)
            if (granted) {
                mutate { it.copy(rewardedHintAvailable = true) }
                requestHint()
            } else {
                feedback.emit(SoundEvent.Mistake)
            }
        }
    }

    private fun applyHint(hint: com.mingeek.sudokusage.domain.hint.Hint) {
        val before = current() ?: return
        val newGameAfterMove = hint.resultMove?.let { engine.apply(before.game, it) } ?: before.game
        val finalBoard = if (hint.eliminations.isNotEmpty() && hint.resultMove == null) {
            applyEliminationsToBoard(newGameAfterMove.board, hint.eliminations)
        } else {
            newGameAfterMove.board
        }
        val finalGame = newGameAfterMove.copy(
            board = finalBoard,
            hintsUsed = newGameAfterMove.hintsUsed + 1,
        )
        mutate {
            it.copy(
                game = finalGame,
                conflicts = rules.conflicts(finalGame.board),
                digitCounts = digitCountsOf(finalGame.board),
                hintState = HintState.None,
            )
        }
        gameEventBus.emit(
            GameEvent.HintUsed(hint.techniqueId, System.currentTimeMillis())
        )
        feedback.emit(SoundEvent.HintReveal)
    }

    private fun applyEliminationsToBoard(
        board: Board,
        eliminations: List<Pair<CellRef, Int>>,
    ): Board {
        var b = board
        for ((ref, digit) in eliminations) {
            val cell = b.cellAt(ref)
            if (digit in cell.notes) {
                b = b.withCell(cell.copy(notes = cell.notes - digit))
            }
        }
        return b
    }

    fun toggleNoteMode() {
        mutate { it.copy(noteMode = !it.noteMode) }
        feedback.emit(SoundEvent.UiToggle)
    }

    fun undo() {
        val ready = current() ?: return
        if (ready.game.moveHistory.isEmpty()) return
        val newGame = engine.undo(ready.game)
        mutate { it.copy(game = newGame, conflicts = rules.conflicts(newGame.board), digitCounts = digitCountsOf(newGame.board)) }
        feedback.emit(SoundEvent.UiBack)
    }

    fun redo() {
        val ready = current() ?: return
        if (ready.game.redoStack.isEmpty()) return
        val newGame = engine.redo(ready.game)
        mutate { it.copy(game = newGame, conflicts = rules.conflicts(newGame.board), digitCounts = digitCountsOf(newGame.board)) }
        feedback.emit(SoundEvent.UiTap)
    }

    fun pause() {
        val ready = current() ?: return
        if (ready.game.status != GameStatus.Playing) return
        mutate { it.copy(game = engine.pause(it.game)) }
        timerJob?.cancel()
        feedback.emit(SoundEvent.UiToggle)
    }

    fun resume() {
        val ready = current() ?: return
        if (ready.game.status != GameStatus.Paused) return
        mutate { it.copy(game = engine.resume(it.game)) }
        startTimer()
        feedback.emit(SoundEvent.UiTap)
    }

    private fun applyMove(move: Move) {
        val ready = current() ?: return
        val newGame = engine.apply(ready.game, move)
        mutate {
            it.copy(
                game = newGame,
                conflicts = rules.conflicts(newGame.board),
                digitCounts = digitCountsOf(newGame.board),
            )
        }
    }

    private fun current(): GameUiState.Ready? = _state.value as? GameUiState.Ready

    private fun mutate(block: (GameUiState.Ready) -> GameUiState.Ready) {
        _state.update { current -> if (current is GameUiState.Ready) block(current) else current }
    }

    private fun digitCountsOf(board: Board): Map<Int, Int> {
        val counts = HashMap<Int, Int>(rules.symbols.last)
        for (cell in board.cells) {
            val v = cell.displayValue ?: continue
            counts[v] = (counts[v] ?: 0) + 1
        }
        return counts
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val ready = current() ?: continue
                if (ready.game.status != GameStatus.Playing) continue
                mutate { it.copy(game = it.game.copy(elapsedMs = it.game.elapsedMs + 1000L)) }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        val ready = current()
        if (ready != null) {
            appScope.launch {
                if (ready.game.status == GameStatus.Won || ready.game.status == GameStatus.Failed) {
                    saves.clear()
                } else {
                    saves.save(ready.game)
                }
            }
        }
        super.onCleared()
    }

    private companion object {
        const val FREE_HINT_LIMIT = 3
    }
}
