package dev.anmitali.stir.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.os.Build
import dev.anmitali.stir.data.settings.DndBehavior
import dev.anmitali.stir.ui.alarmedit.PersistentConfigSheet
import dev.anmitali.stir.ui.common.SettingRow
import dev.anmitali.stir.ui.common.SoundPickerRow
import dev.anmitali.stir.ui.common.stirViewModel
import dev.anmitali.stir.ui.theme.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenAbout: () -> Unit) {
    val viewModel = stirViewModel { app -> SettingsViewModel(app.settingsRepository) }
    val settings by viewModel.settings.collectAsState()
    var showPersistentDefaultsSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { SectionLabel("Appearance") }
            item {
                Column {
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, AppThemeMode.entries.size),
                            ) {
                                Text(mode.label())
                            }
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    SettingRow(title = "Dynamic color", subtitle = "Match the system wallpaper palette") {
                        Switch(checked = settings.dynamicColorEnabled, onCheckedChange = viewModel::setDynamicColorEnabled)
                    }
                }
            }

            item { HorizontalDivider() }

            item { SectionLabel("Do Not Disturb") }
            item {
                Column {
                    DndChoiceRow(
                        title = "Ring through Do Not Disturb",
                        subtitle = "Recommended: alarms use the dedicated alarm volume and always sound",
                        selected = settings.dndBehavior == DndBehavior.RING_THROUGH_DND,
                        onSelect = { viewModel.setDndBehavior(DndBehavior.RING_THROUGH_DND) },
                    )
                    DndChoiceRow(
                        title = "Respect Do Not Disturb",
                        subtitle = "Vibrate only while Do Not Disturb is active",
                        selected = settings.dndBehavior == DndBehavior.RESPECT_DND_VIBRATE_ONLY,
                        onSelect = { viewModel.setDndBehavior(DndBehavior.RESPECT_DND_VIBRATE_ONLY) },
                    )
                }
            }

            item { HorizontalDivider() }

            item { SectionLabel("New alarm defaults") }
            item { SoundPickerRow(title = "Default sound", soundUri = settings.defaultSoundUri, onSoundSelected = viewModel::setDefaultSoundUri) }
            item {
                SettingRow(title = "Default vibrate") {
                    Switch(checked = settings.defaultVibrationEnabled, onCheckedChange = viewModel::setDefaultVibrationEnabled)
                }
            }
            item {
                Column {
                    Text("Default volume", style = MaterialTheme.typography.titleMedium)
                    Slider(value = settings.defaultVolume, onValueChange = viewModel::setDefaultVolume)
                }
            }
            item {
                SettingRow(title = "Default persistent", subtitle = "Applied to newly created alarms") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (settings.defaultPersistent.enabled) {
                            TextButton(onClick = { showPersistentDefaultsSheet = true }) { Text("Configure") }
                        }
                        Switch(
                            checked = settings.defaultPersistent.enabled,
                            onCheckedChange = { viewModel.setDefaultPersistent(settings.defaultPersistent.copy(enabled = it)) },
                        )
                    }
                }
            }
            item {
                RingTimeoutRow(seconds = settings.ringTimeoutSeconds, onChange = viewModel::setRingTimeoutSeconds)
            }

            item { HorizontalDivider() }

            item {
                SettingRow(title = "About Stir") {
                    TextButton(onClick = onOpenAbout) { Text("Open") }
                }
            }
        }
    }

    if (showPersistentDefaultsSheet) {
        PersistentConfigSheet(
            config = settings.defaultPersistent,
            onDismiss = { showPersistentDefaultsSheet = false },
            onChange = viewModel::setDefaultPersistent,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun DndChoiceRow(title: String, subtitle: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RingTimeoutRow(seconds: Int, onChange: (Int) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Ring timeout", style = MaterialTheme.typography.titleMedium)
            Text("${seconds / 60}m ${seconds % 60}s", color = MaterialTheme.colorScheme.primary)
        }
        Text(
            "How long a loud phase rings before timing out unattended",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = seconds.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 30f..900f,
        )
    }
}

private fun AppThemeMode.label(): String = when (this) {
    AppThemeMode.LIGHT -> "Light"
    AppThemeMode.DARK -> "Dark"
    AppThemeMode.SYSTEM -> "System"
}
