package dev.anmitali.stir.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

const val SILENT_SOUND_URI = "silent"

private const val VOLUME_RAMP_STEPS = 24

class AlarmPlaybackController(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var rampJob: Job? = null
    private var muted = false

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun startLoud(
        soundUri: String?,
        targetVolume: Float,
        vibrationEnabled: Boolean,
        rampFrom: Float?,
        rampDurationSeconds: Int,
        muteAudio: Boolean,
        scope: CoroutineScope,
    ) {
        muted = muteAudio
        ensurePlayer(soundUri)
        rampJob?.cancel()
        if (!muteAudio && rampFrom != null && rampDurationSeconds > 0) {
            rampJob = scope.launch { rampVolume(rampFrom, targetVolume, rampDurationSeconds) }
        } else {
            setVolume(if (muteAudio) 0f else targetVolume)
        }
        if (vibrationEnabled) startVibration() else stopVibration()
    }

    fun switchToQuiet(quietVolume: Float, vibrationEnabled: Boolean, muteAudio: Boolean) {
        rampJob?.cancel()
        muted = muteAudio
        setVolume(if (muteAudio) 0f else quietVolume)
        if (vibrationEnabled) stopVibration()
    }

    fun stop() {
        rampJob?.cancel()
        mediaPlayer?.runCatching { stop(); release() }
        mediaPlayer = null
        stopVibration()
    }

    private fun ensurePlayer(soundUri: String?) {
        if (mediaPlayer != null || soundUri == SILENT_SOUND_URI) return
        val uri = soundUri?.let(Uri::parse)
            ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: return
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            isLooping = true
            runCatching {
                setDataSource(context, uri)
                prepare()
                start()
            }
        }
    }

    private fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(clamped, clamped)
    }

    private suspend fun rampVolume(from: Float, to: Float, durationSeconds: Int) {
        val stepDelayMs = max(1L, (durationSeconds * 1000L) / VOLUME_RAMP_STEPS)
        setVolume(from)
        for (step in 1..VOLUME_RAMP_STEPS) {
            delay(stepDelayMs)
            val fraction = step.toFloat() / VOLUME_RAMP_STEPS
            setVolume(from + (to - from) * fraction)
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 700, 500)
        val effect = VibrationEffect.createWaveform(pattern, 0)
        vibrator.vibrate(effect)
    }

    private fun stopVibration() {
        vibrator.cancel()
    }
}
