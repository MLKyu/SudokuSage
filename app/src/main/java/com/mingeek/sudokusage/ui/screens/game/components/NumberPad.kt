package com.mingeek.sudokusage.ui.screens.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NumberPad(
    digitCounts: Map<Int, Int>,
    boardSize: Int,
    onDigitClick: (Int) -> Unit,
    selectedDigit: Int? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (n in 1..boardSize) {
            val count = digitCounts[n] ?: 0
            val exhausted = count >= boardSize
            DigitKey(
                digit = n,
                remaining = boardSize - count,
                exhausted = exhausted,
                selected = n == selectedDigit,
                onClick = { if (!exhausted) onDigitClick(n) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DigitKey(
    digit: Int,
    remaining: Int,
    exhausted: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(shape)
                .background(
                    when {
                        selected -> colors.secondary
                        exhausted -> colors.surfaceVariant
                        else -> colors.primaryContainer
                    }
                )
                .then(
                    if (selected) Modifier.border(2.dp, colors.onSecondary.copy(alpha = 0.6f), shape)
                    else Modifier
                )
                .clickable(enabled = !exhausted, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = digit.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = when {
                    selected -> colors.onSecondary
                    exhausted -> colors.onSurfaceVariant
                    else -> colors.onPrimaryContainer
                },
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = remaining.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
