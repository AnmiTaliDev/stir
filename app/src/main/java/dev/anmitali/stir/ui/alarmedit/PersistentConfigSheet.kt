package dev.anmitali.stir.ui.alarmedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.anmitali.stir.domain.model.PersistentConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersistentConfigSheet(
    config: PersistentConfig,
    onDismiss: () -> Unit,
    onChange: (PersistentConfig) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("Persistent", style = MaterialTheme.typography.titleLarge)
            Text(
                "Stopping the alarm (or letting it ring unanswered) starts a quiet phase that " +
                        "cannot be dismissed. Once it passes on its own, the alarm rings loud again and " +
                        "keeps ringing until you stop it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            LabeledSlider(
                label = "Quiet phase duration",
                valueText = "${config.quietDurationSeconds}s",
                value = config.quietDurationSeconds.toFloat(),
                range = 15f..300f,
                onChange = { onChange(config.copy(quietDurationSeconds = it.toInt())) },
            )

            LabeledSlider(
                label = "Quiet volume",
                valueText = "${(config.quietVolume * 100).toInt()}%",
                value = config.quietVolume,
                range = 0f..1f,
                onChange = { onChange(config.copy(quietVolume = it)) },
            )

            LabeledSlider(
                label = "Ramp back to loud over",
                valueText = "${config.rampDurationSeconds}s",
                value = config.rampDurationSeconds.toFloat(),
                range = 0f..60f,
                onChange = { onChange(config.copy(rampDurationSeconds = it.toInt())) },
            )
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
