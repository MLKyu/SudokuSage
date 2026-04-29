package com.mingeek.sudokusage.ui.screens.replay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mingeek.sudokusage.ui.screens.game.components.SudokuBoard

@Composable
fun ReplayScreen(
    viewModel: ReplayViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "검토",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        when (val s = state) {
            null -> EmptyState()
            else -> Body(state = s, onSetStep = viewModel::setStep, onBack = viewModel::stepBack, onForward = viewModel::stepForward)
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "검토할 게임이 없어요. 한 게임을 먼저 완료해 보세요.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Body(
    state: ReplayUiState,
    onSetStep: (Int) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SudokuBoard(
                board = state.board,
                selected = null,
                conflicts = emptySet(),
                onCellClick = {},
                boxRows = state.boxRows,
                boxCols = state.boxCols,
                modifier = Modifier.fillMaxWidth(),
                cages = state.game.cages,
            )
        }
        Spacer(Modifier.height(8.dp))

        StepCounter(step = state.step, total = state.totalSteps)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, enabled = state.step > 0) {
                Icon(Icons.AutoMirrored.Default.NavigateBefore, contentDescription = "이전")
            }
            Slider(
                value = state.step.toFloat(),
                onValueChange = { onSetStep(it.toInt()) },
                valueRange = 0f..state.totalSteps.toFloat().coerceAtLeast(1f),
                steps = (state.totalSteps - 1).coerceAtLeast(0),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onForward, enabled = state.step < state.totalSteps) {
                Icon(Icons.AutoMirrored.Default.NavigateNext, contentDescription = "다음")
            }
        }

        SummaryRow(state = state)
    }
}

@Composable
private fun StepCounter(step: Int, total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$step / $total 단계",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun SummaryRow(state: ReplayUiState) {
    val game = state.game
    val totalSeconds = game.elapsedMs / 1000
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SummaryStat("시간", "%d:%02d".format(totalSeconds / 60, totalSeconds % 60))
        SummaryStat("실수", game.mistakes.toString())
        SummaryStat("힌트", game.hintsUsed.toString())
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
