package dev.anmitali.stir.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.anmitali.stir.StirApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId < 0) return

        val app = context.applicationContext as StirApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = app.alarmRepository.getById(alarmId) ?: return@launch
                app.schedulingCoordinator.clearSnoozeMarker(alarmId)
                app.schedulingCoordinator.rescheduleAfterFiring(alarm)

                val serviceIntent = Intent(context, AlarmPlaybackService::class.java)
                    .setAction(AlarmPlaybackService.ACTION_RING)
                    .putExtra(EXTRA_ALARM_ID, alarmId)
                ContextCompat.startForegroundService(context, serviceIntent)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
