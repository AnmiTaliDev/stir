package dev.anmitali.stir.ui.alarmedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.anmitali.stir.domain.model.Alarm
import dev.anmitali.stir.domain.model.AlarmGroup
import dev.anmitali.stir.domain.model.RepeatDays
import dev.anmitali.stir.domain.model.SnoozeConfig
import dev.anmitali.stir.ui.common.SettingRow
import dev.anmitali.stir.ui.common.SoundPickerRow
import dev.anmitali.stir.ui.common.WEEKDAYS
import dev.anmitali.stir.ui.common.WEEKENDS
import dev.anmitali.stir.ui.common.stirViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long,
    onDone: () -> Unit,
) {
    val viewModel = stirViewModel { app ->
        AlarmEditViewModel(alarmId, app.alarmRepository, app.alarmGroupRepository, app.settingsRepository, app.schedulingCoordinator)
    }
    val uiState by viewModel.uiState.collectAsState()
    val alarm = uiState.alarm

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isNew) "New alarm" else "Edit alarm") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isNew) {
                        IconButton(onClick = { viewModel.delete() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete alarm")
                        }
                    }
                    TextButton(onClick = { viewModel.save() }, enabled = alarm != null) {
                        Text("Save")
                    }
                },
            )
        },
    ) { padding ->
        if (alarm == null) return@Scaffold

        var showPersistentSheet by rememberSaveable { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                StirTimePicker(
                    hour = alarm.hour,
                    minute = alarm.minute,
                    onTimeChange = viewModel::setTime,
                )
            }

            item {
                RepeatDaysSection(
                    alarm = alarm,
                    onToggleDay = viewModel::toggleDay,
                    onSetRepeatDays = viewModel::setRepeatDays,
                )
            }

            item {
                OutlinedTextField(
                    value = alarm.label,
                    onValueChange = viewModel::setLabel,
                    label = { Text("Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item { SoundPickerRow(title = "Sound", soundUri = alarm.soundUri, onSoundSelected = viewModel::setSoundUri) }

            item {
                SettingRow(title = "Vibrate") {
                    Switch(checked = alarm.vibrationEnabled, onCheckedChange = viewModel::setVibrationEnabled)
                }
            }

            item { VolumeRow(alarm.volume, onVolumeChange = viewModel::setVolume) }

            item { SnoozeSection(alarm.snooze, onChange = viewModel::setSnooze) }

            item {
                GroupSection(
                    groups = uiState.groups,
                    selectedGroupId = alarm.groupId,
                    onGroupSelected = viewModel::setGroup,
                )
            }

            item {
                SettingRow(title = "Persistent", subtitle = "One quiet phase you can't dismiss, then rings until stopped") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (alarm.persistent.enabled) {
                            TextButton(onClick = { showPersistentSheet = true }) { Text("Configure") }
                        }
                        Switch(
                            checked = alarm.persistent.enabled,
                            onCheckedChange = { viewModel.setPersistent(alarm.persistent.copy(enabled = it)) },
                        )
                    }
                }
            }
        }

        if (showPersistentSheet) {
            PersistentConfigSheet(
                config = alarm.persistent,
                onDismiss = { showPersistentSheet = false },
                onChange = viewModel::setPersistent,
            )
        }
    }
}

@Composable
private fun RepeatDaysSection(
    alarm: Alarm,
    onToggleDay: (DayOfWeek, Boolean) -> Unit,
    onSetRepeatDays: (RepeatDays) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Repeat", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RepeatPresetChip(
                    label = "Every day",
                    selected = alarm.repeatDays.isEveryDay,
                    onClick = { onSetRepeatDays(RepeatDays.ALL) },
                )
                RepeatPresetChip(
                    label = "Weekdays",
                    selected = alarm.repeatDays.toSet() == WEEKDAYS,
                    onClick = { onSetRepeatDays(RepeatDays.of(WEEKDAYS)) },
                )
                RepeatPresetChip(
                    label = "Weekends",
                    selected = alarm.repeatDays.toSet() == WEEKENDS,
                    onClick = { onSetRepeatDays(RepeatDays.of(WEEKENDS)) },
                )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DayOfWeek.entries) { day ->
                    val selected = day in alarm.repeatDays
                    FilterChip(
                        selected = selected,
                        onClick = { onToggleDay(day, !selected) },
                        label = { Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RepeatPresetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun VolumeRow(volume: Float, onVolumeChange: (Float) -> Unit) {
    Column {
        Text("Volume", style = MaterialTheme.typography.titleMedium)
        Slider(value = volume, onValueChange = onVolumeChange)
    }
}

@Composable
private fun SnoozeSection(snooze: SnoozeConfig, onChange: (SnoozeConfig) -> Unit) {
    Column {
        SettingRow(title = "Snooze") {
            Switch(checked = snooze.enabled, onCheckedChange = { onChange(snooze.copy(enabled = it)) })
        }
        if (snooze.enabled) {
            StepperRow(
                label = "Duration (min)",
                value = snooze.durationMinutes,
                range = 1..30,
                onChange = { onChange(snooze.copy(durationMinutes = it)) },
            )
            StepperRow(
                label = "Max snoozes",
                value = snooze.maxCount ?: 0,
                range = 0..10,
                unlimitedAtZero = true,
                onChange = { onChange(snooze.copy(maxCount = if (it == 0) null else it)) },
            )
        }
    }
}

@Composable
private fun StepperRow(label: String, value: Int, range: IntRange, unlimitedAtZero: Boolean = false, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value - 1 >= range.first) onChange(value - 1) }) { Text("-", fontWeight = FontWeight.Bold) }
            Text(if (unlimitedAtZero && value == 0) "Unlimited" else value.toString(), modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { if (value + 1 <= range.last) onChange(value + 1) }) { Text("+", fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupSection(
    groups: List<AlarmGroup>,
    selectedGroupId: Long?,
    onGroupSelected: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = groups.firstOrNull { it.id == selectedGroupId }?.name ?: "None"

    Column {
        Text("Group", style = MaterialTheme.typography.titleMedium)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize(),
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = { onGroupSelected(null); expanded = false },
                )
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name) },
                        onClick = { onGroupSelected(group.id); expanded = false },
                    )
                }
            }
        }
    }
}
