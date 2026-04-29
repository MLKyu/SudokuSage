package com.mingeek.sudokusage.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mingeek.sudokusage.R
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.GameStatus
import com.mingeek.sudokusage.game.GameUiState
import com.mingeek.sudokusage.game.GameViewModel
import com.mingeek.sudokusage.game.HintState
import com.mingeek.sudokusage.ui.screens.game.components.GameActionRow
import com.mingeek.sudokusage.ui.screens.game.components.GameTopBar
import com.mingeek.sudokusage.ui.screens.game.components.NumberPad
import com.mingeek.sudokusage.ui.screens.game.components.SudokuBoard

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onExit: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenReplay: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (val s = state) {
                is GameUiState.Loading -> LoadingContent()
                is GameUiState.Error -> ErrorContent(message = s.message, onExit = onExit)
                is GameUiState.Ready -> ReadyContent(
                    ui = s,
                    onCellClick = viewModel::selectCell,
                    onCellLongPress = viewModel::longPressCell,
                    onDigit = viewModel::input,
                    onErase = viewModel::erase,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onToggleNote = viewModel::toggleNoteMode,
                    onPause = viewModel::pause,
                    onResume = viewModel::resume,
                    onExit = onExit,
                    onHint = viewModel::requestHint,
                    onOpenReplay = onOpenReplay,
                )
            }
            val ready = state as? GameUiState.Ready
            if (ready?.hintGateOpen == true) {
                HintGateDialog(
                    onWatchAd = {
                        (context as? android.app.Activity)?.let(viewModel::watchAdForHint)
                    },
                    onUpgrade = {
                        viewModel.dismissHintGate()
                        onOpenPro()
                    },
                    onCancel = viewModel::dismissHintGate,
                )
            }
        }
    }
}

@Composable
private fun HintGateDialog(
    onWatchAd: () -> Unit,
    onUpgrade: () -> Unit,
    onCancel: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("힌트를 더 받으려면") },
        text = {
            Text("이번 게임의 무료 힌트를 모두 사용했어요. 광고를 보고 1회 더 받거나, Pro로 업그레이드해 무제한으로 사용하세요.")
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onUpgrade) { Text("Pro 업그레이드") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onWatchAd) { Text("광고 보고 받기") }
        },
    )
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))
            Text("퍼즐을 만드는 중...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ErrorContent(message: String, onExit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onExit) { Text("뒤로") }
        }
    }
}

