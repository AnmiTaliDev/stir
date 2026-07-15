package dev.anmitali.stir.alarm.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.stir.alarm.ACTION_SNOOZE
import dev.anmitali.stir.alarm.ACTION_STOP
import dev.anmitali.stir.alarm.AlarmPlaybackService
import dev.anmitali.stir.alarm.EXTRA_ALARM_ID
import dev.anmitali.stir.alarm.RingSessionState
import dev.anmitali.stir.ui.theme.StirTheme

class AlarmRingActivity : ComponentActivity() {

    private var alarmId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        showOverLockScreen()
        hideSystemBars()

        setContent {
            StirTheme {
                val state by RingSessionState.state.collectAsStateWithLifecycle()
                var hasSeenOwnState by remember { mutableStateOf(false) }

                LaunchedEffect(state) {
                    if (state != null && state?.alarmId == alarmId) {
                        hasSeenOwnState = true
                    } else if (hasSeenOwnState) {
                        finish()
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    AlarmRingScreen(
                        state = state?.takeIf { it.alarmId == alarmId },
                        onStop = { sendAction(ACTION_STOP) },
                        onSnooze = { sendAction(ACTION_SNOOZE) },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        alarmId = intent.getLongExtra(EXTRA_ALARM_ID, alarmId)
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun sendAction(action: String) {
        val serviceIntent = Intent(this, AlarmPlaybackService::class.java)
            .setAction(action)
            .putExtra(EXTRA_ALARM_ID, alarmId)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
