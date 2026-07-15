package dev.anmitali.stir.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.anmitali.stir.StirApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as StirApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.schedulingCoordinator.rescheduleAllEnabled()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
