package com.mingeek.sudokusage.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mingeek.sudokusage.data.preferences.InputMode
import com.mingeek.sudokusage.data.preferences.ThemeChoice
import com.mingeek.sudokusage.data.preferences.ThemePalette

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onOpenPro: () -> Unit = {},
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
                text = "설정",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionLabel("게임")
            SettingCard {
                ThemePicker(current = state.gameplay.theme, onChoose = viewModel::setTheme)
            }
            SettingCard {
                PalettePicker(current = state.gameplay.palette, onChoose = viewModel::setPalette)
            }
            SettingCard {
                InputModePicker(current = state.gameplay.inputMode, onChoose = viewModel::setInputMode)
            }
            SettingCard {
                ToggleRow(
                    label = "실수 한도 (3회)",
                    checked = state.gameplay.mistakeLimitEnabled,
                    onChange = viewModel::toggleMistakeLimit,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (state.gameplay.mistakeLimitEnabled) {
                        "3회 이상 실수하면 게임이 끝나요."
                    } else {
                        "실수해도 게임이 계속돼요. 통계엔 그대로 기록됩니다."
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionLabel("접근성")
            SettingCard {
                FontScalePicker(current = state.gameplay.fontScale, onChoose = viewModel::setFontScale)
            }
            SettingCard {
                ToggleRow("색맹 친화 색상", state.gameplay.colorBlindMode, viewModel::toggleColorBlind)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "셀 강조와 충돌 표시를 파랑·주황 계열로 바꿔 적록 색맹에서도 잘 보이게 해요.",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionLabel("사운드 & 진동")
            SettingCard {
                ToggleRow("배경 음악", state.audio.bgmEnabled, viewModel::toggleBgm)
                if (state.audio.bgmEnabled) {
                    VolumeSlider(state.audio.bgmVolume, viewModel::setBgmVolume)
                }
            }
            SettingCard {
                ToggleRow("효과음", state.audio.sfxEnabled, viewModel::toggleSfx)
                if (state.audio.sfxEnabled) {
                    VolumeSlider(state.audio.sfxVolume, viewModel::setSfxVolume)
                }
            }
            SettingCard {
                ToggleRow("진동", state.audio.hapticEnabled, viewModel::toggleHaptic)
            }

            Spacer(Modifier.height(8.dp))
            ProEntryRow(onClick = onOpenPro)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun ThemePicker(current: ThemeChoice, onChoose: (ThemeChoice) -> Unit) {
    Column {
        Text(
            text = "테마",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeChoiceChip(label = "시스템", selected = current == ThemeChoice.System, onClick = { onChoose(ThemeChoice.System) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "라이트", selected = current == ThemeChoice.Light, onClick = { onChoose(ThemeChoice.Light) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "다크", selected = current == ThemeChoice.Dark, onClick = { onChoose(ThemeChoice.Dark) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "AMOLED", selected = current == ThemeChoice.Amoled, onClick = { onChoose(ThemeChoice.Amoled) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PalettePicker(current: ThemePalette, onChoose: (ThemePalette) -> Unit) {
    Column {
        Text(
            text = "색상 팔레트",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeChoiceChip(label = "Sage", selected = current == ThemePalette.Sage, onClick = { onChoose(ThemePalette.Sage) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "Forest", selected = current == ThemePalette.Forest, onClick = { onChoose(ThemePalette.Forest) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "Ocean", selected = current == ThemePalette.Ocean, onClick = { onChoose(ThemePalette.Ocean) }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeChoiceChip(label = "Sunset", selected = current == ThemePalette.Sunset, onClick = { onChoose(ThemePalette.Sunset) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "Lavender", selected = current == ThemePalette.Lavender, onClick = { onChoose(ThemePalette.Lavender) }, modifier = Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun InputModePicker(current: InputMode, onChoose: (InputMode) -> Unit) {
    Column {
        Text(
            text = "입력 방식",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (current == InputMode.CellFirst)
                "셀을 먼저 누르고, 숫자를 눌러 채워요."
            else
                "숫자를 먼저 골라 두고, 셀을 눌러 채워요.",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeChoiceChip(label = "셀-우선", selected = current == InputMode.CellFirst, onClick = { onChoose(InputMode.CellFirst) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "숫자-우선", selected = current == InputMode.NumberFirst, onClick = { onChoose(InputMode.NumberFirst) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FontScalePicker(current: Float, onChoose: (Float) -> Unit) {
    Column {
        Text(
            text = "글자 크기 (보드)",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeChoiceChip(label = "작게", selected = current < 0.95f, onClick = { onChoose(0.85f) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "보통", selected = current in 0.95f..1.05f, onClick = { onChoose(1.0f) }, modifier = Modifier.weight(1f))
            ThemeChoiceChip(label = "크게", selected = current > 1.05f, onClick = { onChoose(1.25f) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ThemeChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

@Composable
private fun ProEntryRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "SudokuSage Pro",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "광고 제거 · 무제한 힌트 · 클라우드 동기화",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        content()
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun VolumeSlider(value: Float, onChange: (Float) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
        )
        Slider(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.weight(1f),
        )
    }
}