@Composable
private fun ReadyContent(
    ui: GameUiState.Ready,
    onCellClick: (com.mingeek.sudokusage.domain.board.CellRef) -> Unit,
    onCellLongPress: (com.mingeek.sudokusage.domain.board.CellRef) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleNote: () -> Unit,
    onHint: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onExit: () -> Unit,
    onOpenReplay: () -> Unit,
) {
    val game = ui.game
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp
        Column(modifier = Modifier.fillMaxSize()) {
            GameTopBar(
                difficultyLabel = stringResource(difficultyLabelRes(game.difficulty)),
                mistakes = game.mistakes,
                mistakeLimit = game.mistakeLimit,
                elapsedMs = game.elapsedMs,
                status = game.status,
                onBack = onExit,
                onTogglePause = {
                    if (game.status == GameStatus.Playing) onPause() else onResume()
                },
            )
            Spacer(Modifier.height(8.dp))
            HintBanner(state = ui.hintState)

            if (isWide) {
                androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxSize()) {
                    BoardArea(
                        ui = ui,
                        onCellClick = onCellClick,
                        onCellLongPress = onCellLongPress,
                        onResume = onResume,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        GameActionRow(
                            canUndo = game.moveHistory.isNotEmpty(),
                            canRedo = game.redoStack.isNotEmpty(),
                            noteMode = ui.noteMode,
                            onUndo = onUndo,
                            onRedo = onRedo,
                            onErase = onErase,
                            onToggleNote = onToggleNote,
                            onHint = onHint,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        NumberPad(
                            digitCounts = ui.digitCounts,
                            boardSize = game.board.size,
                            onDigitClick = onDigit,
                            selectedDigit = ui.selectedDigit,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            } else {
                BoardArea(
                    ui = ui,
                    onCellClick = onCellClick,
                    onCellLongPress = onCellLongPress,
                    onResume = onResume,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                GameActionRow(
                    canUndo = game.moveHistory.isNotEmpty(),
                    canRedo = game.redoStack.isNotEmpty(),
                    noteMode = ui.noteMode,
                    onUndo = onUndo,
                    onRedo = onRedo,
                    onErase = onErase,
                    onToggleNote = onToggleNote,
                    onHint = onHint,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                Spacer(Modifier.height(16.dp))
                NumberPad(
                    digitCounts = ui.digitCounts,
                    boardSize = game.board.size,
                    onDigitClick = onDigit,
                    selectedDigit = ui.selectedDigit,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        when (game.status) {
            GameStatus.Won -> WinDialog(
                elapsedMs = game.elapsedMs,
                mistakes = game.mistakes,
                onExit = onExit,
                onReplay = onOpenReplay,
            )
            GameStatus.Failed -> FailDialog(onExit = onExit, onReplay = onOpenReplay)
            else -> Unit
        }
    }
}

@Composable
private fun BoardArea(
    ui: GameUiState.Ready,
    onCellClick: (com.mingeek.sudokusage.domain.board.CellRef) -> Unit,
    onCellLongPress: (com.mingeek.sudokusage.domain.board.CellRef) -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val game = ui.game
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        if (game.status == GameStatus.Paused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "일시정지",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onResume) { Text("계속하기") }
                }
            }
        } else {
            val hintFocus = (ui.hintState as? HintState.Active)?.hint?.focusCells?.toSet().orEmpty()
            SudokuBoard(
                board = game.board,
                selected = ui.selected,
                conflicts = ui.conflicts,
                onCellClick = onCellClick,
                onCellLongPress = onCellLongPress,
                boxRows = ui.boxRows,
                boxCols = ui.boxCols,
                fontScale = ui.fontScale,
                colorBlindMode = ui.colorBlindMode,
                modifier = Modifier.fillMaxWidth(),
                hintFocus = hintFocus,
                cages = game.cages,
            )
        }
    }
}

@Composable
private fun WinDialog(elapsedMs: Long, mistakes: Int, onExit: () -> Unit, onReplay: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("🎉 클리어!") },
        text = {
            val total = elapsedMs / 1000
            Text("시간: ${total / 60}:${"%02d".format(total % 60)}\n실수: $mistakes")
        },
        confirmButton = {
            TextButton(onClick = onExit) { Text("홈으로") }
        },
        dismissButton = {
            TextButton(onClick = onReplay) { Text("검토") }
        },
    )
}

@Composable
private fun FailDialog(onExit: () -> Unit, onReplay: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("실수 한도 초과") },
        text = { Text("3번 이상 실수했어요. 다시 도전해 보세요.") },
        confirmButton = {
            TextButton(onClick = onExit) { Text("홈으로") }
        },
        dismissButton = {
            TextButton(onClick = onReplay) { Text("검토") }
        },
    )
}

@Composable
private fun HintBanner(state: HintState) {
    if (state !is HintState.Active || state.level < 2) return
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = state.hint.techniqueName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = state.hint.explanation,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "💡 한 번 더 누르면 적용",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

private fun difficultyLabelRes(d: Difficulty): Int = when (d) {
    Difficulty.Easy -> R.string.difficulty_easy
    Difficulty.Medium -> R.string.difficulty_medium
    Difficulty.Hard -> R.string.difficulty_hard
    Difficulty.Expert -> R.string.difficulty_expert
    Difficulty.Master -> R.string.difficulty_master
    Difficulty.Extreme -> R.string.difficulty_extreme
}
