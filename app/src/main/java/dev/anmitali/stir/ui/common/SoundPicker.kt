package dev.anmitali.stir.ui.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.IntentCompat
import dev.anmitali.stir.alarm.SILENT_SOUND_URI

@Composable
fun SoundPickerRow(title: String, soundUri: String?, onSoundSelected: (String?) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri = result.data?.let {
            IntentCompat.getParcelableExtra(
                it,
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                Uri::class.java
            )
        }
        onSoundSelected(uri?.toString() ?: SILENT_SOUND_URI)
    }

    SettingRow(title = title, subtitle = soundLabel(context, soundUri)) {
        androidx.compose.material3.TextButton(onClick = {
            val currentUri = when (soundUri) {
                null -> RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
                SILENT_SOUND_URI -> null
                else -> Uri.parse(soundUri)
            }
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM),
                )
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
            }
            launcher.launch(intent)
        }) {
            androidx.compose.material3.Text("Change")
        }
    }
}

@Composable
private fun soundLabel(context: Context, soundUri: String?): String = when (soundUri) {
    null -> "Default"
    SILENT_SOUND_URI -> "Silent"
    else -> remember(soundUri) {
        runCatching { RingtoneManager.getRingtone(context, Uri.parse(soundUri))?.getTitle(context) }
            .getOrNull() ?: "Custom sound"
    }
}
