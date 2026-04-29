package com.mingeek.sudokusage.ui.screens.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.mingeek.sudokusage.domain.board.Board
import com.mingeek.sudokusage.domain.board.CellRef
import com.mingeek.sudokusage.domain.board.Region
import com.mingeek.sudokusage.ui.theme.BoardLineThick
import com.mingeek.sudokusage.ui.theme.BoardLineThickDark
import com.mingeek.sudokusage.ui.theme.BoardLineThin
import com.mingeek.sudokusage.ui.theme.BoardLineThinDark
import com.mingeek.sudokusage.ui.theme.CellConflict
import com.mingeek.sudokusage.ui.theme.CellConflictCB
import com.mingeek.sudokusage.ui.theme.CellHint
import com.mingeek.sudokusage.ui.theme.CellHintCB
import com.mingeek.sudokusage.ui.theme.CellPeer
import com.mingeek.sudokusage.ui.theme.CellPeerCB
import com.mingeek.sudokusage.ui.theme.CellSelected
import com.mingeek.sudokusage.ui.theme.CellSelectedCB

@Composable
fun SudokuBoard(
    board: Board,
    selected: CellRef?,
    conflicts: Set<CellRef>,
    onCellClick: (CellRef) -> Unit,
    onCellLongPress: (CellRef) -> Unit = {},
    boxRows: Int = 3,
    boxCols: Int = 3,
    fontScale: Float = 1f,
    colorBlindMode: Boolean = false,
    modifier: Modifier = Modifier,
    hintFocus: Set<CellRef> = emptySet(),
    cages: List<Region.Cage> = emptyList(),
) {
    val isDark = isSystemInDarkTheme()
    val thinColor = if (isDark) BoardLineThinDark else BoardLineThin
    val thickColor = if (isDark) BoardLineThickDark else BoardLineThick
    val colors = MaterialTheme.colorScheme

    val selectedColor = if (colorBlindMode) CellSelectedCB else CellSelected
    val peerColor = if (colorBlindMode) CellPeerCB else CellPeer
    val conflictColor = if (colorBlindMode) CellConflictCB else CellConflict
    val hintColor = if (colorBlindMode) CellHintCB else CellHint

    val measurer = rememberTextMeasurer()
    val baseValuePx = (160 / board.size).coerceIn(16, 32)
    val baseNotePx = (60 / board.size).coerceIn(8, 14)
    val valueFontSize = (baseValuePx * fontScale).coerceIn(12f, 40f).sp
    val noteFontSize = (baseNotePx * fontScale).coerceIn(7f, 18f).sp
    val givenStyle = TextStyle(
        color = colors.onSurface,
        fontSize = valueFontSize,
        fontWeight = FontWeight.SemiBold,
    )
    val enteredStyle = givenStyle.copy(color = colors.primary, fontWeight = FontWeight.Normal)
    val conflictStyle = givenStyle.copy(color = colors.error)
    val noteStyle = TextStyle(color = colors.onSurfaceVariant, fontSize = noteFontSize)
    val cageLabelStyle = TextStyle(
        color = colors.onSurfaceVariant,
        fontSize = (noteFontSize.value * 1.1f).coerceAtLeast(9f).sp,
        fontWeight = FontWeight.Bold,
    )

    // Map each cell to its cage (if any) + locate the anchor cell (smallest row, then col)
    // for sum-label placement.
    val cellToCage: Map<CellRef, Region.Cage> = remember(cages) {
        cages.flatMap { c -> c.cells.map { it to c } }.toMap()
    }
    val cageAnchors: Map<Region.Cage, CellRef> = remember(cages) {
        cages.associateWith { cage ->
            cage.cells.minWith(compareBy({ it.row }, { it.col }))
        }
    }

    val selectedValue = remember(board, selected) {
        selected?.let { board.cellAt(it).displayValue }
    }
    val size = board.size
    // Note grid layout: prefer the larger dimension as the "row" count so notes 1..N
    // fit in a roughly square block. For 9 → 3×3, 6 → 2×3, 4 → 2×2.
    val noteRows = boxRows
    val noteCols = boxCols

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(size) {
                detectTapGestures(
                    onTap = { offset ->
                        val cellPx = this.size.width / size.toFloat()
                        val r = (offset.y / cellPx).toInt().coerceIn(0, size - 1)
                        val c = (offset.x / cellPx).toInt().coerceIn(0, size - 1)
                        onCellClick(CellRef(r, c))
                    },
                    onLongPress = { offset ->
                        val cellPx = this.size.width / size.toFloat()
                        val r = (offset.y / cellPx).toInt().coerceIn(0, size - 1)
                        val c = (offset.x / cellPx).toInt().coerceIn(0, size - 1)
                        onCellLongPress(CellRef(r, c))
                    },
                )
            }
    ) {
        val cellPx = this.size.width / size

        // 1. Cell highlights
        for (r in 0 until size) {
            for (c in 0 until size) {
                val ref = CellRef(r, c)
                val cell = board.cellAt(r, c)
                val highlight: Color? = when {
                    ref in hintFocus -> hintColor
                    ref in conflicts -> conflictColor
                    ref == selected -> selectedColor
                    selected != null && (
                        r == selected.row ||
                        c == selected.col ||
                        (r / boxRows == selected.row / boxRows && c / boxCols == selected.col / boxCols)
                    ) -> peerColor
                    selectedValue != null && cell.displayValue == selectedValue -> peerColor
                    else -> null
                }
                if (highlight != null) {
                    drawRect(
                        color = highlight,
                        topLeft = Offset(c * cellPx, r * cellPx),
                        size = Size(cellPx, cellPx),
                    )
                }
            }
        }

        // 1.5 Cage borders — dashed inset on edges where neighbor is in a different cage.
        if (cages.isNotEmpty()) {
            val inset = cellPx * 0.06f
            val dash = PathEffect.dashPathEffect(floatArrayOf(cellPx * 0.06f, cellPx * 0.04f), 0f)
            val cageStroke = Stroke(width = 1.5f, pathEffect = dash)
            for (r in 0 until size) {
                for (c in 0 until size) {
                    val ref = CellRef(r, c)
                    val mine = cellToCage[ref] ?: continue
                    val left = c * cellPx + inset
                    val top = r * cellPx + inset
                    val right = (c + 1) * cellPx - inset
                    val bottom = (r + 1) * cellPx - inset
                    // top edge
                    if (cellToCage[CellRef(r - 1, c)] != mine) {
                        drawLine(colors.onSurfaceVariant, Offset(left, top), Offset(right, top),
                            strokeWidth = cageStroke.width, pathEffect = dash)
                    }
                    // bottom edge
                    if (cellToCage[CellRef(r + 1, c)] != mine) {
                        drawLine(colors.onSurfaceVariant, Offset(left, bottom), Offset(right, bottom),
                            strokeWidth = cageStroke.width, pathEffect = dash)
                    }
                    // left edge
                    if (cellToCage[CellRef(r, c - 1)] != mine) {
                        drawLine(colors.onSurfaceVariant, Offset(left, top), Offset(left, bottom),
                            strokeWidth = cageStroke.width, pathEffect = dash)
                    }
                    // right edge
                    if (cellToCage[CellRef(r, c + 1)] != mine) {
                        drawLine(colors.onSurfaceVariant, Offset(right, top), Offset(right, bottom),
                            strokeWidth = cageStroke.width, pathEffect = dash)
                    }
                }
            }
            // Sum labels at anchor cells.
            for ((cage, anchor) in cageAnchors) {
                val layout = measurer.measure(AnnotatedString(cage.targetSum.toString()), cageLabelStyle)
                val x = anchor.col * cellPx + inset + 2f
                val y = anchor.row * cellPx + inset + 1f
                drawText(textLayoutResult = layout, topLeft = Offset(x, y))
            }
        }

        // 2. Grid lines — thick at box boundaries, thin elsewhere.
        for (i in 0..size) {
            // vertical line at column i: thickness driven by boxCols
            val vThick = i % boxCols == 0
            drawLine(
                color = if (vThick) thickColor else thinColor,
                start = Offset(i * cellPx, 0f),
                end = Offset(i * cellPx, this.size.height),
                strokeWidth = if (vThick) 3f else 1f,
            )
            // horizontal line at row i: thickness driven by boxRows
            val hThick = i % boxRows == 0
            drawLine(
                color = if (hThick) thickColor else thinColor,
                start = Offset(0f, i * cellPx),
                end = Offset(this.size.width, i * cellPx),
                strokeWidth = if (hThick) 3f else 1f,
            )
        }

        // 3. Values & notes
        for (r in 0 until size) {
            for (c in 0 until size) {
                val cell = board.cellAt(r, c)
                val ref = CellRef(r, c)
                val cx = (c + 0.5f) * cellPx
                val cy = (r + 0.5f) * cellPx
                val display = cell.displayValue
                if (display != null) {
                    val style = when {
                        ref in conflicts -> conflictStyle
                        cell.isGiven -> givenStyle
                        else -> enteredStyle
                    }
                    val layout = measurer.measure(AnnotatedString(display.toString()), style)
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(cx - layout.size.width / 2f, cy - layout.size.height / 2f),
                    )
                } else if (cell.notes.isNotEmpty()) {
                    for (n in 1..size) {
                        if (n !in cell.notes) continue
                        val nr = (n - 1) / noteCols
                        val nc = (n - 1) % noteCols
                        val nx = c * cellPx + (nc + 0.5f) * cellPx / noteCols
                        val ny = r * cellPx + (nr + 0.5f) * cellPx / noteRows
                        val layout = measurer.measure(AnnotatedString(n.toString()), noteStyle)
                        drawText(
                            textLayoutResult = layout,
                            topLeft = Offset(nx - layout.size.width / 2f, ny - layout.size.height / 2f),
                        )
                    }
                }
            }
        }
    }
}
